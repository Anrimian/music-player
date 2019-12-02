package com.github.anrimian.musicplayer.data.database.dao.albums;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface AlbumsDao {

    @Query("SELECT id FROM albums WHERE storageId = :storageId")
    Long selectIdByStorageId(long storageId);

    @Query("SELECT storageId as id," +
            "name as album," +
            "(SELECT artistName FROM artists WHERE artists.id = artistId) as artist," +
            "(SELECT storageId FROM artists WHERE artists.id = artistId) as artistId," +
            "firstYear as firstYear," +
            "lastYear as lastYear " +
            "FROM albums")
    List<StorageAlbum> selectAllAsStorageAlbums();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AlbumEntity> artists);

    @Insert
    long insert(AlbumEntity entity);

    @Query("SELECT id as id," +
            "storageId as storageId, " +
            "name as name, " +
            "(SELECT artistName FROM artists WHERE artists.id = albums.artistId) as artist, " +
            "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
            "FROM albums")
    Observable<List<Album>> getAllObservable();

    @Query("SELECT " +
            "(SELECT artistName FROM artists WHERE id = artistId) as artist, " +
            "title as title, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "filePath as filePath, " +
            "duration as duration, " +
            "size as size, " +
            "id as id, " +
            "storageId as storageId, " +
            "dateAdded as dateAdded, " +
            "dateModified as dateModified, " +
            "corruptionType as corruptionType " +
            "FROM compositions " +
            "WHERE albumId = :albumId")
    Observable<List<Composition>> getCompositionsInAlbumObservable(long albumId);

    @Query("SELECT " +
            "(SELECT artistName FROM artists WHERE id = artistId) as artist, " +
            "title as title, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "filePath as filePath, " +
            "duration as duration, " +
            "size as size, " +
            "id as id, " +
            "storageId as storageId, " +
            "dateAdded as dateAdded, " +
            "dateModified as dateModified, " +
            "corruptionType as corruptionType " +
            "FROM compositions " +
            "WHERE albumId = :albumId")
    List<Composition> getCompositionsInAlbum(long albumId);

    @Query("SELECT id as id," +
            "storageId as storageId, " +
            "name as name, " +
            "(SELECT artistName FROM artists WHERE artists.id = albums.artistId) as artist, " +
            "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
            "FROM albums " +
            "WHERE albums.artistId = :artistId")
    Observable<List<Album>> getAllAlbumsForArtist(long artistId);

    @Query("SELECT id as id," +
            "storageId as storageId, " +
            "name as name, " +
            "(SELECT artistName FROM artists WHERE artists.id = albums.artistId) as artist, " +
            "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
            "FROM albums " +
            "WHERE id = :albumId LIMIT 1")
    Observable<List<Album>> getAlbumObservable(long albumId);

    @Query("SELECT id as id," +
            "storageId as storageId, " +
            "name as name, " +
            "(SELECT artistName FROM artists WHERE artists.id = albums.artistId) as artist, " +
            "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
            "FROM albums " +
            "WHERE id = :albumId LIMIT 1")
    Album getAlbum(long albumId);

    @Query("SELECT id FROM albums " +
            "WHERE (artistId = :artistId OR (artistId IS NULL AND :artistId IS NULL)) " +
            "AND name = :name")
    Long findAlbum(Long artistId, String name);

    @Query("UPDATE albums SET artistId = :artistId WHERE id = :albumId")
    void setAuthorId(long albumId, long artistId);

    @Query("SELECT * FROM albums WHERE id = :id")
    AlbumEntity getAlbumEntity(long id);

    @Query("DELETE FROM albums " +
            "WHERE id = :id AND (SELECT count() FROM compositions WHERE albumId = albums.id) = 0")
    void deleteEmptyAlbum(long id);

    @Query("SELECT name FROM albums")
    String[] getAlbumNames();

    @Query("UPDATE albums SET name = :name WHERE id = :id")
    void updateAlbumName(String name, long id);
}
