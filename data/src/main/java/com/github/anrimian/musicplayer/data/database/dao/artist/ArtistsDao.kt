package com.github.anrimian.musicplayer.data.database.dao.artist;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ArtistsDao {

    @RawQuery(observedEntities = { ArtistEntity.class, CompositionEntity.class })
    Observable<List<Artist>> getAllObservable(SupportSQLiteQuery query);

    @Query("SELECT id as id," +
            "name as name, " +
            "(SELECT count() FROM compositions WHERE artistId = artists.id) as compositionsCount, " +
            "(SELECT count() FROM albums WHERE artistId = artists.id) as albumsCount " +
            "FROM artists " +
            "WHERE id = :artistId LIMIT 1")
    Observable<List<Artist>> getArtistObservable(long artistId);

    @RawQuery(observedEntities = { ArtistEntity.class, CompositionEntity.class, AlbumEntity.class })
    Observable<List<Composition>> getCompositionsByArtistObservable(SimpleSQLiteQuery query);

    @RawQuery
    List<Composition> getCompositionsByArtist(SimpleSQLiteQuery query);

    @Query("WITH artistCompositions(id) AS (SELECT id FROM compositions WHERE artistId = :artistId) " +
            "SELECT id FROM artistCompositions " +
            "UNION " +
            "SELECT compositions.id FROM compositions " +
            "WHERE (SELECT count(*) FROM artistCompositions) = 0 AND albumId IN(SELECT id FROM albums WHERE artistId = :artistId)")
    Single<List<Long>> getAllCompositionIdsByArtist(long artistId);

    @Query("SELECT id FROM albums WHERE artistId = :artistId")
    List<Long> getAllAlbumsWithArtist(long artistId);

    @Query("SELECT name FROM artists")
    String[] getAuthorNames();

    @Query("SELECT name FROM artists WHERE id = :artistId")
    String getAuthorName(long artistId);

    @Query("SELECT id FROM artists WHERE name = :name")
    Long findArtistIdByName(String name);

    @Query("INSERT OR REPLACE INTO artists (name) VALUES (:name)")
    long insertArtist(String name);

    @Query("DELETE FROM artists " +
            "WHERE id = :id " +
            "AND (SELECT count() FROM compositions WHERE artistId = artists.id) = 0 " +
            "AND (SELECT count() FROM albums WHERE artistId = artists.id) = 0")
    void deleteEmptyArtist(long id);

    @Query("DELETE FROM artists " +
            "WHERE (SELECT count() FROM compositions WHERE artistId = artists.id) = 0 " +
            "AND (SELECT count() FROM albums WHERE artistId = artists.id) = 0")
    void deleteEmptyArtists();

    @Query("UPDATE artists SET name = :name WHERE id = :id")
    void updateArtistName(String name, long id);

    @Query("UPDATE compositions " +
            "SET artistId = :newArtistId " +
            "WHERE artistId = :oldArtistId")
    void changeCompositionsArtist(long oldArtistId, long newArtistId);

    @Query("UPDATE compositions " +
            "SET dateModified = :dateModified " +
            "WHERE artistId = :artistId OR " +
            "albumId IN(SELECT id FROM albums WHERE artistId = :artistId)")
    void updateArtistCompositionsModifyTime(long artistId, Date dateModified);

    static String getCompositionsQuery(boolean useFileName) {
        return "SELECT " +
                CompositionsDao.getCompositionSelectionQuery(useFileName) +
                "FROM compositions " +
                "WHERE artistId = ?";
    }

    static String getAllCompositionsQuery(boolean useFileName) {
        return "WITH artistCompositions AS (SELECT " +
                CompositionsDao.getCompositionSelectionQuery(useFileName) +
                "FROM compositions " +
                "WHERE artistId = ?) " +
                "SELECT * FROM artistCompositions " +
                "UNION " +
                "SELECT " +
                CompositionsDao.getCompositionSelectionQuery(useFileName) +
                "FROM compositions " +
                "WHERE (SELECT count(*) FROM artistCompositions) = 0 AND albumId IN(SELECT id FROM albums WHERE artistId = ?)";
    }
}
