package com.github.anrimian.musicplayer.data.database.dao.playlist

import android.util.Log
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlaylistEntryEntity
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylist
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylistItem
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlayListsDaoWrapperTest {

    private lateinit var playListDao: PlaylistDao
    private lateinit var compositionsDao: CompositionsDao
    private lateinit var db: LibraryDatabase

    private lateinit var daoWrapper: PlaylistsDaoWrapper

    @BeforeEach
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        db = Room.inMemoryDatabaseBuilder(context, LibraryDatabase::class.java).build()
        compositionsDao = db.compositionsDao()
        playListDao = db.playListDao()

        daoWrapper = PlaylistsDaoWrapper(playListDao, compositionsDao, db)
    }

    @AfterEach
    fun tearDown() {
        db.close()
    }

    @Test
    fun testMoveItems() {
        val time = System.currentTimeMillis()
        val playlistId = daoWrapper.insertPlaylist("playlist", time, time) { null }

        for (i in 0..9) {
            val id = DbTestUtils.insert(compositionsDao, null, null, i.toString())
            playListDao.insertPlayListEntries(
                listOf(
                    PlaylistEntryEntity(
                        0,
                        null,
                        id,
                        playlistId,
                        i
                    )
                )
            )
        }

        var items = daoWrapper.getPlayListItemsObservable(playlistId, false, null).blockingFirst()
        displayItems("testMoveItems, items: ", items)

        daoWrapper.moveItems(playlistId, 0, 7)

        items = daoWrapper.getPlayListItemsObservable(playlistId, false, null).blockingFirst()
        displayItems("testMoveItems, moved items: ", items)
    }

    @Test
    fun getNextOrderPositionBasedOnMaxOrderPosition() {
        val time = System.currentTimeMillis()
        val playlistId = daoWrapper.insertPlaylist("playlist", time, time) { null }

        // Sparse positions: 10, 20
        for (i in 0..1) {
            val id = DbTestUtils.insert(compositionsDao, null, null, i.toString())
            playListDao.insertPlayListEntry(null, id, playlistId, if (i == 0) 10 else 20)
        }

        assertEquals(21, daoWrapper.getNextOrderPosition(playlistId))
    }

    @Test
    fun getNextOrderPositionForEmptyPlaylist() {
        val time = System.currentTimeMillis()
        val playlistId = daoWrapper.insertPlaylist("playlist", time, time) { null }

        assertEquals(0, daoWrapper.getNextOrderPosition(playlistId))
    }

    @Test
    fun insertPlayListItemsAppendsAtTheEndUsingGetNextOrderPosition() {
        val time = System.currentTimeMillis()
        val playlistId = daoWrapper.insertPlaylist("playlist", time, time) { null }

        // Start with a sparse position 50
        val id1 = compositionsDao.insert(
            null, null, null, "1", null, null, null, null, "test", 100L, 100L, 1L, 
            time, time, 0, 0, 0, 0, LocalFileStatus.AVAILABLE, null, InitialSource.LOCAL
        )
        playListDao.insertPlayListEntry(null, id1, playlistId, 50)

        // Then append a second item using the default parameter
        val storageId2 = 2L
        compositionsDao.insert(
            null, null, null, "2", null, null, null, null, "test", 100L, 100L, storageId2, 
            time, time, 0, 0, 0, 0, LocalFileStatus.AVAILABLE, null, InitialSource.LOCAL
        )
        val item2 = StoragePlaylistItem(2, storageId2)
        daoWrapper.insertPlayListItems(listOf(item2), playlistId)

        // Check the second item's position, it should be 51
        val items = daoWrapper.getPlayListItemsObservable(playlistId, false, null).blockingFirst()
        assertEquals(2, items.size)
        // items are ordered by orderPosition
        assertEquals(51, playListDao.selectPositionById(items[1].entryId))
    }

    @Test
    fun testUpdatePlaylistNameThatAlreadyExists() {
        val time = System.currentTimeMillis()
        daoWrapper.insertPlaylist("test", time, time) { 1L }
        daoWrapper.insertPlaylist("test1", time, time) { 2L }
        daoWrapper.insertPlaylist("test2", time, time) { 3L }

        val duplicatePlayList2 = StoragePlaylist(2L, "test", time, time)
        val duplicatePlayList3 = StoragePlaylist(3L, "test", time, time)
//        daoWrapper.applyChanges(emptyList(), asList(
//                new Change<>(playList2, duplicatePlayList2),
//                new Change<>(playList3, duplicatePlayList3)
//        ));

        println("KEKAS" + daoWrapper.getPlayListsObservable(null).blockingFirst())
    }

    private fun displayItems(message: String, items: List<PlaylistEntry>) {
        val text = items.joinToString(separator = "\n") { item ->
            "itemId = ${item.entryId}; title = ${item.title};"
        }
        Log.d("KEK", "$message\n$text")
        println("$message\n$text")
    }
}