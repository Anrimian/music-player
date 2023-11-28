package com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists

import androidx.core.util.Pair
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListFile
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlayList
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class StoragePlaylistsAnalyzerTest {

    private val compositionsDao: CompositionsDaoWrapper = mock()
    private val playListsDao: PlayListsDaoWrapper = mock()
    private val playlistsStorageProvider: StoragePlayListsProvider = mock()
    private val playlistsPlaylistFilesStorage: PlaylistFilesStorage = mock()

    private val analyzer = StoragePlaylistsAnalyzer(
        compositionsDao,
        playListsDao,
        playlistsStorageProvider,
        playlistsPlaylistFilesStorage
    )

    @Test
    fun `copy new playlist from storage to db test`() {
        whenever(playListsDao.allAsStoragePlayLists).doReturn(emptyList())
        val storagePlayList = mock<StoragePlayList>()
        val storagePlaylists = mapOf("0", storagePlayList)

        val newDbPlaylists = ArrayList<StoragePlayList>()
        analyzer.analyzeStoragePlayListsData(storagePlaylists, emptyList(), newDbPlaylists)

        assertEquals(1, newDbPlaylists.size)
        assertEquals(storagePlayList, newDbPlaylists[0])
    }

    @Test
    fun `copy playlist from db to cache test`() {
        val storagePlayList = mock<AppPlayList> { on { name } doReturn "1" }

        val newDbPlaylists = ArrayList<PlayListFile>()
        val newCachePlaylists = ArrayList<AppPlayList>()
        analyzer.analyzeCachedPlayListsData(
            listOf(storagePlayList),
            emptyList(),
            newDbPlaylists,
            newCachePlaylists,
            mock(),
        )

        assertEquals(1, newCachePlaylists.size)
        assertEquals(storagePlayList, newCachePlaylists[0])
        assertTrue(newDbPlaylists.isEmpty())
    }

    @Test
    fun `copy playlist from cache to db test`() {
        val filePlayList = mock<PlayListFile> { on { name } doReturn "1" }

        val newDbPlaylists = ArrayList<PlayListFile>()
        val newCachePlaylists = ArrayList<AppPlayList>()
        analyzer.analyzeCachedPlayListsData(
            emptyList(),
            listOf(filePlayList),
            newDbPlaylists,
            newCachePlaylists,
            mock(),
        )

        assertEquals(1, newDbPlaylists.size)
        assertEquals(filePlayList, newDbPlaylists[0])
        assertTrue(newCachePlaylists.isEmpty())
    }

    @Test
    fun `copy updated playlist from cache to db test`() {
        val testName = "1"
        val filePlayList = mock<PlayListFile> {
            on { name } doReturn testName
            on { modifyDate } doReturn Date(2L)
        }
        val dbPlayList = mock<AppPlayList> {
            on { name } doReturn testName
            on { dateModified } doReturn Date(1L)
        }

        val updateDbPlaylists = ArrayList<Pair<AppPlayList, PlayListFile>>()
        analyzer.analyzeCachedPlayListsData(
            listOf(dbPlayList),
            listOf(filePlayList),
            mock(),
            mock(),
            updateDbPlaylists
        )

        assertEquals(1, updateDbPlaylists.size)
        assertEquals(filePlayList, updateDbPlaylists[0].second)
    }

    @Test
    fun `when db item count is less than cache item count - copy cached playlist items to db test`() {
        val testName = "1"
        val filePlayList = mock<PlayListFile> {
            on { name } doReturn testName
            on { modifyDate } doReturn Date(1L)
            on { entries } doReturn listOf(mock())
        }
        val dbPlayList = mock<AppPlayList> {
            on { name } doReturn testName
            on { dateModified } doReturn Date(1L)
            on { compositionsCount } doReturn 0
        }

        val updateDbPlaylists = ArrayList<Pair<AppPlayList, PlayListFile>>()
        analyzer.analyzeCachedPlayListsData(
            listOf(dbPlayList),
            listOf(filePlayList),
            mock(),
            mock(),
            updateDbPlaylists
        )

        assertEquals(1, updateDbPlaylists.size)
        assertEquals(filePlayList, updateDbPlaylists[0].second)
    }

    private fun <T> mapOf(key: String, value: T): Map<String, T> {
        return HashMap<String, T>().apply { put(key, value) }
    }
}