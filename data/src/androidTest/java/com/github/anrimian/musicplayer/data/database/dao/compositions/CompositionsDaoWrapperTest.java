package com.github.anrimian.musicplayer.data.database.dao.compositions;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.composition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CompositionsDaoWrapperTest {

    private AppDatabase db;
    private CompositionsDao compositionsDao;
    private ArtistsDao artistsDao;
    private AlbumsDao albumsDao;

    private CompositionsDaoWrapper daoWrapper;

    @BeforeEach
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        compositionsDao = db.compositionsDao();
        artistsDao = db.artistsDao();
        albumsDao = db.albumsDao();

        daoWrapper = new CompositionsDaoWrapper(db,
                artistsDao,
                compositionsDao,
                albumsDao,
                db.genreDao(),
                db.foldersDao());
    }

    @AfterEach
    public void tearDown() {
        db.close();
    }

    @Test
    public void updateArtistToUnknownName() {
        long artistId = artistsDao.insertArtist( "test artist");
        long compositionId = compositionsDao.insert(composition(artistId, null, "test title"));

        daoWrapper.updateArtist(compositionId, "test artist2");

        Long newArtistId = artistsDao.findArtistIdByName("test artist2");
        assertNull(artistsDao.findArtistIdByName("test artist"));
        assertNotNull(newArtistId);

        assertEquals(newArtistId, compositionsDao.getArtistId(compositionId));
    }

    @Test
    public void updateArtistFromUnknownName() {
        long compositionId = compositionsDao.insert(composition(null, null, "test title"));

        daoWrapper.updateArtist(compositionId, "test artist2");

        Long newArtistId = artistsDao.findArtistIdByName("test artist2");
        assertNotNull(newArtistId);

        assertEquals(newArtistId, compositionsDao.getArtistId(compositionId));
    }

    @Test
    public void nullifyArtist() {
        long artistId = artistsDao.insertArtist( "test artist");
        long compositionId = compositionsDao.insert(composition(artistId, null, "test title"));

        daoWrapper.updateArtist(compositionId, null);

        assertNull(artistsDao.findArtistIdByName("test artist"));
        assertNull(compositionsDao.getArtistId(compositionId));
    }

    @Test
    public void updateArtistToKnownName() {
        Long artistId = artistsDao.insertArtist( "test artist");
        Long secondArtistId = artistsDao.insertArtist( "test artist2");
        long compositionId = compositionsDao.insert(composition(artistId, null, "test title"));

        daoWrapper.updateArtist(compositionId, "test artist2");

        assertNull(artistsDao.findArtistIdByName("test artist"));
        assertNotNull(artistsDao.findArtistIdByName("test artist2"));

        assertEquals(secondArtistId, compositionsDao.getArtistId(compositionId));
    }

    @Test
    public void updateAlbumArtistWithSingleAlbum() {
        long artistId = artistsDao.insertArtist( "test artist");
        long albumId = albumsDao.insertAlbum(artistId, "test album");
        long compositionId = compositionsDao.insert(composition(null, albumId, "test title"));

        daoWrapper.updateAlbumArtist(compositionId, "test artist2");

        Long newArtistId = artistsDao.findArtistIdByName("test artist2");
        assertNull(artistsDao.findArtistIdByName("test artist"));
        assertNotNull(newArtistId);

        long newAlbumId = compositionsDao.getAlbumId(compositionId);
        Long albumArtist = albumsDao.getAlbumEntity(newAlbumId).getArtistId();
        assertNotNull(albumArtist);
        assertEquals(newArtistId, albumArtist);
    }

    @Test
    public void nullifyAlbumArtist() {
        long artistId = artistsDao.insertArtist( "test artist");
        long albumId = albumsDao.insertAlbum(artistId, "test album");
        long compositionId = compositionsDao.insert(composition(null, albumId, "test title"));

        daoWrapper.updateAlbumArtist(compositionId, null);

        assertNull(artistsDao.findArtistIdByName("test artist"));

        long newAlbumId = compositionsDao.getAlbumId(compositionId);
        Long albumArtist = albumsDao.getAlbumEntity(newAlbumId).getArtistId();
        assertNull(albumArtist);
    }

    @Test
    public void updateAlbumArtistWithMultipleEntriesAlbum() {
        Long artistId = artistsDao.insertArtist( "test artist");
        long albumId = albumsDao.insertAlbum(artistId, "test album");
        long compositionId = compositionsDao.insert(composition(null, albumId, "test title"));
        compositionsDao.insert(composition(null, albumId, "test title2"));

        daoWrapper.updateAlbumArtist(compositionId, "test artist2");

        //check new artist
        Long newArtistId = artistsDao.findArtistIdByName("test artist2");
        assertEquals(artistId, artistsDao.findArtistIdByName("test artist"));
        assertNotNull(newArtistId);

        //check old album
        Long albumArtist = albumsDao.getAlbumEntity(albumId).getArtistId();
        assertNotNull(albumArtist);
        assertEquals(artistId, albumArtist);

        //check new album
        long newAlbumId = compositionsDao.getAlbumId(compositionId);
        assertNotEquals(0, newAlbumId);
        AlbumEntity newAlbum = albumsDao.getAlbumEntity(newAlbumId);
        assertEquals("test album", newAlbum.getName());
        Long newAlbumArtist = newAlbum.getArtistId();
        assertNotNull(newAlbumArtist);
        assertEquals(newArtistId, newAlbumArtist);
    }

    @Test
    public void updateArtistWitFullAlbumMove() {
        long artistId = artistsDao.insertArtist( "test artist");
        long albumId = albumsDao.insertAlbum(artistId, "test album");
        long compositionId = compositionsDao.insert(composition(null, albumId, "test title"));
        long secondCompositionId = compositionsDao.insert(composition(null, albumId, "test title2"));

        daoWrapper.updateAlbumArtist(compositionId, "test artist2");
        daoWrapper.updateAlbumArtist(secondCompositionId, "test artist2");

        //check old artist
        assertNull(artistsDao.findArtistIdByName("test artist"));

        //check new artist
        Long newArtistId = artistsDao.findArtistIdByName("test artist2");
        assertNotNull(newArtistId);

        //check old album
        assertNull(albumsDao.getAlbumEntity(albumId));

        ////check new album
        Long newAlbumId = compositionsDao.getAlbumId(compositionId);
        assertEquals(newAlbumId, compositionsDao.getAlbumId(secondCompositionId));

        AlbumEntity newAlbum = albumsDao.getAlbumEntity(newAlbumId);
        assertEquals("test album", newAlbum.getName());
        Long newAlbumArtist = newAlbum.getArtistId();
        assertNotNull(newAlbumArtist);
        assertEquals(newArtistId, newAlbumArtist);
    }

    @Test
    public void updateAlbumToUnknownName() {
        long artistId = artistsDao.insertArtist( "test artist");
        albumsDao.insertAlbum(artistId, "test album");
        long compositionId = compositionsDao.insert(composition(artistId, null, "test title"));

        daoWrapper.updateAlbum(compositionId, "test album2");

        Long newAlbumId = albumsDao.findAlbum(artistId, "test album2");
        assertNull(albumsDao.findAlbum(artistId, "test artist"));
        assertNotNull(newAlbumId);

        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId));
    }

    @Test
    public void nullifyAlbum() {
        long artistId = artistsDao.insertArtist( "test artist");
        albumsDao.insertAlbum(artistId, "test album");
        long compositionId = compositionsDao.insert(composition(artistId, null, "test title"));

        daoWrapper.updateAlbum(compositionId, null);

        assertNull(albumsDao.findAlbum(artistId, "test artist"));

        assertNull(compositionsDao.getAlbumId(compositionId));
    }

    @Test
    public void updateAlbumToKnownName() {
        long artistId = artistsDao.insertArtist( "test artist");
        albumsDao.insertAlbum(artistId, "test album");
        Long secondAlbumId = albumsDao.insertAlbum(artistId, "test album2");
        long compositionId = compositionsDao.insert(composition(artistId, null, "test title"));

        daoWrapper.updateAlbum(compositionId, "test album2");

        Long newAlbumId = albumsDao.findAlbum(artistId, "test album2");
        assertNull(albumsDao.findAlbum(artistId, "test artist"));
        assertNotNull(newAlbumId);

        assertEquals(secondAlbumId, compositionsDao.getAlbumId(compositionId));
    }

    @Test
    public void updateAlbumToWithDifferentAlbumArtist() {
        long compositionArtistId = artistsDao.insertArtist( "test artist");
        long albumArtistId = artistsDao.insertArtist( "test album artist");
        long oldAlbumId = albumsDao.insertAlbum(albumArtistId, "test album");
        long compositionId = compositionsDao.insert(composition(compositionArtistId, oldAlbumId, "test title"));

        daoWrapper.updateAlbum(compositionId, "test album2");

        Long newAlbumId = albumsDao.findAlbum(albumArtistId, "test album2");
        assertNull(albumsDao.findAlbum(albumArtistId, "test artist"));
        assertNotNull(newAlbumId);

        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId));
    }

    @Test
    public void updateAlbumWithOldAlbumWithoutArtist() {
        long compositionArtistId = artistsDao.insertArtist( "test artist");
        long oldAlbumId = albumsDao.insertAlbum(null, "test album");
        long compositionId = compositionsDao.insert(composition(compositionArtistId, oldAlbumId, "test title"));

        daoWrapper.updateAlbum(compositionId, "test album2");

        Long newAlbumId = albumsDao.findAlbum(compositionArtistId, "test album2");
        assertNull(albumsDao.findAlbum(compositionArtistId, "test artist"));
        assertNotNull(newAlbumId);

        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId));
    }

    @Test
    public void updateAlbumWithoutArtist() {
        long compositionId = compositionsDao.insert(composition(null, null, "test title"));

        daoWrapper.updateAlbum(compositionId, "test album2");

        Long newAlbumId = albumsDao.findAlbum(null, "test album2");
        assertNotNull(newAlbumId);
        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId));
    }

    @Test
    public void updateAlbumWithoutArtistAndWithKnownName() {
        Long secondAlbumId = albumsDao.insertAlbum(null, "test album2");
        long compositionId = compositionsDao.insert(composition(null, null, "test title"));

        daoWrapper.updateAlbum(compositionId, "test album2");

        Long newAlbumId = albumsDao.findAlbum(null, "test album2");
        assertNull(albumsDao.findAlbum(null, "test artist"));
        assertEquals(secondAlbumId, newAlbumId);
        assertEquals(newAlbumId, compositionsDao.getAlbumId(compositionId));
    }
}