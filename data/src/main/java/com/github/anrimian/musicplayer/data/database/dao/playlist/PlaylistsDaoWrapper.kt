package com.github.anrimian.musicplayer.data.database.dao.playlist

import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlaylistEntryEntity
import com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArg
import com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs
import com.github.anrimian.musicplayer.data.models.exceptions.DuplicatePlaylistEntriesException
import com.github.anrimian.musicplayer.data.models.exceptions.PlaylistAlreadyExistsException
import com.github.anrimian.musicplayer.data.repositories.playlists.models.PlaylistEntryPosition
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlaylist
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylist
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylistItem
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import com.github.anrimian.musicplayer.domain.utils.rx.firstListItemOrComplete
import io.reactivex.rxjava3.core.Observable

class PlaylistsDaoWrapper(
    private val playListDao: PlaylistDao,
    private val compositionsDao: CompositionsDao,
    private val libraryDatabase: LibraryDatabase
) {

    fun insertStoragePlaylist(playList: StoragePlaylist, entries: List<StoragePlaylistItem>) {
        libraryDatabase.runInTransaction {
            val storageId = playList.storageId
            if (playListDao.isPlayListExistsByStorageId(storageId)) {
                return@runInTransaction
            }
            val id = playListDao.insertPlaylist(
                storageId,
                getUniquePlayListName(playList.name),
                playList.addedTime,
                playList.modifiedTime
            )
            insertPlayListItems(entries, id)
        }
    }

    fun insertPlaylist(
        name: String,
        addedTime: Long,
        modifiedTime: Long,
        compositionsIds: List<Long>
    ): Long {
        return libraryDatabase.runInTransaction<Long> {
            val playlistId = playListDao.insertPlaylist(null, name, addedTime, modifiedTime)
            insertPlayListEntries(playlistId, compositionsIds)
            playlistId
        }
    }

    fun setPlayListEntries(playlistId: Long, compositionsIds: List<Long>) {
        libraryDatabase.runInTransaction {
            if (!playListDao.isPlaylistExists(playlistId)) {
                return@runInTransaction
            }
            playListDao.clearPlayListEntries(playlistId)
            insertPlayListEntries(playlistId, compositionsIds)
        }
    }

    fun insertPlaylist(
        name: String,
        addedTime: Long,
        modifiedTime: Long,
        storagePlayListFetcher: () -> Long?
    ): Long {
        if (playListDao.isPlayListWithNameExists(name)) {
            throw PlaylistAlreadyExistsException()
        }
        val storageId = storagePlayListFetcher()
        val id = playListDao.insertPlaylist(storageId, name, addedTime, modifiedTime)
        check(id != -1L) { "db not modified" }
        return id
    }

    fun getAllAsStoragePlayLists(): List<AppPlaylist> {
        return playListDao.getAllAsStoragePlayLists()
    }

    fun getAllPlayLists(): List<AppPlaylist> {
        return playListDao.getAllPlayLists()
    }

    fun getPlayList(playlistId: Long): AppPlaylist? {
        return playListDao.getPlayList(playlistId)
    }

    fun deletePlayList(id: Long) {
        playListDao.deletePlaylist(id)
    }

    fun updatePlayListName(id: Long, name: String) {
        if (playListDao.isPlayListWithNameExists(name)) {
            throw PlaylistAlreadyExistsException()
        }
        playListDao.updatePlaylistName(id, name)
    }

    fun getPlayListsObservable(searchQuery: String?): Observable<List<Playlist>> {
        return playListDao.getPlayListsObservable(getSearchArg(searchQuery))
    }

    fun getPlayListsObservable(id: Long): Observable<Playlist> {
        return playListDao.getPlayListObservable(id)
            .firstListItemOrComplete()
    }

    fun getPlayListItemsObservable(
        playListId: Long,
        useFileName: Boolean,
        searchText: String?
    ): Observable<List<PlaylistEntry>> {
        val query = PlaylistDao.getPlaylistEntriesQuery(useFileName)
        val args = arrayOf<Any?>(playListId, *getSearchArgs(searchText, 3))
        val sqlQuery = SimpleSQLiteQuery(query, args)
        return playListDao.getPlayListItemsObservable(sqlQuery)
    }

    fun getPlayListsForCompositions(compositionIds: List<Long>): Set<Long> {
        val playlistsIds = HashSet<Long>()
        for (compositionId in compositionIds) {
            playlistsIds.addAll(playListDao.getPlaylistsForComposition(compositionId))
        }
        return playlistsIds
    }

    fun getCompositionIdsInPlaylist(playlistId: Long): List<Long> {
        return playListDao.getCompositionIdsInPlaylist(playlistId)
    }

    fun getCompositionsInPlaylist(
        playlistId: Long,
        useFileName: Boolean
    ): List<Composition> {
        val query = PlaylistDao.getCompositionsQuery(useFileName)
        val sqlQuery = SimpleSQLiteQuery(query, arrayOf<Any>(playlistId))
        return playListDao.getCompositionsInPlaylist(sqlQuery)
    }

    fun getPlayListItemsAsFileEntries(playListId: Long): List<com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry> {
        return playListDao.getPlayListItemsAsFileEntries(playListId)
    }

    fun getNextOrderPosition(playListId: Long) = playListDao.selectNextOrderPosition(playListId)

    fun deletePlayListEntry(id: Long, playListId: Long): Int {
        return libraryDatabase.runInTransaction<Int> {
            val position = playListDao.selectPositionById(id)
            playListDao.deletePlayListEntry(id)
            playListDao.decreasePositionsAfter(position, playListId)
            position
        }
    }

    //? change checkForDuplicates, ignoreDuplicates to enum
    // DuplicateCheckType: INSERT_ALL, INSERT_EXCLUDE_DUPLICATES, ABORT_ON_DUPLICATE
    fun addCompositions(
        compositions: List<CompositionModel>,
        playListId: Long,
        position: Int,
        checkForDuplicates: Boolean,
        ignoreDuplicates: Boolean
    ): List<CompositionModel> {
        var compositionsToInsert = compositions
        if (checkForDuplicates || ignoreDuplicates) {
            val duplicates = getCompositionsInPlaylist(playListId, compositions)
            if (duplicates.isNotEmpty()) {
                if (!ignoreDuplicates) {
                    val hasNonDuplicates = duplicates.size < compositions.size
                    throw DuplicatePlaylistEntriesException(duplicates, hasNonDuplicates)
                }
                val duplicatesSet = duplicates.toSet()
                compositionsToInsert = compositions.filterNot { duplicatesSet.contains(it) }
            }
        }
        addCompositions(compositionsToInsert, playListId, position)
        return compositionsToInsert
    }

    fun insertPlayListItems(
        items: List<StoragePlaylistItem>,
        playListId: Long,
        position: Int = playListDao.selectNextOrderPosition(playListId)
    ) {
        libraryDatabase.runInTransaction {
            playListDao.increasePositionsByCountAfter(items.size, position, playListId)
            val entities = ArrayList<PlaylistEntryEntity>(items.size)
            var orderPosition = position
            for (item in items) {
                val compositionId = compositionsDao.selectIdByStorageId(item.audioId)
                if (compositionId == 0L) {
                    continue
                }
                val entryEntity = PlaylistEntryEntity(
                    0,
                    item.itemId,
                    compositionId,
                    playListId,
                    orderPosition++
                )
                entities.add(entryEntity)
            }
            playListDao.insertPlayListEntries(entities)
        }
    }

    fun selectStorageId(id: Long): Long? {
        return playListDao.selectStorageId(id)
    }

    fun selectStorageItemId(id: Long): Long? {
        return playListDao.selectStorageItemId(id)
    }

    fun moveItems(playListId: Long, fromPos: Int, toPos: Int) {
        playListDao.moveItems(playListId, fromPos, toPos)
    }

    fun getEntryPositions(playlistId: Long): List<PlaylistEntryPosition> {
        return playListDao.getEntryPositions(playlistId)
    }

    fun updateEntryPosition(itemId: Long, position: Int) {
        playListDao.updateEntryPosition(itemId, position)
    }

    fun sortEntries(playlistId: Long, order: Order, useFileName: Boolean) {
        val sortOrder = if (order.isReversed) "DESC" else "ASC"
        val orderClause = when (order.orderType) {
            OrderType.NAME -> {
                if (useFileName) "compositions.fileName COLLATE NOCASE $sortOrder"
                else "CASE WHEN compositions.title IS NULL OR compositions.title = '' THEN compositions.fileName ELSE compositions.title END COLLATE NOCASE $sortOrder"
            }
            OrderType.FILE_NAME -> "compositions.fileName COLLATE NOCASE $sortOrder"
            OrderType.DURATION -> "compositions.duration $sortOrder"
            OrderType.ARTIST -> "(SELECT name FROM artists WHERE id = compositions.artistId) COLLATE NOCASE $sortOrder"
            else -> throw IllegalStateException("unknown order type: $order")
        }
        val query = SimpleSQLiteQuery("""
            SELECT play_lists_entries.itemId 
            FROM play_lists_entries 
            INNER JOIN compositions ON play_lists_entries.audioId = compositions.id
            WHERE play_lists_entries.playListId = ?
            ORDER BY $orderClause
        """, arrayOf(playlistId))
        val sortedIds = playListDao.getEntryIdsSorted(query)

        libraryDatabase.runInTransaction {
            sortedIds.forEachIndexed { index, itemId ->
                playListDao.updateEntryPosition(itemId, index)
            }
            playListDao.updatePlayListModifyTime(playlistId, System.currentTimeMillis())
        }
    }

    fun selectPlayListName(playListId: Long): String? {
        return playListDao.selectPlayListName(playListId)
    }

    fun findPlaylist(name: String): Long {
        return playListDao.findPlaylist(name)
    }

    fun getPlaylistSize(playListId: Long): Int {
        return playListDao.getPlaylistSize(playListId)
    }

    private fun insertPlayListEntries(playlistId: Long, compositionsIds: List<Long>) {
        compositionsIds.forEachIndexed { i, compositionId ->
            if (compositionsDao.isCompositionExists(compositionId)) {
                playListDao.insertPlayListEntry(null, compositionId, playlistId, i)
            }
        }
    }

    private fun addCompositions(
        compositions: List<CompositionModel>,
        playListId: Long,
        position: Int
    ) {
        libraryDatabase.runInTransaction {
            playListDao.increasePositionsByCountAfter(compositions.size, position, playListId)
            val entities = ArrayList<PlaylistEntryEntity>(compositions.size)
            var orderPosition = position
            for (item in compositions) {
                val entryEntity = PlaylistEntryEntity(
                    0,
                    null,
                    item.id,
                    playListId,
                    orderPosition++
                )
                entities.add(entryEntity)
            }
            playListDao.insertPlayListEntries(entities)
            playListDao.updatePlayListModifyTime(playListId, System.currentTimeMillis())
        }
    }

    private fun getCompositionsInPlaylist(
        playListId: Long,
        compositions: List<CompositionModel>
    ): List<CompositionModel> {
        val playlistCompositionsSet = playListDao.getCompositionIdsInPlaylist(playListId).toSet()
        return compositions.filter { playlistCompositionsSet.contains(it.id) }
    }

    private fun getUniquePlayListName(name: String, salt: String = ""): String {
        var uniqueName = name
        var i = 0
        while (playListDao.isPlayListWithNameExists(uniqueName)) {
            i++
            uniqueName = "$name($i)$salt"
        }
        return uniqueName
    }
}
