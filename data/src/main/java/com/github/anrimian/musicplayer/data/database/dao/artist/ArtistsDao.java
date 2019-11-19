package com.github.anrimian.musicplayer.data.database.dao.artist;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtist;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface ArtistsDao {

    @Query("SELECT id FROM artists WHERE storageId = :storageId")
    Long selectIdByStorageId(long storageId);

    @Query("SELECT storageId as id," +
            "artistName as artist " +
            "FROM artists")
    List<StorageArtist> selectAllAsStorageArtists();

    @Insert
    void insertAll(List<ArtistEntity> artists);

    @Query("SELECT id as id," +
            "artistName as name, " +
            "(SELECT count() FROM compositions WHERE artistId = artists.id) as compositionsCount " +
            "FROM artists " +
            "ORDER BY id DESC")
    Observable<List<Artist>> getAllObservable();

    @Query("SELECT id as id," +
            "artistName as name, " +
            "(SELECT count() FROM compositions WHERE artistId = artists.id) as compositionsCount " +
            "FROM artists " +
            "WHERE id = :artistId LIMIT 1")
    Observable<List<Artist>> getArtistObservable(long artistId);

    @Query("SELECT * " +
            "FROM compositions " +
            "WHERE artistId = :artist")
    Observable<List<CompositionEntity>> getCompositionsByArtist(long artist);

    @Query("SELECT id FROM artists WHERE artistName = :author")
    long findArtistIdByName(String author);

    @Insert
    long insertArtist(ArtistEntity artistEntity);

    @Query("DELETE FROM artists " +
            "WHERE id = :id AND (SELECT count() FROM compositions WHERE artistId = artists.id) = 0")
    void deleteEmptyArtists(long id);
}
