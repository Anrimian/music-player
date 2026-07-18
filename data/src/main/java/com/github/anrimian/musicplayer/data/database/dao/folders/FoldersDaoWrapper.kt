package com.github.anrimian.musicplayer.data.database.dao.folders

import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils
import com.github.anrimian.musicplayer.data.storage.providers.FileVolume
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.folders.AbstractDirectory
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderInfo
import com.github.anrimian.musicplayer.domain.models.folders.Volume
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.utils.rx.firstListItemOrComplete
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class FoldersDaoWrapper(
    private val libraryDatabase: LibraryDatabase,
    private val foldersDao: FoldersDao,
    private val compositionsDao: CompositionsDaoWrapper,
) {

    fun getFilesObservable(
        parentFolderId: Long?,
        order: Order,
        useFileName: Boolean,
        searchText: String?,
    ): Observable<List<FileSource>> {
        val folderObservable = getFoldersObservable(parentFolderId, order, searchText)

        val compositionsObservable = compositionsDao.getCompositionsInFolderObservable(
            parentFolderId,
            order,
            useFileName,
            searchText
        ).map { compositions -> compositions.map(::CompositionFileSource) }

        return Observable.combineLatest(
            folderObservable,
            compositionsObservable
        ) { folders, compositions -> folders + compositions }
    }

    fun getFolderObservable(folderId: Long): Observable<FolderInfo> {
        return foldersDao.getFolderObservable(folderId)
            .firstListItemOrComplete()
    }

    fun getVolumes(): Observable<List<Volume>> {
        return foldersDao.getVolumes()
    }

    fun extractAllCompositionsFromFiles(fileSources: Iterable<FileSource>): Single<List<Long>> {
        return Observable.fromIterable(fileSources)
            .flatMap(::fileSourceToCompositionId)
            .toList()
    }

    fun extractAllCompositionsFromFiles(
        fileSources: Iterable<FileSource>,
        order: Order,
        useFileName: Boolean,
    ): Single<List<Composition>> {
        return Observable.fromIterable(fileSources)
            .flatMap { fileSource -> fileSourceToComposition(fileSource, order, useFileName) }
            .toList()
    }

    fun getAllCompositionsInFolder(
        parentFolderId: Long?,
        order: Order,
        useFileName: Boolean,
    ): List<Composition> {
        return buildList {
            val query = """
                ${FoldersDao.getRecursiveFolderQuery(parentFolderId)}
                SELECT id 
                FROM folders 
                WHERE parentId = $parentFolderId OR (parentId IS NULL AND $parentFolderId IS NULL)
                ${getOrderQuery(order)}
            """
            val sqlQuery = SimpleSQLiteQuery(query)
            val folders = foldersDao.getFoldersIds(sqlQuery)
            for (id in folders) {
                addAll(getAllCompositionsInFolder(id, order, useFileName))
            }
            addAll(compositionsDao.getCompositionsInFolder(parentFolderId, order, useFileName))
        }
    }

    fun deleteFolder(folderId: Long, childCompositions: Array<Long>) {
        libraryDatabase.runInTransaction {
            compositionsDao.deleteAll(childCompositions)
            foldersDao.deleteFolder(folderId)
        }
    }

    fun deleteFolders(folders: List<Long>, childCompositions: Array<Long>) {
        libraryDatabase.runInTransaction {
            compositionsDao.deleteAll(childCompositions)
            foldersDao.deleteFolders(folders)
        }
    }

    fun changeFolderName(folderId: Long, newName: String) {
        libraryDatabase.runInTransaction {
            val parentId = foldersDao.getFolderParentId(folderId)
            val existingFolderId = foldersDao.getFolderByName(newName, parentId)
            foldersDao.changeFolderName(folderId, newName)
            if (existingFolderId != null) {
                //move all references out from existing folder and delete it
                foldersDao.replaceParentId(existingFolderId, folderId)
                compositionsDao.replaceFolderId(existingFolderId, folderId)
                foldersDao.deleteFolder(existingFolderId)
            }
        }
    }

    fun getFullFolderPath(folderId: Long): String {
        return foldersDao.getFullFolderPath(folderId)
    }

    fun getFullFolderPath(folderId: Long?): String {
        return if (folderId == null) "" else foldersDao.getFullFolderPath(folderId)
    }

    fun getAllParentFoldersId(currentFolder: Long?): List<Long> {
        return buildList {
            if (currentFolder != null) {
                addAll(foldersDao.getAllParentFoldersId(currentFolder))
            }
        }
    }

    fun updateFolderId(files: Collection<FileSource>, toFolderId: Long?) {
        libraryDatabase.runInTransaction {
            for (fileSource in files) {
                when (fileSource) {
                    is CompositionFileSource -> {
                        val id = fileSource.composition.id
                        compositionsDao.updateFolderId(id, toFolderId)
                    }
                    is AbstractDirectory -> {
                        val id = fileSource.getFolderId()
                        foldersDao.updateParentId(id, toFolderId)
                    }
                }
            }
            deleteEmptyFolders()
        }
    }

    fun moveCompositionsIntoFolder(
        parentFolderId: Long?,
        directoryName: String,
        files: Collection<FileSource>,
    ): Long {
        return libraryDatabase.runInTransaction<Long> {
            val folderId = foldersDao.getFolderByName(directoryName, parentFolderId)
                ?: createFolder(parentFolderId, directoryName)
            updateFolderId(files, folderId)
            folderId
        }
    }

    fun createFolder(parentId: Long?, name: String): Long {
        return foldersDao.insertFolder(name, parentId, null)
    }

    fun deleteEmptyFolders() {
        do {
            val deletedRows = foldersDao.deleteFoldersWithoutContainment()
        } while (deletedRows != 0)

        foldersDao.deleteOrphanedVolumes()
    }

    fun getOrCreateFolder(
        filePath: String,
        folderCache: MutableMap<String, Long>,
    ): Long? {
        val volume = FileVolume.fromCanonicalPath(filePath)
        val rootFolderId = getRootFolderForVolume(volume, folderCache)
        val relativePath = if (filePath.length > volume.path.length) {
            filePath.substring(volume.path.length).trimStart('/')
        } else {
            ""
        }
        return getOrCreateFolder(filePath, relativePath, rootFolderId, folderCache)
    }

    private fun getRootFolderForVolume(
        volume: FileVolume,
        folderCache: MutableMap<String, Long>
    ): Long {
        var folderId = folderCache[volume.path]
        if (folderId != null) {
            return folderId
        }

        var volumeId = foldersDao.getVolumeByPath(volume.path)
        if (volumeId == null) {
            volumeId = foldersDao.insertVolume(volume.storageKey, volume.path, volume.isPrimary)
        }
        folderId = getOrCreateFolderId(volume.storageKey, null, volumeId)
        folderCache[volume.path] = folderId
        return folderId
    }

    private fun getOrCreateFolder(
        fullPath: String,
        filePath: String,
        parentId: Long?,
        folderCache: MutableMap<String, Long>,
    ): Long? {
        if (filePath.isEmpty()) {
            return parentId
        }

        val cachedId = folderCache[fullPath]
        if (cachedId != null) {
            return cachedId
        }

        val delimiterIndex = filePath.indexOf('/')
        val folderId = if (delimiterIndex == -1) {
            getOrCreateFolderId(filePath, parentId, null)
        } else {
            val folderName = filePath.substring(0, delimiterIndex)
            val parentFolderId = getOrCreateFolderId(folderName, parentId, null)
            val folderPath = filePath.substring(delimiterIndex + 1)
            getOrCreateFolder(fullPath, folderPath, parentFolderId, folderCache)
        }
        if (folderId != null) {
            folderCache[fullPath] = folderId
        }
        return folderId
    }

    private fun getOrCreateFolderId(folderName: String, parentId: Long?, volumeId: Long?): Long {
        return foldersDao.getFolderByName(folderName, parentId, volumeId)
            ?: foldersDao.insertFolder(folderName, parentId, volumeId)
    }

    private fun getFoldersObservable(
        parentFolderId: Long?,
        order: Order,
        searchText: String?,
    ): Observable<List<FolderFileSource>> {
        val selectAllFolders = !searchText.isNullOrEmpty()
        val query = """
            ${FoldersDao.getRecursiveFolderQuery(parentFolderId, selectAllFolders)}, 
            childCompositions(storageId, initialSource) AS (SELECT storageId, initialSource FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))
            SELECT id, name, 
            (SELECT count() FROM childCompositions) as filesCount, 
            (SELECT exists(SELECT 1 FROM childCompositions WHERE storageId IS NOT NULL AND initialSource = 1 LIMIT 1)) as hasAnyStorageFile 
            FROM folders 
            WHERE (? IS NULL AND (parentId = $parentFolderId OR (parentId IS NULL AND $parentFolderId IS NULL))) 
            $SEARCH_QUERY_TEMPLATE 
            ${getOrderQuery(order)}
        """
        val sqlQuery = SimpleSQLiteQuery(query, DatabaseUtils.getSearchArgs(searchText, 3))
        return foldersDao.getFoldersObservable(sqlQuery)
    }

    private fun getOrderQuery(order: Order): String {
        val column = when (order.orderType) {
            OrderType.NAME, OrderType.FILE_NAME -> "name"
            OrderType.ADD_TIME -> "(SELECT max(addedTime) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))"
            OrderType.DURATION -> "(SELECT sum(duration) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))"
            OrderType.SIZE -> "(SELECT sum(size) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))"
            else -> throw IllegalStateException("unknown order type$order")
        }
        val direction = if (order.isReversed) "DESC" else "ASC"
        return "ORDER BY $column $direction"
    }

    private fun fileSourceToComposition(
        fileSource: FileSource,
        order: Order,
        useFileName: Boolean,
    ): Observable<Composition> {
        return when (fileSource) {
            is CompositionFileSource -> Observable.just(fileSource.composition)
            is AbstractDirectory -> Observable.fromIterable(
                getAllCompositionsInFolder(fileSource.getFolderId(), order, useFileName)
            )
        }
    }

    private fun fileSourceToCompositionId(fileSource: FileSource): Observable<Long> {
        return when (fileSource) {
            is CompositionFileSource -> Observable.just(fileSource.composition.id)
            is AbstractDirectory -> Observable.fromIterable(
                compositionsDao.getAllFilesInFolder(fileSource.getFolderId())
            ).map { composition -> composition.id }
        }
    }

    companion object {
        private const val SEARCH_QUERY_TEMPLATE = " OR (? IS NOT NULL AND name LIKE ?)"
    }

}