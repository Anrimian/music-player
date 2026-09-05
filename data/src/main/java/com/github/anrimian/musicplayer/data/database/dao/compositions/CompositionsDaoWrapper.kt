package com.github.anrimian.musicplayer.data.database.dao.compositions

import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao
import com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils
import com.github.anrimian.musicplayer.data.models.composition.ExternalComposition
import com.github.anrimian.musicplayer.data.models.exceptions.CompositionNotFoundException
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry
import com.github.anrimian.musicplayer.data.storage.providers.music.AudioFileKey
import com.github.anrimian.musicplayer.data.storage.providers.music.DBComposition
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.models.composition.AudioFileInfo
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus
import com.github.anrimian.musicplayer.domain.models.composition.tags.AudioFileTagInfo
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.models.search.CompositionLookup
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.domain.utils.getOrPut
import com.github.anrimian.musicplayer.domain.utils.rx.firstListItemOrComplete
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

class CompositionsDaoWrapper(
    private val libraryDatabase: LibraryDatabase,
    private val artistsDao: ArtistsDao,
    private val artistsDaoWrapper: ArtistsDaoWrapper,
    private val compositionsDao: CompositionsDao,
    private val albumsDao: AlbumsDao,
    private val genreDao: GenreDao,
    private val foldersDao: FoldersDao,
) {

    private val updateSubject = BehaviorSubject.createDefault(Constants.TRIGGER)

    fun getCompositionObservable(id: Long, useFileName: Boolean): Observable<Composition> {
        val query = CompositionsDao.getCompositionQuery(useFileName)
        query.append(" WHERE id = ? LIMIT 1")
        val sqlQuery = SimpleSQLiteQuery(
            query.toString(),
            arrayOf(id.toString())
        )
        return compositionsDao.getCompositionsObservable(sqlQuery)
            .firstListItemOrComplete()
    }

    fun getFullCompositionObservable(id: Long): Observable<FullComposition> {
        return compositionsDao.getFullCompositionObservable(id)
            .firstListItemOrComplete()
    }

    fun getLyricsObservable(compositionId: Long): Observable<String> {
        return compositionsDao.getLyricsObservable(compositionId)
    }

    fun getFullComposition(id: Long): FullComposition {
        return compositionsDao.getFullComposition(id)
    }

    fun getAudioFileInfo(id: Long): AudioFileInfo {
        return compositionsDao.getAudioFileInfo(id)
    }

    fun getAudioFilesInfo(ids: List<Long>): List<AudioFileInfo> {
        return ids.map(compositionsDao::getAudioFileInfo)
    }

    fun getCompositionsObservable(
        order: Order,
        useFileName: Boolean,
        searchText: String?,
    ): Observable<List<Composition>> {
        val query = CompositionsDao.getCompositionQuery(useFileName)
        query.append(CompositionsDao.getSearchWhereQuery(useFileName))
        query.append(getOrderQuery(order))
        val sqlQuery = SimpleSQLiteQuery(
            query.toString(),
            DatabaseUtils.getSearchArgs(searchText, 3)
        )
        return updateSubject.switchMap { compositionsDao.getCompositionsObservable(sqlQuery) }
    }

    fun getCompositionKeys(lookup: CompositionLookup): List<FileKey> {
        return compositionsDao.getCompositionKeys(
            lookup.minDuration,
            lookup.maxDuration,
            lookup.fileExtensions?.size ?: 0,
            lookup.fileExtensions
        ).map { key -> FileKey(key.name, key.parentPath) }
    }

    fun launchManualUpdate() {
        updateSubject.onNext(Constants.TRIGGER)
    }

    fun getCompositionsInFolderObservable(
        folderId: Long?,
        order: Order,
        useFileName: Boolean,
        searchText: String?,
    ): Observable<List<Composition>> {
        val query = CompositionsDao.getCompositionQuery(useFileName)
        query.append(CompositionsDao.getSearchWhereQuery(useFileName))
        query.append(" AND (? IS NOT NULL OR ")
        query.append("(folderId = ")
        query.append(folderId)
        query.append(" OR (folderId IS NULL AND ")
        query.append(folderId)
        query.append(" IS NULL)))")
        query.append(getOrderQuery(order))
        val sqlQuery = SimpleSQLiteQuery(
            query.toString(),
            DatabaseUtils.getSearchArgs(searchText, 4)
        )
        return compositionsDao.getCompositionsInFolderObservable(sqlQuery)
    }

    fun getAllCompositionsInFolder(
        parentFolderId: Long?,
        useFileName: Boolean,
    ): List<Composition> {
        var query = FoldersDao.getRecursiveFolderQuery(parentFolderId)
        query += CompositionsDao.getCompositionQuery(useFileName)
        query += " WHERE folderId IN (SELECT childFolderId FROM allChildFolders) "
        query += "OR folderId = "
        query += parentFolderId
        val sqlQuery = SimpleSQLiteQuery(query)
        return compositionsDao.executeQuery(sqlQuery)
    }

    fun getAllFilesInFolder(parentFolderId: Long?): List<AudioFileInfo> {
        var query = FoldersDao.getRecursiveFolderQuery(parentFolderId)
        query += CompositionsDao.getAudioFileInfoQuery()
        query += " WHERE folderId IN (SELECT childFolderId FROM allChildFolders) "
        query += "OR folderId = "
        query += parentFolderId
        val sqlQuery = SimpleSQLiteQuery(query)
        return compositionsDao.getAudioFilesInfo(sqlQuery)
    }

    fun getCompositionsInFolder(
        parentFolderId: Long?,
        order: Order,
        useFileName: Boolean,
    ): List<Composition> {
        val query = CompositionsDao.getCompositionQuery(useFileName)
        query.append(" WHERE folderId = ")
        query.append(parentFolderId)
        query.append(" OR (folderId IS NULL AND ")
        query.append(parentFolderId)
        query.append(" IS NULL)")
        query.append(getOrderQuery(order))
        val sqlQuery = SimpleSQLiteQuery(query.toString())
        return compositionsDao.executeQuery(sqlQuery)
    }

    fun selectAllAsStorageCompositions(): Map<AudioFileKey, DBComposition> {
        val result = HashMap<AudioFileKey, DBComposition>()
        val pageSize = 1000
        var index = 0
        var pageResult: Map<AudioFileKey, DBComposition>
        do {
            pageResult = compositionsDao.selectAsDbCompositions(pageSize, index)
                .associateBy { item -> AudioFileKey(item.parentPath, item.fileName) }
            result.putAll(pageResult)
            index++
        } while (pageResult.size == pageSize)
        return result
    }

    fun requireStorageId(compositionId: Long): Long {
        val storageId = compositionsDao.getStorageId(compositionId)
            ?: throw CompositionNotFoundException("composition not found")
        return storageId
    }

    fun getStorageId(compositionId: Long): Long? {
        return compositionsDao.getStorageId(compositionId)
    }

    fun selectStorageId(compositionId: Long): Maybe<Long> {
        return Maybe.fromCallable { compositionsDao.getStorageId(compositionId) }
    }

    fun selectStorageIds(ids: List<Long>): Map<Long, Long> {
        if (ids.isEmpty()) {
            return emptyMap()
        }
        val result = HashMap<Long, Long>(ids.size)
        ids.chunked(500).forEach { chunk ->
            result.putAll(compositionsDao.selectStorageIds(chunk))
        }
        return result
    }

    fun delete(id: Long) {
        libraryDatabase.runInTransaction {
            compositionsDao.delete(id)
            albumsDao.deleteEmptyAlbums()
            artistsDao.deleteEmptyArtists()
            genreDao.deleteEmptyGenres()
            foldersDao.deleteFoldersWithoutContainment()
        }
    }

    fun deleteAll(ids: Array<Long>) {
        libraryDatabase.runInTransaction {
            ids.asIterable().chunked(500).forEach { chunk ->
                compositionsDao.delete(chunk.toTypedArray())
            }
            albumsDao.deleteEmptyAlbums()
            artistsDao.deleteEmptyArtists()
            genreDao.deleteEmptyGenres()
            foldersDao.deleteFoldersWithoutContainment()
        }
    }

    fun deleteCompositionsWithLocalFileStatus(vararg statuses: LocalFileStatus) {
        compositionsDao.deleteCompositionsWithLocalFileStatus(*statuses)
    }

    fun clearAllPathModifyTime() {
        compositionsDao.clearAllPathModifyTime()
    }

    fun clearPathModifyTime(keys: Iterable<FileKey>) {
        libraryDatabase.runInTransaction {
            for (key in keys) {
                val id = findCompositionIdByFilePath(key.parentPath, key.name) ?: continue
                compositionsDao.clearPathModifyTime(id)
            }
        }
    }

    fun getPathModifyTime(key: FileKey): Long? {
        val id = findCompositionIdByFilePath(key.parentPath, key.name) ?: return null
        return compositionsDao.getPathModifyTime(id)
    }

    fun updateFolderId(id: Long, folderId: Long?) {
        compositionsDao.updateFolderId(id, folderId)
    }

    fun replaceFolderId(fromFolderId: Long, folderId: Long?) {
        compositionsDao.replaceFolderId(fromFolderId, folderId)
    }

    fun updateStorageId(id: Long, storageId: Long?) {
        compositionsDao.updateStorageId(id, storageId)
    }

    fun updateAlbum(compositionId: Long, albumName: String?) {
        libraryDatabase.runInTransaction {
            val albumId = if (albumName.isNullOrBlank()) {
                null
            } else {
                var artistId: Long? = null
                val existsAlbumId = compositionsDao.getAlbumId(compositionId)
                if (existsAlbumId != null) {
                    artistId = albumsDao.getArtistId(existsAlbumId)
                }
                if (artistId == null) {
                    artistId = compositionsDao.getArtistId(compositionId)
                }
                var targetAlbumId = albumsDao.findAlbum(artistId, albumName)
                if (targetAlbumId == null && artistId == null) {
                    targetAlbumId = albumsDao.findAlbum(null, albumName)
                }
                targetAlbumId ?: albumsDao.insertAlbum(artistId, albumName)
            }
            val oldAlbumId = compositionsDao.getAlbumId(compositionId)
            if (oldAlbumId != albumId) {
                compositionsDao.updateAlbum(compositionId, albumId)
                if (oldAlbumId != null) {
                    albumsDao.deleteEmptyAlbum(oldAlbumId)
                }
            }
        }
    }

    fun updateArtist(id: Long, authorName: String?) {
        libraryDatabase.runInTransaction {
            val artistId = artistsDaoWrapper.getOrCreateArtist(authorName)

            val oldArtistId = compositionsDao.getArtistId(id)
            compositionsDao.updateArtist(id, artistId)

            // if OLD artist exists and has no references - delete him
            if (oldArtistId != null) {
                artistsDao.deleteEmptyArtist(oldArtistId)
            }
        }
    }

    fun updateAlbumArtist(id: Long, artistName: String?) {
        libraryDatabase.runInTransaction {
            //find album
            val albumId = compositionsDao.getAlbumId(id) ?: return@runInTransaction

            val artistId = artistsDaoWrapper.getOrCreateArtist(artistName)

            val albumEntity = albumsDao.getAlbumEntity(albumId)
            val oldArtistId = albumEntity.artistId

            //find new album with author id and name
            var newAlbumId = albumsDao.findAlbum(artistId, albumEntity.name)

            //if not exists, create
            if (newAlbumId == null) {
                newAlbumId = albumsDao.insertAlbum(artistId, albumEntity.name)
            }
            //set new album to composition
            compositionsDao.setAlbumId(id, newAlbumId)

            //if album is empty, delete
            albumsDao.deleteEmptyAlbum(albumId)

            // 4) if OLD artist exists and has no references - delete him
            if (oldArtistId != null) {
                artistsDao.deleteEmptyArtist(oldArtistId)
            }
        }
    }

    fun setCompositionGenres(compositionId: Long, genres: Array<String>) {
        genreDao.removeCompositionGenres(compositionId)
        for (genre in genres) {
            var genreId = genreDao.findGenre(genre)
            if (genreId == null) {
                genreId = genreDao.insertGenre(genre)
            }
            genreDao.insertGenreEntry(compositionId, genreId)
        }
    }

    fun updateTitle(id: Long, title: String?) {
        libraryDatabase.runInTransaction {
            compositionsDao.updateTitle(id, title)
            compositionsDao.setModifyTime(id, System.currentTimeMillis())
        }
    }

    fun updateDuration(id: Long, duration: Long) {
        libraryDatabase.runInTransaction {
            compositionsDao.updateDuration(id, duration)
            compositionsDao.setModifyTime(id, System.currentTimeMillis())
        }
    }

    fun updateTrackNumber(id: Long, trackNumber: Long?) {
        libraryDatabase.runInTransaction {
            compositionsDao.updateTrackNumber(id, trackNumber)
            compositionsDao.setModifyTime(id, System.currentTimeMillis())
        }
    }

    fun updateDiscNumber(id: Long, discNumber: Long?) {
        libraryDatabase.runInTransaction {
            compositionsDao.updateDiscNumber(id, discNumber)
            compositionsDao.setModifyTime(id, System.currentTimeMillis())
        }
    }

    fun updateComment(id: Long, text: String?) {
        libraryDatabase.runInTransaction {
            compositionsDao.updateComment(id, text)
            compositionsDao.setModifyTime(id, System.currentTimeMillis())
        }
    }

    fun updateLyrics(id: Long, text: String?) {
        libraryDatabase.runInTransaction {
            compositionsDao.updateLyrics(id, text)
            compositionsDao.setModifyTime(id, System.currentTimeMillis())
        }
    }

    fun updateFileSize(id: Long, fileSize: Long) {
        libraryDatabase.runInTransaction {
            compositionsDao.updateFileSize(id, fileSize)
            compositionsDao.setModifyTime(id, System.currentTimeMillis())
        }
    }

    fun setModifyTimeToCurrent(id: Long) {
        compositionsDao.setModifyTime(id, System.currentTimeMillis())
    }

    fun setCompositionPathModifyTime(id: Long, time: Long?) {
        compositionsDao.setPathModifyTime(id, time)
    }

    fun updateCoverModifyTimeAndSize(id: Long, size: Long, time: Long) {
        compositionsDao.setCoverModifyTimeAndSize(id, size, time)
    }

    fun updateCoverModifyTime(id: Long, time: Long) {
        compositionsDao.setCoverModifyTime(id, time)
    }

    fun updateCompositionFileName(id: Long, fileName: String) {
        compositionsDao.updateCompositionFileName(id, fileName)
    }

    fun setLocalFileStatus(id: Long, status: LocalFileStatus) {
        compositionsDao.setLocalFileStatus(id, status)
    }

    fun setCorruptionType(id: Long, corruptionType: CorruptionType?) {
        compositionsDao.setCorruptionType(id, corruptionType)
    }

    fun writeErrorAboutComposition(
        composition: Composition,
        corruptionType: CorruptionType?,
    ) {
        if (composition.corruptionType == corruptionType) {
            return
        }
        libraryDatabase.runInTransaction {
            val id = composition.id
            compositionsDao.setCorruptionType(id, corruptionType)
            if (corruptionType == CorruptionType.NOT_FOUND
                || corruptionType == CorruptionType.NOT_FOUND_IN_ALL_STORAGES
            ) {
                compositionsDao.updateStorageId(id, null)
                compositionsDao.setCompositionLastFileScanTime(id, 0)
                if (composition.fileStatus == LocalFileStatus.AVAILABLE) {
                    compositionsDao.setLocalFileStatus(id, LocalFileStatus.DISAPPEARED)
                    compositionsDao.setCompositionMissedTime(id, System.currentTimeMillis())
                }
            }
        }
    }

    fun updateCompositionFileState(
        composition: FullComposition,
        storageId: Long,
        fileModifyTime: Long?,
        coverModifyTime: Long,
    ) {
        libraryDatabase.runInTransaction {
            val id = composition.id
            compositionsDao.updateStorageId(id, storageId)
            compositionsDao.setCompositionMissedTime(id, 0)
            compositionsDao.setLocalFileStatus(id, LocalFileStatus.AVAILABLE)
            if (composition.corruptionType == CorruptionType.NOT_FOUND) {
                compositionsDao.setCorruptionType(id, null)
            }
            if (fileModifyTime != null) {
                compositionsDao.setModifyTime(id, fileModifyTime)
            }
            if (coverModifyTime != 0L && coverModifyTime != composition.coverModifyTime) {
                compositionsDao.setCoverModifyTime(id, coverModifyTime)
            }
        }
    }

    fun setCompositionMissedTime(id: Long, time: Long) {
        compositionsDao.setCompositionMissedTime(id, time)
    }

    fun getMissingCompositionsCountObservable(): Observable<Int> {
        return compositionsDao.getMissingCompositionsCountObservable()
    }

    fun getMissingAudioFilesObservable(): Observable<List<AudioFileInfo>> {
        val sb = CompositionsDao.getAudioFileInfoQuery()
        sb.append(" WHERE missingTime > 0")
        val sqlQuery = SimpleSQLiteQuery(sb.toString())
        return compositionsDao.getAudioFilesInfoObservable(sqlQuery)
    }

    fun deleteMissingCompositions() {
        libraryDatabase.runInTransaction {
            compositionsDao.deleteMissingCompositions()
            foldersDao.deleteFoldersWithoutContainment()
        }
    }

    fun selectNextCompositionsToScan(
        lastCompleteScanTime: Long,
        filesCount: Int,
    ): Single<List<FullComposition>> {
        return compositionsDao.selectNextCompositionsToScan(lastCompleteScanTime, filesCount)
    }

    fun setCompositionLastFileScanTime(composition: FullComposition, time: Long) {
        compositionsDao.setCompositionLastFileScanTime(composition.id, time)
    }

    fun cleanLastFileScanTime() {
        compositionsDao.cleanLastFileScanTime()
    }

    fun updateCompositionsByFileInfo(
        scannedCompositions: List<Pair<FullComposition, AudioFileTagInfo>>,
        allCompositions: List<FullComposition>,
        updateModifyTime: Boolean,
    ) {
        libraryDatabase.runInTransaction {
            for ((composition, tagInfo) in scannedCompositions) {
                val wasChanges = updateCompositionByFileInfo(composition, tagInfo)
                val id = composition.id
                // handle case when file was modified by another app
                if (wasChanges && updateModifyTime && compositionsDao.getCompositionLastFileScanTime(id) != 0L) {
                    compositionsDao.setModifyTime(id, compositionsDao.getCompositionStorageModifyTime(id))
                }
            }
            val currentTime = System.currentTimeMillis()
            for (composition in allCompositions) {
                setCompositionLastFileScanTime(composition, currentTime)
            }
        }
    }

    fun updateCompositionByFileInfo(
        composition: FullComposition,
        fileInfo: AudioFileTagInfo,
    ): Boolean {
        return libraryDatabase.runInTransaction<Boolean> {
            val id = composition.id

            // file was read outside of this transaction, so composition can be deleted meanwhile
            if (!compositionsDao.exists(id)) {
                return@runInTransaction false
            }

            val tags = fileInfo.audioTags

            var wasChanges = false

            val tagTitle = tags.title
            if (!TextUtils.isEmpty(tagTitle) && composition.title != tagTitle) {
                compositionsDao.updateTitle(id, tagTitle)
                wasChanges = true
            }

            val tagArtist = tags.artist
            if (!TextUtils.isEmpty(tagArtist) && composition.artist != tagArtist) {
                updateArtist(id, tagArtist)
                wasChanges = true
            }

            val tagAlbum = tags.album
            if (!TextUtils.isEmpty(tagAlbum) && composition.album != tagAlbum) {
                updateAlbum(id, tagAlbum)
                wasChanges = true
            }

            val tagAlbumArtist = tags.albumArtist
            if (!TextUtils.isEmpty(tagAlbumArtist) && composition.albumArtist != tagAlbumArtist) {
                updateAlbumArtist(id, tagAlbumArtist)
                wasChanges = true
            }

            //if we just update duration, we'll lose milliseconds part. So just update 0 values
            val tagDuration = tags.durationSeconds
            val duration = composition.duration
            if (duration == 0L && tagDuration != 0) {
                val tagDurationMillis = tagDuration * 1000L
                compositionsDao.updateDuration(id, tagDurationMillis)
                if (compositionsDao.selectCorruptionType(id) == CorruptionType.UNKNOWN) {
                    compositionsDao.setCorruptionType(id, null)
                }
                wasChanges = true
            }

            val tagTrackNumber = tags.trackNumber
            if (composition.trackNumber != tagTrackNumber) {
                compositionsDao.updateTrackNumber(id, tagTrackNumber)
                wasChanges = true
            }

            val tagDiscNumber = tags.discNumber
            if (composition.discNumber != tagDiscNumber) {
                compositionsDao.updateDiscNumber(id, tagDiscNumber)
                wasChanges = true
            }

            val tagComment = tags.comment
            if (!TextUtils.isEmpty(tagComment) && composition.comment != tagComment) {
                compositionsDao.updateComment(id, tagComment)
                wasChanges = true
            }

            val tagLyrics = tags.lyrics
            if (!TextUtils.isEmpty(tagLyrics) && composition.lyrics != tagLyrics) {
                compositionsDao.updateLyrics(id, tagLyrics)
                wasChanges = true
            }
            val tagGenres = tags.genres
            val compositionGenres = CompositionHelper.splitGenres(composition.genres)
            if (!compositionGenres.contentEquals(tagGenres)) {
                setCompositionGenres(id, tagGenres)
                wasChanges = true
            }

            val fileSize = fileInfo.fileSize
            if (composition.size != fileSize) {
                compositionsDao.updateFileSize(id, fileSize)
                wasChanges = true
            }
            return@runInTransaction wasChanges
        }
    }

    fun getFolderId(compositionId: Long): Long? {
        return compositionsDao.getFolderId(compositionId)
    }

    fun getAllAsExternalCompositions(parentPath: String?): List<ExternalComposition> {
        val folderId = if (parentPath.isNullOrEmpty()) {
            null
        } else {
            findFolderId(parentPath) ?: return emptyList()
        }
        return compositionsDao.getAllAsExternalCompositions(folderId)
    }

    fun findCompositionIdByFilePath(parentPath: String?, fileName: String): Long? {
        val folderId = findFolderId(parentPath)
        return compositionsDao.findCompositionByFileName(fileName, folderId)
    }

    fun requireCompositionIdByFilePath(parentPath: String?, fileName: String): Long {
        val id = findCompositionIdByFilePath(parentPath, fileName)
            ?: throw CompositionNotFoundException("$fileName not found")
        return id
    }

    fun getCompositionNameAndPath(id: Long): FileKey {
        val fileName = compositionsDao.getCompositionFileName(id)
            ?: throw CompositionNotFoundException("composition not found")
        val parentPath = compositionsDao.getCompositionParentPath(id)
        return FileKey(fileName, parentPath)
    }

    fun getCompositionSize(id: Long): Long {
        return compositionsDao.getCompositionSize(id)
    }

    fun updateCompositionIdsInitialSource(
        compositionsIds: List<Long>,
        initialSource: InitialSource,
        updateFrom: InitialSource,
    ) {
        libraryDatabase.runInTransaction {
            for (id in compositionsIds) {
                updateCompositionInitialSource(id, initialSource, updateFrom)
            }
        }
    }

    fun updateCompositionInitialSource(
        compositionId: Long,
        initialSource: InitialSource,
        updateFrom: InitialSource,
    ) {
        compositionsDao.updateCompositionInitialSource(compositionId, initialSource, updateFrom)
    }

    fun selectDeletedComposition(
        ids: Array<Long>,
        useFileName: Boolean,
    ): List<DeletedComposition> {
        val query = CompositionsDao.getDeletedCompositionQuery(useFileName, ids.size).toString()
        val sqlQuery = SimpleSQLiteQuery(query, ids)
        return compositionsDao.selectDeletedComposition(sqlQuery)
    }

    fun selectDeletedComposition(compositionId: Long, useFileName: Boolean): DeletedComposition {
        return selectDeletedComposition(arrayOf(compositionId), useFileName)[0]
    }

    fun getCompositionIds(
        fileEntries: List<PlayListEntry>,
        pathIdMapCache: HashMap<String, Long>,
    ): List<Long> {
        return fileEntries.mapNotNull { entry ->
            val path = entry.filePath
            return@mapNotNull pathIdMapCache.getOrPut(path) {
                val parentPath = FileUtils.getParentDirPath(path)
                val fileName = FileUtils.getFileName(path)
                val nameIds = compositionsDao.findCompositionsByFileName(fileName)
                for (nameId in nameIds) {
                    val dbPath = compositionsDao.getCompositionParentPath(nameId)
                    if (dbPath.endsWith(parentPath)) {
                        return@getOrPut nameId
                    }
                }
                return@getOrPut null
            }
        }
    }

    fun getCompositionsInFolder(relativePath: String?): List<FileKey> {
        val folderId = findFolderId(relativePath) ?: return emptyList()
        return getCompositionsInFolder(folderId)
    }

    fun getCompositionsInFolder(folderId: Long?): List<FileKey> {
        val compositions = getAllFilesInFolder(folderId)
        return compositions.map { c -> FileKey(c.fileName, c.parentPath) }
    }

    fun findFolderId(filePath: String?): Long? {
        if (filePath.isNullOrEmpty()) {
            return null
        }

        val volume = foldersDao.findVolumeByPath(filePath) ?: return null
        val rootFolderId = volume.rootFolderId
        val relativePath = filePath.removePrefix(volume.path).removePrefix("/")
        if (relativePath.isEmpty()) {
            return rootFolderId
        }
        return findFolderId(relativePath, rootFolderId)
    }

    private fun findFolderId(relativePath: String?, parentId: Long?): Long? {
        if (relativePath.isNullOrEmpty()) {
            return parentId
        }

        val folderId: Long?
        val delimiterIndex = relativePath.indexOf('/')
        if (delimiterIndex == -1) {
            folderId = foldersDao.getFolderByName(relativePath, parentId)
        } else {
            val folderName = relativePath.substring(0, delimiterIndex)
            val parentFolderId = foldersDao.getFolderByName(folderName, parentId) ?: return null
            val folderPath = relativePath.substring(delimiterIndex + 1)
            folderId = findFolderId(folderPath, parentFolderId)
        }
        return folderId
    }

    private fun getOrderQuery(order: Order): String {
        val column = when (order.orderType) {
            OrderType.NAME -> "CASE WHEN title IS NULL OR title = '' THEN fileName ELSE title END"
            OrderType.FILE_NAME -> "fileName"
            OrderType.ADD_TIME -> "addedTime"
            OrderType.SIZE -> "size"
            OrderType.DURATION -> "duration"
            OrderType.ARTIST -> "artist"
            else -> throw IllegalStateException("unknown order type: $order")
        }
        val direction = if (order.isReversed) "DESC" else "ASC"
        return " ORDER BY $column $direction"
    }

}
