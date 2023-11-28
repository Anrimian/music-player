package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists

import androidx.core.util.Pair
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListFile
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlayList
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.domain.utils.Objects
import com.github.anrimian.musicplayer.domain.utils.mergeMaps
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils


/**
 * If there will be new playlists from media storage -> they will be copied to db
 * Then:
 * If there will be new playlists from db -> they will be copied to file cache
 * If there will be new playlists from file cache -> try will be copied to db
 * If playlist modify date or item count is less than in cache -> db items will be rewritten from cache
 */
class StoragePlaylistsAnalyzer(
    private val compositionsDao: CompositionsDaoWrapper,
    private val playListsDao: PlayListsDaoWrapper,
    private val playlistsStorageProvider: StoragePlayListsProvider,
    private val playlistsFilesStorage: PlaylistFilesStorage
) {

    fun applyPlayListsData(storagePlayLists: Map<String, StoragePlayList>) {
        //compare media storage and db
        val newDbPlaylistsFromStorage = ArrayList<StoragePlayList>()
        analyzeStoragePlayListsData(
            storagePlayLists,
            playListsDao.allAsStoragePlayLists,
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
        val newCachePlaylists = ArrayList<AppPlayList>()
        val updateDbPlaylists = ArrayList<Pair<AppPlayList, PlayListFile>>()
        analyzeCachedPlayListsData(
            playListsDao.allPlayLists,
            playlistsFilesStorage.getCachedPlaylists(),
            newDbPlaylists,
            newCachePlaylists,
            updateDbPlaylists,
        )
        val pathIdMap = HashMap<String, Long>()
        for (newDbPlaylist in newDbPlaylists) {
            val compositionIds = compositionsDao.getCompositionIds(newDbPlaylist.entries, pathIdMap)
            playListsDao.insertPlayList(
                newDbPlaylist.name,
                newDbPlaylist.createDate,
                newDbPlaylist.modifyDate,
                compositionIds
            )
        }
        for (newCachePlayList in newCachePlaylists) {
            val playlistFile = PlayListFile(
                newCachePlayList.name,
                newCachePlayList.dateAdded,
                newCachePlayList.dateModified,
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
        dbPlayLists: List<AppPlayList>,
        cachedPlayLists: List<PlayListFile>,
        outNewDbPlaylists: ArrayList<PlayListFile>,
        outNewCachePlaylists: ArrayList<AppPlayList>,
        outUpdateDbPlaylists: ArrayList<Pair<AppPlayList, PlayListFile>>,
    ) {
        val dbPlayListsMap = ListUtils.mapToMap(
            dbPlayLists,
            HashMap(),
            AppPlayList::getName
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
                playlist.dateModified != playlistFile.modifyDate || playlist.compositionsCount != playlistFile.entries.size
            },
            { playlist, playlistFile ->
                playlist.dateModified > playlistFile.modifyDate || playlist.compositionsCount > playlistFile.entries.size
            },
            { old, new -> outUpdateDbPlaylists.add(Pair(old, new)) },
            { _, _ -> }//ignore, we update cache after edit
        )
    }

    fun analyzeStoragePlayListsData(
        storagePlayLists: Map<String, StoragePlayList>,
        dbPlayLists: List<AppPlayList>,
        outNewDbPlaylists: ArrayList<StoragePlayList>
    ) {
        val dbPlayListsMap = ListUtils.mapToMap(
            dbPlayLists,
            HashMap(),
            AppPlayList::getName
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

    private fun hasActualChanges(first: AppPlayList, second: StoragePlayList): Boolean {
        return !Objects.equals(first.name, second.name)
                && DateUtils.isAfter(first.dateModified, second.dateModified)
    }

}