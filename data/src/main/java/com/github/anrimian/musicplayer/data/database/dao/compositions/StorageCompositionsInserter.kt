package com.github.anrimian.musicplayer.data.database.dao.compositions

import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao
import com.github.anrimian.musicplayer.data.database.mappers.CompositionCorruptionDetector
import com.github.anrimian.musicplayer.data.storage.providers.music.DBComposition
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageAudioFile
import com.github.anrimian.musicplayer.domain.models.common.TimedChange
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus

class StorageCompositionsInserter(
    private val libraryDatabase: LibraryDatabase,
    private val compositionsDao: CompositionsDao,
    private val compositionsDaoWrapper: CompositionsDaoWrapper,
    private val foldersDaoWrapper: FoldersDaoWrapper,
    private val artistsDao: ArtistsDao,
    private val artistsDaoWrapper: ArtistsDaoWrapper,
    private val albumsDao: AlbumsDao,
    private val genresDao: GenreDao
) {

    fun applyChanges(
        addedCompositions: List<StorageAudioFile>,
        restoredCompositions: List<Pair<DBComposition, StorageAudioFile>>,
        missedCompositions: List<DBComposition>,
        deletedCompositions: List<DBComposition>,
        changedCompositions: List<TimedChange<DBComposition, StorageAudioFile>>,
    ) {
        val previousCount = compositionsDao.getCompositionsCount()

        libraryDatabase.runInTransaction {
            applyCompositionChanges(
                addedCompositions,
                restoredCompositions,
                missedCompositions,
                deletedCompositions,
                changedCompositions
            )
        }

        if (previousCount == 0L) {
            //on first app launch room invalidation tracker can be not launched so call update manually
            compositionsDaoWrapper.launchManualUpdate()
        }
    }

    private fun applyCompositionChanges(
        compositionsToAdd: List<StorageAudioFile>,
        restoredCompositions: List<Pair<DBComposition, StorageAudioFile>>,
        missedCompositions: List<DBComposition>,
        deletedCompositions: List<DBComposition>,
        changedCompositions: List<TimedChange<DBComposition, StorageAudioFile>>,
    ) {
        insertCompositions(compositionsToAdd)
        for ((composition, storageComposition) in restoredCompositions) {
            val id = composition.id
            compositionsDao.updateStorageId(id, storageComposition.storageId)
            compositionsDao.setCompositionMissedTime(id, 0)
            compositionsDao.setLocalFileStatus(id, LocalFileStatus.AVAILABLE)
            compositionsDao.setCorruptionType(id, null)
        }
        val currentTime = System.currentTimeMillis()
        for (composition in missedCompositions) {
            val id = composition.id
            compositionsDao.updateStorageId(id, null)
            compositionsDao.setCompositionMissedTime(id, currentTime)
            compositionsDao.setCorruptionType(id, CorruptionType.NOT_FOUND)
            compositionsDao.setLocalFileStatus(id, LocalFileStatus.DISAPPEARED)
            compositionsDao.setCompositionLastFileScanTime(id, 0)
        }
        for (composition in deletedCompositions) {
            compositionsDao.delete(composition.id)
        }
        for (change in changedCompositions) {
            handleCompositionUpdate(change)
        }
        albumsDao.deleteEmptyAlbums()
        artistsDao.deleteEmptyArtists()
        foldersDaoWrapper.deleteEmptyFolders()
        genresDao.deleteEmptyGenres()
    }

    private fun insertCompositions(compositionsToAdd: List<StorageAudioFile>) {
        //optimization with cache, ~33% faster
        val artistsCache = HashMap<String, Long>()
        val foldersCache = HashMap<String, Long>()

        for (composition in compositionsToAdd) {
            val artist = composition.artist
            val artistId = artistsDaoWrapper.getOrInsertArtist(artist, artistsCache)

            val folderId = foldersDaoWrapper.getOrCreateFolder(
                composition.parentPath,
                foldersCache,
            )

            //if we have not found composition - just remove not_found mark
            val id = compositionsDao.findCompositionByFileName(composition.fileName, folderId)
            if (id != null) {
                val storageId = compositionsDao.selectStorageId(id)
                val actualStorageId = composition.storageId
                if (storageId != actualStorageId) {
                    compositionsDao.updateStorageId(id, actualStorageId)
                    if (storageId == null) {
                        val corruptionType = compositionsDao.selectCorruptionType(id)
                        if (corruptionType == CorruptionType.NOT_FOUND || corruptionType == CorruptionType.NOT_FOUND_IN_ALL_STORAGES) {
                            compositionsDao.setCorruptionType(id, null)
                        }
                    }
                    continue
                }
            }

            compositionsDao.insert(
                artistId = artistId,
                albumId = null,
                folderId = folderId,
                title = composition.title,
                trackNumber = null,
                discNumber = null,
                comment = null,
                lyrics = null,
                fileName = composition.fileName,
                duration = composition.duration,
                size = composition.size,
                storageId = composition.storageId,
                addedTime = composition.addedTime,
                modifiedTime = 0,
                storageModifyTime = composition.modifiedTime,
                pathModifyTime = null,
                lastScanTime = 0,
                coverModifyTime = 0,
                localFileStatus = LocalFileStatus.AVAILABLE,
                corruptionType = CompositionCorruptionDetector.getCorruptionType(composition),
                initialSource = InitialSource.LOCAL
            )
        }
    }

    private fun handleCompositionUpdate(change: TimedChange<DBComposition, StorageAudioFile>) {
        val dbComposition = change.oldData
        val storageComposition = change.newData

        val relativePath = storageComposition.parentPath
        var isPathChanged = false
        if (relativePath != dbComposition.parentPath) {
            val folderId = foldersDaoWrapper.getOrCreateFolder(
                relativePath,
                HashMap(),
            )
            compositionsDao.updateFolderId(dbComposition.id, folderId)
            isPathChanged = true
        }
        if (storageComposition.fileName != dbComposition.fileName) {
            isPathChanged = true
        }

        compositionsDao.update(
            storageComposition.fileName,
            storageComposition.duration,
            storageComposition.modifiedTime,
            storageComposition.storageId
        )

        if (isPathChanged) {
            compositionsDao.setPathModifyTime(dbComposition.id, change.time)
        }
    }

}