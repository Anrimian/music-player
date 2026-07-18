package com.github.anrimian.musicplayer.data.database.dao.compositions

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.playlist.DbTestUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CompositionsDaoWrapperTest {

    private lateinit var db: LibraryDatabase
    private lateinit var compositionsDao: CompositionsDao
    private lateinit var artistsDao: ArtistsDao
    private lateinit var albumsDao: AlbumsDao

    private lateinit var daoWrapper: CompositionsDaoWrapper

    @BeforeEach
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        db = Room.inMemoryDatabaseBuilder(context, LibraryDatabase::class.java).build()
        compositionsDao = db.compositionsDao()
        artistsDao = db.artistsDao()
        albumsDao = db.albumsDao()

        daoWrapper = CompositionsDaoWrapper(
            db,
            artistsDao,
            ArtistsDaoWrapper(db, artistsDao, albumsDao),
            compositionsDao,
            albumsDao,
            db.genreDao(),
            db.foldersDao()
        )
    }

    @AfterEach
    fun tearDown() {
        db.close()
    }

    @Test
    fun updateArtistToUnknownName() {
        val artistId = artistsDao.insertArtist("test artist")
        val compositionId = DbTestUtils.insert(compositionsDao, artistId, null, "test title")

        daoWrapper.updateArtist(compositionId, "test artist2")

        val newArtistId = artistsDao.findArtistIdByName("test artist2")
        assertNull(artistsDao.findArtistIdByName("test artist"))
        assertNotNull(newArtistId)

        assertEquals(newArtistId, compositionsDao.getArtistId(compositionId))
    }

    @Test
    fun updateArtistFromUnknownName() {
        val compositionId = DbTestUtils.insert(compositionsDao, null, null, "test title")

        daoWrapper.updateArtist(compositionId, "test artist2")

        val newArtistId = artistsDao.findArtistIdByName("test artist2")
        assertNotNull(newArtistId)

        assertEquals(newArtistId, compositionsDao.getArtistId(compositionId))
    }

    @Test
    fun nullifyArtist() {
        val artistId = artistsDao.insertArtist("test artist")
        val compositionId = DbTestUtils.insert(compositionsDao, artistId, null, "test title")

        daoWrapper.updateArtist(compositionId, null)

        assertNull(artistsDao.findArtistIdByName("test artist"))
        assertNull(compositionsDao.getArtistId(compositionId))
    }

    @Test
    fun updateArtistToKnownName() {
        val artistId = artistsDao.insertArtist("test artist")
        val secondArtistId = artistsDao.insertArtist("test artist2")
        val compositionId = DbTestUtils.insert(compositionsDao, artistId, null, "test title")

        daoWrapper.updateArtist(compositionId, "test artist2")

        assertNull(artistsDao.findArtistIdByName("test artist"))
        assertNotNull(artistsDao.findArtistIdByName("test artist2"))

        assertEquals(secondArtistId, compositionsDao.getArtistId(compositionId))
    }

    @Test
    fun updateAlbumArtistWithSingleAlbum() {
        val artistId = artistsDao.insertArtist("test artist")
        val albumId = albumsDao.insertAlbum(artistId, "test album")
        val compositionId = DbTestUtils.insert(compositionsDao, null, albumId, "test title")

        daoWrapper.updateAlbumArtist(compositionId, "test artist2")

        val newArtistId = artistsDao.findArtistIdByName("test artist2")
        assertNull(artistsDao.findArtistIdByName("test artist"))
        assertNotNull(newArtistId)

        val newAlbumId = compositionsDao.getAlbumId(compositionId)!!
        val albumArtist = albumsDao.getAlbumEntity(newAlbumId).artistId
        assertNotNull(albumArtist)
        assertEquals(newArtistId, albumArtist)
    }

    @Test
    fun nullifyAlbumArtist() {
        val artistId = artistsDao.insertArtist("test artist")
        val albumId = albumsDao.insertAlbum(artistId, "test album")
        val compositionId = DbTestUtils.insert(compositionsDao, null, albumId, "test title")

        daoWrapper.updateAlbumArtist(compositionId, null)

        assertNull(artistsDao.findArtistIdByName("test artist"))

        val newAlbumId = compositionsDao.getAlbumId(compositionId)!!
        val albumArtist = albumsDao.getAlbumEntity(newAlbumId).artistId
        assertNull(albumArtist)
    }

    @Test
    fun updateAlbumArtistWithMultipleEntriesAlbum() {
        val artistId = artistsDao.insertArtist("test artist")
        val albumId = albumsDao.insertAlbum(artistId, "test album")
        val compositionId = DbTestUtils.insert(compositionsDao, null, albumId, "test title")
        DbTestUtils.insert(compositionsDao, null, albumId, "test title2")

        daoWrapper.updateAlbumArtist(compositionId, "test artist2")

        //check new artist
        val newArtistId = artistsDao.findArtistIdByName("test artist2")
        assertEquals(artistId, artistsDao.findArtistIdByName("test artist"))
        assertNotNull(newArtistId)

        //check old album
        val albumArtist = albumsDao.getAlbumEntity(albumId).artistId
        assertNotNull(albumArtist)
        assertEquals(artistId, albumArtist)

        //check new album
        val newAlbumId = compositionsDao.getAlbumId(compositionId)!!
        assertNotEquals(0, newAlbumId)
        val newAlbum = albumsDao.getAlbumEntity(newAlbumId)
        assertEquals("test album", newAlbum.name)
        val newAlbumArtist = newAlbum.artistId
        assertNotNull(newAlbumArtist)
        assertEquals(newArtistId, newAlbumArtist)
    }

    @Test
    fun updateArtistWitFullAlbumMove() {
        val artistId = artistsDao.insertArtist("test artist")
        val albumId = albumsDao.insertAlbum(artistId, "test album")
        val compositionId = DbTestUtils.insert(compositionsDao, null, albumId, "test title")
        val secondCompositionId = DbTestUtils.insert(compositionsDao, null, albumId, "test title2")

        daoWrapper.updateAlbumArtist(compositionId, "test artist2")
        daoWrapper.updateAlbumArtist(secondCompositionId, "test artist2")

        //check old artist
        assertNull(artistsDao.findArtistIdByName("test artist"))

        //check new artist
        val newArtistId = artistsDao.findArtistIdByName("test artist2")
        assertNotNull(newArtistId)

        //check old album
        assertNull(albumsDao.getAlbumEntity(albumId))

        //check new album
        val newAlbumId = compositionsDao.getAlbumId(compositionId)
        assertEquals(newAlbumId, compositionsDao.getAlbumId(secondCompositionId))

        val newAlbum = albumsDao.getAlbumEntity(newAlbumId!!)
        assertEquals("test album", newAlbum.name)
        val newAlbumArtist = newAlbum.artistId
        assertNotNull(newAlbumArtist)
        assertEquals(newArtistId, newAlbumArtist)
    }

    @Test
    fun updateAlbumToUnknownName() {
        val artistId = artistsDao.insertArtist("test artist")
        albumsDao.insertAlbum(artistId, "test album")
        val compositionId = DbTestUtils.insert(compositionsDao, artistId, null, "test title")

        daoWrapper.updateAlbum(compositionId, "test album2")

        val newAlbumId = albumsDao.findAlbum(artistId, "test album2")
        assertNull(albumsDao.findAlbum(artistId, "test artist"))
        assertNotNull(newAlbumId)

        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId))
    }

    @Test
    fun nullifyAlbum() {
        val artistId = artistsDao.insertArtist("test artist")
        albumsDao.insertAlbum(artistId, "test album")
        val compositionId = DbTestUtils.insert(compositionsDao, artistId, null, "test title")

        daoWrapper.updateAlbum(compositionId, null)

        assertNull(albumsDao.findAlbum(artistId, "test artist"))

        assertNull(compositionsDao.getAlbumId(compositionId))
    }

    @Test
    fun updateAlbumToKnownName() {
        val artistId = artistsDao.insertArtist("test artist")
        albumsDao.insertAlbum(artistId, "test album")
        val secondAlbumId = albumsDao.insertAlbum(artistId, "test album2")
        val compositionId = DbTestUtils.insert(compositionsDao, artistId, null, "test title")

        daoWrapper.updateAlbum(compositionId, "test album2")

        val newAlbumId = albumsDao.findAlbum(artistId, "test album2")
        assertNull(albumsDao.findAlbum(artistId, "test artist"))
        assertNotNull(newAlbumId)

        assertEquals(secondAlbumId, compositionsDao.getAlbumId(compositionId))
    }

    @Test
    fun updateAlbumToWithDifferentAlbumArtist() {
        val compositionArtistId = artistsDao.insertArtist("test artist")
        val albumArtistId = artistsDao.insertArtist("test album artist")
        val oldAlbumId = albumsDao.insertAlbum(albumArtistId, "test album")
        val compositionId =
            DbTestUtils.insert(compositionsDao, compositionArtistId, oldAlbumId, "test title")

        daoWrapper.updateAlbum(compositionId, "test album2")

        val newAlbumId = albumsDao.findAlbum(albumArtistId, "test album2")
        assertNull(albumsDao.findAlbum(albumArtistId, "test artist"))
        assertNotNull(newAlbumId)

        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId))
    }

    @Test
    fun updateAlbumWithOldAlbumWithoutArtist() {
        val compositionArtistId = artistsDao.insertArtist("test artist")
        val oldAlbumId = albumsDao.insertAlbum(null, "test album")
        val compositionId =
            DbTestUtils.insert(compositionsDao, compositionArtistId, oldAlbumId, "test title")

        daoWrapper.updateAlbum(compositionId, "test album2")

        val newAlbumId = albumsDao.findAlbum(compositionArtistId, "test album2")
        assertNull(albumsDao.findAlbum(compositionArtistId, "test artist"))
        assertNotNull(newAlbumId)

        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId))
    }

    @Test
    fun updateAlbumWithoutArtist() {
        val compositionId = DbTestUtils.insert(compositionsDao, null, null, "test title")

        daoWrapper.updateAlbum(compositionId, "test album2")

        val newAlbumId = albumsDao.findAlbum(null, "test album2")
        assertNotNull(newAlbumId)
        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId))
    }

    @Test
    fun updateAlbumWithoutArtistAndWithKnownName() {
        val secondAlbumId = albumsDao.insertAlbum(null, "test album2")
        val compositionId = DbTestUtils.insert(compositionsDao, null, null, "test title")

        daoWrapper.updateAlbum(compositionId, "test album2")

        val newAlbumId = albumsDao.findAlbum(null, "test album2")
        assertNull(albumsDao.findAlbum(null, "test artist"))
        assertEquals(secondAlbumId, newAlbumId)
        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId))
    }
}
