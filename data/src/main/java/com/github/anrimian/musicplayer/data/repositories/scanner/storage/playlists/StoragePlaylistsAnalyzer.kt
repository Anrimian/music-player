package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists

import androidx.core.util.Pair
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.playlist.PlaylistsDaoWrapper
import com.github.anrimian.musicplayer.data.models.exceptions.PlaylistAlreadyExistsException
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListFile
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlaylist
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylist
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylistsProvider
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.domain.utils.mergeMaps


/**
 * If there will be new playlists from media storage -> they will be copied to db
 * Then:
 * If there will be new playlists from db -> they will be copied to file cache
 * If there will be new playlists from file cache -> try will be copied to db
 * If playlist modify date or item count is less than in cache -> db items will be rewritten from cache
 */
class StoragePlaylistsAnalyzer(
    private val compositionsDao: CompositionsDaoWrapper,
    private val playListsDao: PlaylistsDaoWrapper,
    private val playlistsStorageProvider: StoragePlaylistsProvider,
    private val playlistsFilesStorage: PlaylistFilesStorage
) {

    @Synchronized
    fun applyPlayListsData(storagePlayLists: Map<String, StoragePlaylist>) {
        //compare media storage and db
        val newDbPlaylistsFromStorage = ArrayList<StoragePlaylist>()
        analyzeStoragePlayListsData(
            storagePlayLists,
            playListsDao.getAllAsStoragePlayLists(),
            newDbPlaylistsFromStorage
        )
        for (newDbPlaylist in newDbPlaylistsFromStorage) {
            playListsDao.insertStoragePlaylist(
                newDbPlaylist,
                playlistsStorageProvider.getPlayListItems(newDbPlaylist.storageId)
            )
        }

        //compare file cache and db
        val newDbPlaylists = ArrayList<PlayListFile>()
        val newCachePlaylists = ArrayList<AppPlaylist>()
        val updateDbPlaylists = ArrayList<Pair<AppPlaylist, PlayListFile>>()
        analyzeCachedPlayListsData(
            playListsDao.getAllPlayLists(),
            playlistsFilesStorage.getCachedPlaylists(),
            newDbPlaylists,
            newCachePlaylists,
            updateDbPlaylists,
        )
        val pathIdMap = HashMap<String, Long>()
        for (newDbPlaylist in newDbPlaylists) {
            val compositionIds = compositionsDao.getCompositionIds(newDbPlaylist.entries, pathIdMap)
            try {
                playListsDao.insertPlaylist(
                    newDbPlaylist.name,
                    newDbPlaylist.createDate,
                    newDbPlaylist.modifyDate,
                    compositionIds
                )
            } catch (_: PlaylistAlreadyExistsException) {}
        }
        for (newCachePlayList in newCachePlaylists) {
            val playlistFile = PlayListFile(
                newCachePlayList.name,
                newCachePlayList.addedTime,
                newCachePlayList.modifiedTime,
                playListsDao.getPlayListItemsAsFileEntries(newCachePlayList.id)
            )
            playlistsFilesStorage.insertPlaylist(playlistFile)
        }
        for (newDbPlaylist in updateDbPlaylists) {
            val entries = newDbPlaylist.second.entries
            val compositionIds = compositionsDao.getCompositionIds(entries, pathIdMap)
            val playlistId = newDbPlaylist.first.id
            playListsDao.setPlayListEntries(playlistId, compositionIds)
        }
    }

    fun analyzeCachedPlayListsData(
        dbPlayLists: List<AppPlaylist>,
        cachedPlayLists: List<PlayListFile>,
        outNewDbPlaylists: ArrayList<PlayListFile>,
        outNewCachePlaylists: ArrayList<AppPlaylist>,
        outUpdateDbPlaylists: ArrayList<Pair<AppPlaylist, PlayListFile>>,
    ) {
        val dbPlayListsMap = ListUtils.mapToMap(
            dbPlayLists,
            HashMap(),
            AppPlaylist::name
        )
        val cachePlayListsMap = ListUtils.mapToMap(
            cachedPlayLists,
            HashMap(),
            PlayListFile::name
        )
        mergeMaps(
            dbPlayListsMap,
            cachePlayListsMap,
            outNewCachePlaylists::add,
            outNewDbPlaylists::add,
            { playlist, playlistFile ->
                playlist.modifiedTime != playlistFile.modifyDate || playlist.compositionsCount != playlistFile.entries.size
            },
            { playlist, playlistFile ->
                playlist.modifiedTime > playlistFile.modifyDate || playlist.compositionsCount > playlistFile.entries.size
            },
            { old, new -> outUpdateDbPlaylists.add(Pair(old, new)) },
            { _, _ -> }//ignore, we update cache after edit
        )
    }

    fun analyzeStoragePlayListsData(
        storagePlayLists: Map<String, StoragePlaylist>,
        dbPlayLists: List<AppPlaylist>,
        outNewDbPlaylists: ArrayList<StoragePlaylist>
    ) {
        val dbPlayListsMap = ListUtils.mapToMap(
            dbPlayLists,
            HashMap(),
            AppPlaylist::name
        )
        mergeMaps(
            dbPlayListsMap,
            storagePlayLists,
            {},
            outNewDbPlaylists::add,
            ::hasActualChanges,
            ::hasActualChanges,
            { _, _ -> },
            { _, _ -> }
        )
    }

    private fun hasActualChanges(first: AppPlaylist, second: StoragePlaylist): Boolean {
        return first.name != second.name && first.modifiedTime > second.modifiedTime
    }

}