package com.github.anrimian.musicplayer.data.database.dao.compositions;

import android.content.Context;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CompositionsDaoWrapperTest {

    private AppDatabase db;
    private CompositionsDao compositionsDao;
    private ArtistsDao artistsDao;
    private AlbumsDao albumsDao;

    private CompositionsDaoWrapper daoWrapper;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        compositionsDao = db.compositionsDao();
        artistsDao = db.artistsDao();
        albumsDao = db.albumsDao();

        daoWrapper = new CompositionsDaoWrapper(db, artistsDao, compositionsDao, albumsDao);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void updateArtistToUnknownName() {
        long artistId = artistsDao.insertArtist(new ArtistEntity(null, "test artist"));
        long compositionId = compositionsDao.insert(composition(artistId, null, "test title"));

        daoWrapper.updateArtist(compositionId, "test artist2");

        long newArtistId = artistsDao.findArtistIdByName("test artist2");
        assertEquals(0, artistsDao.findArtistIdByName("test artist"));
        assertNotEquals(0, newArtistId);

        assertEquals(newArtistId, compositionsDao.getArtistId(compositionId));
    }

    @Test
    public void updateArtistToKnownName() {
        long artistId = artistsDao.insertArtist(new ArtistEntity(null, "test artist"));
        long secondArtistId = artistsDao.insertArtist(new ArtistEntity(null, "test artist2"));
        long compositionId = compositionsDao.insert(composition(artistId, null, "test title"));

        daoWrapper.updateArtist(compositionId, "test artist2");

        assertEquals(0, artistsDao.findArtistIdByName("test artist"));
        assertNotEquals(0, secondArtistId);

        assertEquals(secondArtistId, compositionsDao.getArtistId(compositionId));
    }

    @Test
    public void updateArtistWithSingleAlbum() {
        long artistId = artistsDao.insertArtist(new ArtistEntity(null, "test artist"));
        long albumId = albumsDao.insert(new AlbumEntity(artistId, null, "test album", 0, 0));
        long compositionId = compositionsDao.insert(composition(artistId, albumId, "test title"));

        daoWrapper.updateArtist(compositionId, "test artist2");

        long newArtistId = artistsDao.findArtistIdByName("test artist2");
        assertEquals(0, artistsDao.findArtistIdByName("test artist"));
        assertNotEquals(0, newArtistId);

        assertEquals(newArtistId, compositionsDao.getArtistId(compositionId));

        long newAlbumId = compositionsDao.getAlbumId(compositionId);
        Long albumArtist = albumsDao.getAlbumEntity(newAlbumId).getArtistId();
        assertNotNull(albumArtist);
        assertEquals(newArtistId, (long) albumArtist);
    }

    @Test
    public void updateArtistWithMultipleEntriesAlbum() {
        long artistId = artistsDao.insertArtist(new ArtistEntity(null, "test artist"));
        long albumId = albumsDao.insert(new AlbumEntity(artistId, null, "test album", 0, 0));
        long compositionId = compositionsDao.insert(composition(artistId, albumId, "test title"));
        compositionsDao.insert(composition(artistId, albumId, "test title2"));

        daoWrapper.updateArtist(compositionId, "test artist2");

        //check new artist
        long newArtistId = artistsDao.findArtistIdByName("test artist2");
        assertEquals(artistId, artistsDao.findArtistIdByName("test artist"));
        assertNotEquals(0, newArtistId);

        //check composition
        assertEquals(newArtistId, compositionsDao.getArtistId(compositionId));

        //check old album
        Long albumArtist = albumsDao.getAlbumEntity(albumId).getArtistId();
        assertNotNull(albumArtist);
        assertEquals(artistId, (long) albumArtist);

        //check new album
        long newAlbumId = compositionsDao.getAlbumId(compositionId);
        assertNotEquals(0, newAlbumId);
        AlbumEntity newAlbum = albumsDao.getAlbumEntity(newAlbumId);
        assertEquals("test album", newAlbum.getAlbumName());
        Long newAlbumArtist = newAlbum.getArtistId();
        assertNotNull(newAlbumArtist);
        assertEquals(newArtistId, (long) newAlbumArtist);
    }

    @Test
    public void updateArtistWitFullAlbumMove() {
        long artistId = artistsDao.insertArtist(new ArtistEntity(null, "test artist"));
        long albumId = albumsDao.insert(new AlbumEntity(artistId, null, "test album", 0, 0));
        long compositionId = compositionsDao.insert(composition(artistId, albumId, "test title"));
        long secondCompositionId = compositionsDao.insert(composition(artistId, albumId, "test title2"));

        daoWrapper.updateArtist(compositionId, "test artist2");
        daoWrapper.updateArtist(secondCompositionId, "test artist2");

        //check old artist
        assertEquals(0, artistsDao.findArtistIdByName("test artist"));

        //check new artist
        long newArtistId = artistsDao.findArtistIdByName("test artist2");
        assertNotEquals(0, newArtistId);

        //check composition
        assertEquals(newArtistId, compositionsDao.getArtistId(compositionId));
        assertEquals(newArtistId, compositionsDao.getArtistId(secondCompositionId));

        //check old album
        assertNull(albumsDao.getAlbumEntity(albumId));

        ////check new album
        long newAlbumId = compositionsDao.getAlbumId(compositionId);
        assertEquals(newAlbumId, compositionsDao.getAlbumId(secondCompositionId));

        AlbumEntity newAlbum = albumsDao.getAlbumEntity(newAlbumId);
        assertEquals("test album", newAlbum.getAlbumName());
        Long newAlbumArtist = newAlbum.getArtistId();
        assertNotNull(newAlbumArtist);
        assertEquals(newArtistId, (long) newAlbumArtist);
    }

    private static CompositionEntity composition(Long artistId, Long albumId, String title) {
        return new CompositionEntity(
                artistId,
                albumId,
                title,
                "test file path",
                100L,
                100L,
                null,
                new Date(),
                new Date(),
                null);
    }
}