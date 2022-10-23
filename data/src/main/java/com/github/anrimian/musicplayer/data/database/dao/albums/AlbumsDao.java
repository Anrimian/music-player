package com.github.anrimian.musicplayer.data.database.dao.albums;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface AlbumsDao {

    @Insert
    long insert(AlbumEntity entity);

    @RawQuery(observedEntities = { ArtistEntity.class, CompositionEntity.class, AlbumEntity.class })
    Observable<List<Album>> getAllObservable(SupportSQLiteQuery query);

    @RawQuery(observedEntities = { ArtistEntity.class, CompositionEntity.class, AlbumEntity.class })
    Observable<List<Composition>> getCompositionsInAlbumObservable(SimpleSQLiteQuery query);

    @Query("SELECT id FROM compositions WHERE albumId = :albumId")
    Single<List<Long>> getCompositionIdsInAlbum(long albumId);

    @Query("SELECT id as id," +
            "name as name, " +
            "(SELECT name FROM artists WHERE artists.id = albums.artistId) as artist, " +
            "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
            "FROM albums " +
            "WHERE albums.artistId = :artistId")
    Observable<List<Album>> getAllAlbumsForArtistObservable(long artistId);

    @Query("SELECT id as id," +
            "name as name, " +
            "(SELECT name FROM artists WHERE artists.id = albums.artistId) as artist, " +
            "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
            "FROM albums " +
            "WHERE id = :albumId LIMIT 1")
    Observable<List<Album>> getAlbumObservable(long albumId);

    @Query("SELECT id as id," +
            "name as name, " +
            "(SELECT name FROM artists WHERE artists.id = albums.artistId) as artist, " +
            "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
            "FROM albums " +
            "WHERE id = :albumId LIMIT 1")
    Album getAlbum(long albumId);

    @Query("SELECT name as name FROM albums WHERE id = :albumId LIMIT 1")
    String getAlbumName(long albumId);

    @Query("SELECT id FROM albums " +
            "WHERE (artistId = :artistId OR (artistId IS NULL AND :artistId IS NULL)) " +
            "AND name = :name")
    Long findAlbum(Long artistId, String name);

    @Query("UPDATE albums SET artistId = :artistId WHERE id = :albumId")
    void setAuthorId(long albumId, Long artistId);

    @Query("SELECT * FROM albums WHERE id = :id")
    AlbumEntity getAlbumEntity(long id);

    @Query("DELETE FROM albums " +
            "WHERE id = :id AND (SELECT count() FROM compositions WHERE albumId = albums.id) = 0")
    void deleteEmptyAlbum(long id);

    @Query("DELETE FROM albums " +
            "WHERE (SELECT count() FROM compositions WHERE albumId = albums.id) = 0")
    void deleteEmptyAlbums();

    @Query("SELECT name FROM albums")
    String[] getAlbumNames();

    @Query("UPDATE albums SET name = :name WHERE id = :id")
    void updateAlbumName(String name, long id);

    @Query("SELECT artistId FROM albums WHERE id = :albumId")
    Long getArtistId(long albumId);

    @Query("UPDATE compositions SET albumId = :newAlbumId WHERE id IN (" +
            "SELECT id FROM compositions WHERE albumId = :oldAlbumId" +
            ")")
    void changeCompositionsAlbum(long oldAlbumId, long newAlbumId);

    @Query("UPDATE compositions SET dateModified = :dateModified WHERE id IN (" +
            "SELECT id FROM compositions WHERE albumId = :albumId" +
            ")")
    void updateAlbumCompositionsModifyTime(long albumId, Date dateModified);

    static String getCompositionsQuery(boolean useFileName) {
        return "SELECT " +
                CompositionsDao.getCompositionSelectionQuery(useFileName) +
                "FROM compositions " +
                "WHERE albumId = ? " +
                "ORDER BY fileName";
    }

}
