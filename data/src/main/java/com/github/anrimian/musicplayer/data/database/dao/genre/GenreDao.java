package com.github.anrimian.musicplayer.data.database.dao.genre;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntity;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntryEntity;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenre;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenreItem;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
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
    long insert(GenreEntity entity);

    @Insert
    void insertGenreEntities(List<GenreEntryEntity> entities);

    @Insert
    void insertGenreEntry(GenreEntryEntity entity);

    @Query("SELECT id as id," +
            "name as name, " +
            "(SELECT count() FROM genre_entries WHERE genreId = genres.id) as compositionsCount, " +
            "(SELECT sum(duration) FROM compositions WHERE compositions.id IN (SELECT audioId FROM genre_entries WHERE genreId = genres.id)) as totalDuration " +
            "FROM genres")
    Observable<List<Genre>> getAllObservable();

    @Query("SELECT " +
            "(SELECT artistName FROM artists WHERE id = artistId) as artist, " +
            "title as title, " +
            "(SELECT albumName FROM albums WHERE id = albumId) as album, " +
            "filePath as filePath, " +
            "duration as duration, " +
            "size as size, " +
            "id as id, " +
            "storageId as storageId, " +
            "dateAdded as dateAdded, " +
            "dateModified as dateModified, " +
            "corruptionType as corruptionType " +
            "FROM compositions " +
            "WHERE id IN (SELECT audioId FROM genre_entries WHERE genreId = :genreId)")
    Observable<List<Composition>> getCompositionsInGenre(long genreId);

    @Query("SELECT " +
            "id as dbId, " +
            "storageId as storageId " +
            "FROM genres WHERE genres.storageId IS NOT NULL")
    List<IdPair> getGenresIds();

    @Query("SELECT id as id," +
            "name as name, " +
            "(SELECT count() FROM genre_entries WHERE genreId = genres.id) as compositionsCount, " +
            "(SELECT sum(duration) FROM compositions WHERE compositions.id IN (SELECT audioId FROM genre_entries WHERE genreId = genres.id)) as totalDuration " +
            "FROM genres " +
            "WHERE id = :genreId LIMIT 1")
    Observable<List<Genre>> getGenreObservable(long genreId);

    @Query("SELECT id FROM genres WHERE name = :name")
    Long findGenre(String name);

    @Query("SELECT genreId FROM genre_entries WHERE audioId = :compositionId")
    Long[] getGenresByCompositionId(long compositionId);

    @Query("DELETE FROM genres " +
            "WHERE id IN(:ids) AND (SELECT count() FROM genre_entries WHERE genreId IN(:ids)) = 0")
    void deleteEmptyGenre(Long[] ids);

    @Query("DELETE FROM genre_entries WHERE audioId = :compositionId AND genreId IN(:genreIds)")
    void removeGenreEntry(long compositionId, Long[] genreIds);
}
