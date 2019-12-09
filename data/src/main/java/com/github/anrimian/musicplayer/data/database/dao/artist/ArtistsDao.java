package com.github.anrimian.musicplayer.data.database.dao.artist;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtist;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface ArtistsDao {

    @Query("SELECT id FROM artists WHERE storageId = :storageId")
    Long selectIdByStorageId(long storageId);

    @Query("SELECT storageId as id," +
            "name as artist " +
            "FROM artists")
    List<StorageArtist> selectAllAsStorageArtists();

    @Insert
    void insertAll(List<ArtistEntity> artists);

    @Query("SELECT id as id," +
            "name as name, " +
            "(SELECT count() FROM compositions WHERE artistId = artists.id) as compositionsCount " +
            "FROM artists " +
            "ORDER BY id DESC")
    Observable<List<Artist>> getAllObservable();

    @Query("SELECT id as id," +
            "name as name, " +
            "(SELECT count() FROM compositions WHERE artistId = artists.id) as compositionsCount " +
            "FROM artists " +
            "WHERE id = :artistId LIMIT 1")
    Observable<List<Artist>> getArtistObservable(long artistId);

    @Query("SELECT " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
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
            "WHERE artistId = :artistId")
    Observable<List<Composition>> getCompositionsByArtistObservable(long artistId);

    @Query("SELECT " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
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
            "WHERE artistId = :artistId")
    List<Composition> getCompositionsByArtist(long artistId);

    @Query("SELECT name FROM artists")
    String[] getAuthorNames();

    @Query("SELECT id FROM artists WHERE name = :author")
    Long findArtistIdByName(String author);

    @Insert
    long insertArtist(ArtistEntity artistEntity);

    @Query("DELETE FROM artists " +
            "WHERE id = :id " +
            "AND (SELECT count() FROM compositions WHERE artistId = artists.id) = 0 " +
            "AND (SELECT count() FROM albums WHERE artistId = artists.id) = 0")
    void deleteEmptyArtist(long id);

    @Query("UPDATE artists SET name = :name WHERE id = :id")
    void updateArtistName(String name, long id);
}
