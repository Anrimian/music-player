package com.github.anrimian.musicplayer.data.database.dao.genre;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntity;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntryEntity;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenre;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenreItem;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface GenreDao {

    @Query("SELECT id FROM genres WHERE storageId = :storageId")
    Long selectIdByStorageId(long storageId);

    @Query("SELECT storageId as id," +
            "name as name " +
            "FROM genres")
    List<StorageGenre> selectAllAsStorageGenres();

    @Query("SELECT storageId as id," +
            "audioId as audioId " + //wrong id
            "FROM genre_entries " +
            "WHERE genreId = :genreId")
    List<StorageGenreItem> selectAllAsStorageGenreItems(long genreId);

    @Insert
    void insertAll(List<GenreEntity> entities);

    @Insert
    void insertGenreEntities(List<GenreEntryEntity> entities);

    @Query("SELECT id as id," +
            "name as name, " +
            "(SELECT count() FROM genre_entries WHERE genreId = genres.id) as compositionsCount, " +
            "(SELECT sum(duration) FROM compositions WHERE compositions.id IN (SELECT audioId FROM genre_entries WHERE genreId = genres.id)) as totalDuration " +
            "FROM genres")
    Observable<List<Genre>> getAllObservable();

    @Query("SELECT * " +
            "FROM compositions " +
            "WHERE id IN (SELECT audioId FROM genre_entries WHERE genreId = :genreId)")
    Observable<List<CompositionEntity>> getCompositionsInGenre(long genreId);

    @Query("SELECT " +
            "id as dbId, " +
            "storageId as storageId " +
            "FROM genres WHERE genres.storageId IS NOT NULL")
    List<IdPair> getGenresIds();

}
