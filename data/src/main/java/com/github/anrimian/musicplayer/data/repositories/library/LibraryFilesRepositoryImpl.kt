package com.github.anrimian.musicplayer.data.repositories.library

import android.os.Build
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.playlist.PlaylistsDaoWrapper
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveFolderToItselfException
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveInTheSameFolderException
import com.github.anrimian.musicplayer.data.storage.files.StorageFileInfo
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource
import com.github.anrimian.musicplayer.data.storage.files.StorageMoveOperation
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.domain.models.composition.AudioFileInfo
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.change.ChangedCompositionPath
import com.github.anrimian.musicplayer.domain.models.composition.change.MoveRequest
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FilesChangeResult
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.LibraryFilesRepository
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.PlaylistsRepository
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import com.github.anrimian.musicplayer.domain.utils.FileUtilsKt
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import java.io.FileNotFoundException
import java.util.LinkedList

class LibraryFilesRepositoryImpl(
    private val filesDataSource: StorageFilesDataSource,
    private val libraryDatabase: LibraryDatabase,
    private val compositionsDao: CompositionsDaoWrapper,
    private val foldersDao: FoldersDaoWrapper,
    private val playListsDao: PlaylistsDaoWrapper,
    private val systemAudioCatalogProvider: SystemAudioCatalogProvider,
    private val playListsRepository: PlaylistsRepository,
    private val libraryRepository: LibraryRepository,
    private val ioScheduler: Scheduler,
    private val isPathChangeForNonExistentFilesAllowed: Boolean
): LibraryFilesRepository {

    override fun changeCompositionFileName(
        compositionId: Long,
        fileName: String
    ): Single<FilesChangeResult> = performChangeFilesPath(
        compositionsProvider = { listOf(compositionsDao.getAudioFileInfo(compositionId)) },
        fileAction = { compositions ->
            val composition = compositions.first()

            val newFileName = getUniqueFileName(
                composition.parentPath,
                FileUtilsKt.applyNewName(composition.fileName, fileName)
            )
            val storageId = composition.storageId
            val result = if (storageId == null) {
                if (!isPathChangeForNonExistentFilesAllowed) {
                    throw FileNotFoundException(composition.getPath())
                }
                newFileName
            } else {
                filesDataSource.renameCompositionFile(storageId, newFileName)
            }
            listOf(composition.id to result)
        },
        dbAction = { null } //do nothing, file will be renamed in root method anyway
    )

    override fun changeFolderName(
        folderId: Long,
        newFolderName: String
    ): Single<FilesChangeResult> = performChangeFilesPath(
        compositionsProvider = { compositionsDao.getAllFilesInFolder(folderId) },
        fileAction = { compositions ->
            val folderRelativePath = foldersDao.getFullFolderPath(folderId)
            val newFolderRelativePath = FileUtils.replaceFileName(folderRelativePath, newFolderName)
            val newNames = LinkedList<Pair<Long, String>>()
            val storageFiles = prepareParentPathChange(
                compositions,
                folderRelativePath,
                newFolderRelativePath,
                newNames
            )
            newNames.addAll(filesDataSource.renameCompositionsDirectory(
                storageFiles,
                folderRelativePath,
                newFolderRelativePath
            ))
            newNames
        },
        dbAction = {
            foldersDao.changeFolderName(folderId, newFolderName)
        },
        restoredFilesFetcher = {
            val newRelativePath = foldersDao.getFullFolderPath(folderId)
            libraryRepository.deleteIgnoredFolder(newRelativePath)
        }
    )

    override fun moveFiles(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        toFolderId: Long?
    ): Single<FilesChangeResult> = Completable.fromAction { verifyFolderMove(fromFolderId, toFolderId) }
        .andThen(foldersDao.extractAllCompositionsFromFiles(files))
        .subscribeOn(ioScheduler)
        .flatMap { compositionIds ->
            performChangeFilesPath(
                compositionsProvider = { compositionsDao.getAudioFilesInfo(compositionIds) },
                fileAction = { compositions ->
                    val fromFolderPath = foldersDao.getFullFolderPath(fromFolderId)
                    val toFolderPath = foldersDao.getFullFolderPath(toFolderId)
                    verifyFoldersPathMove(files, fromFolderPath, toFolderPath)

                    val newNames = LinkedList<Pair<Long, String>>()
                    val storageFiles = prepareParentPathChange(
                        compositions,
                        fromFolderPath,
                        toFolderPath,
                        newNames
                    )
                    newNames.addAll(filesDataSource.moveCompositionsToDirectory(
                        storageFiles,
                        fromFolderPath,
                        toFolderPath
                    ))
                    newNames
                },
                dbAction = { foldersDao.updateFolderId(files, toFolderId) }
            )
        }

    override fun moveFilesToNewDirectory(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        targetParentFolderId: Long?,
        directoryName: String
    ): Single<FilesChangeResult> = foldersDao.extractAllCompositionsFromFiles(files)
        .subscribeOn(ioScheduler)
        .flatMap { compositionIds ->
            performChangeFilesPath(
                compositionsProvider = { compositionsDao.getAudioFilesInfo(compositionIds) },
                fileAction = { compositions ->
                    val fromFolderRelativePath = foldersDao.getFullFolderPath(fromFolderId)
                    val parentFolderRelativePath = foldersDao.getFullFolderPath(targetParentFolderId)

                    val toFolderRelativePath = "$parentFolderRelativePath/$directoryName"
                    verifyFoldersPathMove(files, fromFolderRelativePath, toFolderRelativePath)

                    val newNames = LinkedList<Pair<Long, String>>()
                    val storageFiles = prepareParentPathChange(
                        compositions,
                        fromFolderRelativePath,
                        toFolderRelativePath,
                        newNames
                    )

                    newNames.addAll(filesDataSource.moveCompositionsToDirectory(
                        storageFiles,
                        fromFolderRelativePath,
                        toFolderRelativePath
                    ))
                    newNames
                },
                dbAction = {
                    foldersDao.moveCompositionsIntoFolder(
                        targetParentFolderId,
                        directoryName,
                        files
                    )
                },
                restoredFilesFetcher = { folderId ->
                    val newRelativePath = foldersDao.getFullFolderPath(folderId)
                    libraryRepository.deleteIgnoredFolder(newRelativePath)
                }
            )
        }

    override fun moveFiles(
        requests: List<MoveRequest>
    ): Single<FilesChangeResult> = performChangeFilesPath(
        compositionsProvider = {
            compositionsDao.getAudioFilesInfo(requests.map(MoveRequest::compositionId))
        },
        fileAction = { audioFileInfos ->
            val idCompositionMap = audioFileInfos.associateBy { info -> info.id }
            val changedFileNames = LinkedList<Pair<Long, String>>()

            val moveOperations = LinkedList<StorageMoveOperation>()

            for (request in requests) {
                val composition = idCompositionMap[request.compositionId] ?: continue
                checkPathChange(composition, request, changedFileNames, moveOperations)
            }

            val movedFileNames = filesDataSource.executePathChange(moveOperations)
            changedFileNames.addAll(movedFileNames)

            return@performChangeFilesPath changedFileNames
        },
        dbAction = {
            val folderCache = mutableMapOf<String, Long>()
            val affectedFolderIds = mutableSetOf<Long>()

            for (request in requests) {
                val newParentPath = FileUtils.getParentDirPath(request.newPath)
                val newFolderId = foldersDao.getOrCreateFolder(newParentPath, folderCache)

                compositionsDao.updateFolderId(request.compositionId, newFolderId)
                if (newFolderId != null) {
                    affectedFolderIds.add(newFolderId)
                }
            }
            foldersDao.deleteEmptyFolders()

            return@performChangeFilesPath affectedFolderIds
        },
        restoredFilesFetcher = { folderIds ->
            val singles = folderIds.map { folderId ->
                val newRelativePath = foldersDao.getFullFolderPath(folderId)
                libraryRepository.deleteIgnoredFolder(newRelativePath)
            }
            Single.zip(singles) { results ->
                results.flatMap { result ->
                    @Suppress("UNCHECKED_CAST")
                    result as List<FileKey>
                }
            }
        }
    )

    private fun <R> performChangeFilesPath(
        compositionsProvider: () -> Collection<AudioFileInfo>,
        fileAction: (compositions: Collection<AudioFileInfo>) -> List<Pair<Long, String>>,
        dbAction: () -> R,
        restoredFilesFetcher: (R) -> Single<List<FileKey>> = { Single.just(emptyList()) }
    ): Single<FilesChangeResult> = Single.fromCallable {
        systemAudioCatalogProvider.setContentObserverEnabled(false)
        try {
            val compositions = compositionsProvider()

            val changedFileNames = fileAction(compositions)
            val compositionIds = compositions.map(AudioFileInfo::id)
            val currentTime = System.currentTimeMillis()
            val result = libraryDatabase.runInTransaction<R> {
                for ((id, name) in changedFileNames) {
                    compositionsDao.updateCompositionFileName(id, name)
                }
                for (id in compositionIds) {
                    compositionsDao.setCompositionPathModifyTime(id, currentTime)
                }
                setCompositionIdsInitialSourceToApp(compositionIds)
                return@runInTransaction dbAction() // order is important, should be at the end
            }
            val playlists = playListsDao.getPlayListsForCompositions(compositionIds)
            playlists.forEach(playListsRepository::updatePlaylistCache)

            val changedCompositions = compositionsProvider()
            //in case of merged folders, amount of changed compositions can be more than on start.
            // Extra compositions will not play any role in returned paths
            val changedCompositionsMap = changedCompositions.associateBy { c -> c.id }
            val changedFiles = compositions.map { composition ->
                val changedComposition = changedCompositionsMap[composition.id]
                    ?: throw IllegalStateException("missing composition: ${composition.fileName}")
                ChangedCompositionPath(
                    FileKey(composition.fileName, composition.parentPath),
                    FileKey(changedComposition.fileName, changedComposition.parentPath),
                    composition.pathModifyTime
                )
            }
            return@fromCallable restoredFilesFetcher(result)
                .map { restoredFiles ->
                    FilesChangeResult(changedFiles, restoredFiles, currentTime)
                }
        } finally {
            systemAudioCatalogProvider.setContentObserverEnabled(true)
        }
    }.flatMap { s -> s }
        .subscribeOn(ioScheduler)


    private fun verifyFolderMove(fromFolderId: Long?, toFolderId: Long?) {
        if (fromFolderId == toFolderId) {
            throw MoveInTheSameFolderException("move in the same folder")
        }
    }

    private fun verifyFoldersPathMove(
        files: Collection<FileSource>,
        fromPath: String,
        toPath: String,
    ) {
        for (file in files) {
            if (file !is FolderFileSource) {
                continue
            }
            //check if folder does not move to itself
            val moveFolderPath = fromPath + '/' + file.name
            if (toPath.startsWith(moveFolderPath)) {
                val nextSeparatorIndex = toPath.indexOf('/', moveFolderPath.length)
                if (toPath.length == moveFolderPath.length || nextSeparatorIndex == moveFolderPath.length) {
                    throw MoveFolderToItselfException("moving and destination folders matches")
                }
            }
        }
    }

    private fun prepareParentPathChange(
        compositions: Collection<AudioFileInfo>,
        fromPath: String,
        toPath: String,
        outNewNames: LinkedList<Pair<Long, String>>
    ): LinkedList<StorageFileInfo> {
        val storageFiles = LinkedList<StorageFileInfo>()
        for (composition in compositions) {
            val storageId = composition.storageId
            if (storageId == null) {
                if (!isPathChangeForNonExistentFilesAllowed) {
                    throw FileNotFoundException(composition.getPath())
                }
                // do not rename non-existent file, just check for file name conflicts
                val newParentPath = composition.parentPath.replace(fromPath, toPath)
                val fileName = composition.fileName
                val uniqueName = getUniqueFileName(newParentPath, fileName)
                if (uniqueName != fileName) {
                    outNewNames.add(composition.id to uniqueName)
                }
            } else {
                storageFiles.add(StorageFileInfo(composition.id, storageId, composition.fileName))
            }
        }
        return storageFiles
    }

    private fun checkPathChange(
        audioFileInfo: AudioFileInfo,
        request: MoveRequest,
        outNewNames: LinkedList<Pair<Long, String>>,
        outMoveOperations: LinkedList<StorageMoveOperation>
    ) {
        val newParentPath = FileUtils.getParentDirPath(request.newPath)
        val newFileName = FileUtils.getFileName(request.newPath)

        val storageId = audioFileInfo.storageId
        if (storageId == null) {
            if (!isPathChangeForNonExistentFilesAllowed) {
                throw FileNotFoundException(audioFileInfo.getPath())
            }
            val uniqueName = getUniqueFileName(newParentPath, newFileName)
            if (uniqueName != newFileName) {
                outNewNames.add(audioFileInfo.id to uniqueName)
            }
        } else {
            val storageFileInfo = StorageFileInfo(
                audioFileInfo.id,
                storageId,
                newFileName
            )
            outMoveOperations.add(StorageMoveOperation(storageFileInfo, request.newPath))
        }
    }

    private fun getUniqueFileName(parentPath: String, fileName: String): String {
        var existingId: Long?
        var currentNewName = fileName
        var attemptsCount = 0
        do {
            existingId = compositionsDao.findCompositionIdByFilePath(parentPath, currentNewName)
            if (existingId != null) {
                attemptsCount++
                val ext = FileUtils.getExtension(fileName)
                val newName = FileUtils.formatFileName(fileName, false)
                currentNewName = "$newName ($attemptsCount).$ext"
            }
        } while (existingId != null)
        return currentNewName
    }

    private fun setCompositionIdsInitialSourceToApp(compositionIds: List<Long>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            compositionsDao.updateCompositionIdsInitialSource(
                compositionIds,
                InitialSource.APP,
                InitialSource.LOCAL
            )
        }
    }

}