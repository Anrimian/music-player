package com.github.anrimian.musicplayer.data.repositories.library.edit

import android.os.Build
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper
import com.github.anrimian.musicplayer.data.models.composition.file.StorageCompositionSource
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.EditorTimeoutException
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveFolderToItselfException
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveInTheSameFolderException
import com.github.anrimian.musicplayer.data.repositories.library.edit.models.CompositionMoveData
import com.github.anrimian.musicplayer.data.storage.exceptions.GenreAlreadyPresentException
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.change.ChangedCompositionPath
import com.github.anrimian.musicplayer.domain.models.composition.change.CompositionPath
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.tags.AudioFileInfo
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.image.ImageSource
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.Date
import java.util.concurrent.TimeUnit

class EditorRepositoryImpl(
    private val sourceEditor: CompositionSourceEditor,
    private val filesDataSource: StorageFilesDataSource,
    private val libraryDatabase: LibraryDatabase,
    private val compositionsDao: CompositionsDaoWrapper,
    private val albumsDao: AlbumsDaoWrapper,
    private val artistsDao: ArtistsDaoWrapper,
    private val genresDao: GenresDaoWrapper,
    private val foldersDao: FoldersDaoWrapper,
    private val playListsDao: PlayListsDaoWrapper,
    private val storageMusicProvider: StorageMusicProvider,
    private val playListsRepository: PlayListsRepository,
    private val libraryRepository: LibraryRepository,
    private val scheduler: Scheduler
) : EditorRepository {

    private var removedGenreName: String? = null
    private var removedGenreCompositionId: Long = 0
    private var removedGenreCompositionSource: CompositionContentSource? = null
    private var removedGenrePosition = 0

    override fun changeCompositionGenre(
        compositionId: Long,
        source: CompositionContentSource,
        oldGenre: String,
        newGenre: String
    ): Completable {
        val action = Completable.fromAction {
            if (genresDao.containsCompositionGenre(compositionId, newGenre)) {
                throw GenreAlreadyPresentException()
            }
        }.andThen(sourceEditor.changeCompositionGenre(source, oldGenre, newGenre))
            .doOnComplete {
                setCompositionInitialSourceToApp(compositionId)
                genresDao.changeCompositionGenre(compositionId, oldGenre, newGenre)
            }
        return performSourceUpdate(source, action)
    }

    override fun addCompositionGenre(
        compositionId: Long,
        source: CompositionContentSource,
        newGenre: String
    ): Completable {
        val action = Completable.fromAction {
            if (genresDao.containsCompositionGenre(compositionId, newGenre)) {
                throw GenreAlreadyPresentException()
            }
        }.andThen(sourceEditor.addCompositionGenre(source, newGenre))
            .doOnComplete {
                setCompositionInitialSourceToApp(compositionId)
                genresDao.addCompositionToGenre(compositionId, newGenre)
            }
        return performSourceUpdate(source, action)
    }

    override fun moveGenre(
        compositionId: Long,
        source: CompositionContentSource,
        from: Int,
        to: Int
    ): Completable {
        return performSourceUpdate(source, sourceEditor.moveGenre(source, from, to)
            .doOnComplete {
                setCompositionInitialSourceToApp(compositionId)
                genresDao.moveGenres(compositionId, from, to)
            }
        )
    }

    override fun removeCompositionGenre(
        compositionId: Long,
        source: CompositionContentSource,
        genre: String
    ): Completable {
        return performSourceUpdate(source, sourceEditor.removeCompositionGenre(source, genre)
            .doOnComplete {
                setCompositionInitialSourceToApp(compositionId)
                removedGenreName = genre
                removedGenreCompositionId = compositionId
                removedGenreCompositionSource = source
                removedGenrePosition = genresDao.removeCompositionFromGenre(compositionId, genre)
            }
        )
    }

    override fun restoreRemovedCompositionGenre(): Completable {
        return Single.fromCallable {
            val name = removedGenreName ?: return@fromCallable Completable.complete()
            val id = removedGenreCompositionId
            val source = removedGenreCompositionSource!!
            val position = removedGenrePosition
            val action = Completable.fromAction {
                if (genresDao.containsCompositionGenre(id, name)) {
                    throw GenreAlreadyPresentException()
                }
            }.andThen(sourceEditor.addCompositionGenre(source, name, position))
                .doOnComplete { genresDao.addCompositionToGenre(id, name, position) }

            performSourceUpdate(source, action)
        }.flatMapCompletable { c -> c }
    }

    override fun changeCompositionAuthor(
        compositionId: Long,
        source: CompositionContentSource,
        newAuthor: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionAuthor(source, newAuthor)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateArtist(compositionId, newAuthor) }
        )
    }

    override fun changeCompositionAlbumArtist(
        compositionId: Long,
        source: CompositionContentSource,
        newAuthor: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionAlbumArtist(source, newAuthor)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateAlbumArtist(compositionId, newAuthor) }
        )
    }

    override fun changeCompositionAlbum(
        compositionId: Long,
        source: CompositionContentSource,
        newAlbum: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionAlbum(source, newAlbum)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateAlbum(compositionId, newAlbum) }
        )
    }

    override fun changeCompositionTitle(
        compositionId: Long,
        source: CompositionContentSource,
        title: String
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionTitle(source, title)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateTitle(compositionId, title) }
        )
    }

    override fun changeCompositionTrackNumber(
        compositionId: Long,
        source: CompositionContentSource,
        trackNumber: Long?
    ): Completable {
        return performSourceUpdate(source,
            sourceEditor.setCompositionTrackNumber(source, trackNumber)
                .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
                .doOnComplete { compositionsDao.updateTrackNumber(compositionId, trackNumber) }
        )
    }

    override fun changeCompositionDiscNumber(
        compositionId: Long,
        source: CompositionContentSource,
        discNumber: Long?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionDiscNumber(source, discNumber)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateDiscNumber(compositionId, discNumber) }
        )
    }

    override fun changeCompositionComment(
        compositionId: Long,
        source: CompositionContentSource,
        text: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionComment(source, text)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateComment(compositionId, text) }
        )
    }

    override fun changeCompositionLyrics(
        compositionId: Long,
        source: CompositionContentSource,
        text: String?
    ): Completable {
        return performSourceUpdate(source, sourceEditor.setCompositionLyrics(source, text)
            .doOnComplete { setCompositionInitialSourceToApp(compositionId) }
            .doOnComplete { compositionsDao.updateLyrics(compositionId, text) }
        )
    }

    override fun changeCompositionFileName(
        compositionId: Long,
        fileName: String
    ) = performChangeFilesPath(
        { listOf(compositionsDao.getCompositionMoveData(compositionId)) },
        { compositions ->
            val composition = compositions.first()
            val newName = filesDataSource.renameCompositionFile(composition, fileName)
            listOf(composition.id to newName)
        },
        {} //do nothing, file will be renamed in root method anyway
    ).map(List<ChangedCompositionPath>::first)

    override fun changeFolderName(
        folderId: Long,
        newFolderName: String
    ) = performChangeFilesPath(
        { compositionsDao.getAllCompositionsInFolder(folderId) },
        { compositions ->
            val folderRelativePath = foldersDao.getFullFolderPath(folderId)
            filesDataSource.renameCompositionsFolder(
                compositions,
                folderRelativePath,
                newFolderName
            )
        },
        {
            foldersDao.changeFolderName(folderId, newFolderName)
            val newRelativePath = foldersDao.getFullFolderPath(folderId)
            libraryRepository.deleteIgnoredFolder(newRelativePath)
        }
    )

    override fun moveFiles(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        toFolderId: Long?
    ) = Completable.fromAction { verifyFolderMove(fromFolderId, toFolderId) }
        .andThen(foldersDao.extractAllCompositionsFromFiles(files))
        .subscribeOn(scheduler)
        .flatMap { compositionIds ->
            performChangeFilesPath(
                { compositionsDao.getCompositionsMoveData(compositionIds) },
                { compositions ->
                    val fromFolderRelativePath = foldersDao.getFolderRelativePath(fromFolderId)
                    val toFolderRelativePath = foldersDao.getFolderRelativePath(toFolderId)
                    verifyFoldersPathMove(files, fromFolderRelativePath, toFolderRelativePath)
                    filesDataSource.moveCompositionsToDirectory(
                        compositions,
                        fromFolderRelativePath,
                        toFolderRelativePath
                    )
                },
                { foldersDao.updateFolderId(files, toFolderId) }
            )
        }

    override fun moveFilesToNewDirectory(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        targetParentFolderId: Long?,
        directoryName: String?
    ) = foldersDao.extractAllCompositionsFromFiles(files)
        .subscribeOn(scheduler)
        .flatMap { compositionIds ->
            performChangeFilesPath(
                { compositionsDao.getCompositionsMoveData(compositionIds) },
                { compositions ->
                    val fromFolderRelativePath = foldersDao.getFolderRelativePath(fromFolderId)
                    val parentFolderRelativePath = foldersDao.getFolderRelativePath(targetParentFolderId)

                    val toFolderRelativePath = "$parentFolderRelativePath/$directoryName"
                    verifyFoldersPathMove(files, fromFolderRelativePath, toFolderRelativePath)

                    filesDataSource.moveCompositionsToDirectory(
                        compositions,
                        fromFolderRelativePath,
                        toFolderRelativePath
                    )
                },
                {
                    val folderId = foldersDao.moveCompositionsIntoFolder(
                        targetParentFolderId,
                        directoryName,
                        files
                    )
                    val newRelativePath = foldersDao.getFullFolderPath(folderId)
                    libraryRepository.deleteIgnoredFolder(newRelativePath)
                }
            )
        }

    private fun performChangeFilesPath(
        compositionsProvider: () -> Collection<CompositionMoveData>,
        fileAction: (compositions: Collection<CompositionMoveData>) -> List<Pair<Long, String>>,
        dbAction: () -> Unit
    ) = Single.fromCallable {
        storageMusicProvider.setContentObserverEnabled(false)
        try {
            val compositions = compositionsProvider()

            val changedFileNames = fileAction(compositions)
            val compositionIds = compositions.map(CompositionMoveData::id)
            libraryDatabase.runInTransaction {
                for ((id, name) in changedFileNames) {
                    compositionsDao.updateCompositionFileName(id, name)
                }
                setCompositionIdsInitialSourceToApp(compositionIds)
                dbAction()//order is important, should be at the end
            }
            val playlists = playListsDao.getPlayListsForCompositions(compositionIds)
            playlists.forEach(playListsRepository::updatePlaylistCache)
            //btw, if playlist file contains non-existent records, they will be deleted

            val changedCompositions = compositionsProvider()
            //in case of merged folders, amount of changed compositions can be more than on start.
            // Extra compositions will not play any role in returned paths
            val changedCompositionsMap = changedCompositions.associateBy { c -> c.id }
            return@fromCallable compositions.map { composition ->
                val changedComposition = changedCompositionsMap[composition.id]
                    ?: throw IllegalStateException("missing composition: ${composition.fileName}")
                ChangedCompositionPath(
                    CompositionPath(composition.fileName, composition.parentPath),
                    CompositionPath(changedComposition.fileName, changedComposition.parentPath),
                )
            }
        } finally {
            storageMusicProvider.setContentObserverEnabled(true)
        }
    }.subscribeOn(scheduler)

    override fun updateAlbumName(
        name: String?,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        albumId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        val action = sourceEditor.setCompositionsAlbum(sources, name, editingSubject)
            .doOnComplete { albumsDao.updateAlbumName(name, albumId) }
        return performSourceUpdate(compositionIds, sources, action)
    }

    override fun updateAlbumArtist(
        artist: String?,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        albumId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        val action = sourceEditor.setCompositionsAlbumArtist(sources, artist, editingSubject)
            .doOnComplete { albumsDao.updateAlbumArtist(albumId, artist) }
        return performSourceUpdate(compositionIds, sources, action)
    }

    override fun updateArtistName(
        name: String?,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        artistId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        val action = Single.fromCallable { artistsDao.getAuthorName(artistId) }
            .flatMapCompletable { oldName ->
                sourceEditor.renameCompositionsAuthor(sources, oldName, name, editingSubject)
            }
            .doOnComplete { artistsDao.updateArtistName(name, artistId) }
        return performSourceUpdate(compositionIds, sources, action)
    }

    override fun updateGenreName(
        name: String,
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        genreId: Long,
        editingSubject: BehaviorSubject<Long>
    ): Completable {
        val action = Single.fromCallable { genresDao.getGenreName(genreId) }
            .flatMapCompletable { oldName ->
                sourceEditor.setCompositionsGenre(sources, oldName, name, editingSubject)
            }
            .doOnComplete { genresDao.updateGenreName(name, genreId, compositionIds) }
        return performSourceUpdate(compositionIds, sources, action)
    }

    override fun changeCompositionAlbumArt(
        compositionId: Long,
        source: CompositionContentSource,
        imageSource: ImageSource?
    ): Completable {
        val action = sourceEditor.changeCompositionAlbumArt(source, imageSource)
            .doOnSuccess { newSize ->
                setCompositionInitialSourceToApp(compositionId)
                compositionsDao.updateCoverModifyTimeAndSize(compositionId, newSize, Date())
            }
            .ignoreElement()
            .timeout(
                CHANGE_COVER_TIMEOUT_MILLIS,
                TimeUnit.MILLISECONDS,
                Completable.error(EditorTimeoutException())
            )
        return performSourceUpdate(source, action)
    }

    override fun removeCompositionAlbumArt(
        compositionId: Long,
        source: CompositionContentSource
    ): Completable {
        return performSourceUpdate(source, sourceEditor.removeCompositionAlbumArt(source)
            .doOnSuccess { newSize ->
                setCompositionInitialSourceToApp(compositionId)
                compositionsDao.updateCoverModifyTimeAndSize(compositionId, newSize, Date())
                runSystemRescan(source)
            }
            .ignoreElement()
            .timeout(
                CHANGE_COVER_TIMEOUT_MILLIS,
                TimeUnit.MILLISECONDS,
                Completable.error(EditorTimeoutException())
            )
        )
    }

    /**
     * Album-artist in android system and in common file has conflicts. This function
     * updates media library by real file source tags.
     */
    override fun updateTagsFromSource(
        source: CompositionContentSource,
        composition: FullComposition
    ): Completable {
        return sourceEditor.getAudioFileInfo(source)
            .flatMapCompletable { info -> updateCompositionTags(composition, info) }
            .doOnComplete { setCompositionInitialSourceToApp(composition.id) }
            .subscribeOn(scheduler)
    }

    private fun updateCompositionTags(
        composition: FullComposition,
        fileInfo: AudioFileInfo
    ): Completable {
        return Completable.fromAction {
            compositionsDao.updateCompositionByFileInfo(composition, fileInfo)
        }
    }

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

    private fun performSourceUpdate(
        source: CompositionContentSource?,
        completable: Completable
    ): Completable {
        return completable
            .doOnSubscribe { storageMusicProvider.setContentObserverEnabled(false) }
            .doOnComplete { runSystemRescan(source) }
            .doFinally { storageMusicProvider.setContentObserverEnabled(true) }
            .subscribeOn(scheduler)
    }

    private fun performSourceUpdate(
        compositionIds: List<Long>,
        sources: List<CompositionContentSource>,
        completable: Completable
    ): Completable {
        return completable
            .doOnSubscribe { storageMusicProvider.setContentObserverEnabled(false) }
            .doOnComplete {
                setCompositionIdsInitialSourceToApp(compositionIds)
                runSystemRescan(sources)
            }
            .doFinally { storageMusicProvider.setContentObserverEnabled(true) }
            .subscribeOn(scheduler)
    }

    private fun runSystemRescan(sources: List<CompositionContentSource>) {
        for (source in sources) {
            runSystemRescan(source)
        }
    }

    private fun runSystemRescan(source: CompositionContentSource?) {
        if (source is StorageCompositionSource) {
            val (uri) = source
            storageMusicProvider.scanMedia(uri)
        }
    }

    /**
     * Set initial source to app to display in-app delete dialog
     */
    private fun setCompositionInitialSourceToApp(id: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            compositionsDao.updateCompositionInitialSource(
                id,
                InitialSource.APP,
                InitialSource.LOCAL
            )
        }
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

    companion object {
        private const val CHANGE_COVER_TIMEOUT_MILLIS: Long = 25000
    }

}