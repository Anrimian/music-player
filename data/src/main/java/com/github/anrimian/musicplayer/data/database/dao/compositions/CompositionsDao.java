package com.github.anrimian.musicplayer.data.database.dao.compositions;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

@Dao
public interface CompositionsDao {

    @Query("SELECT * FROM compositions")
    Observable<List<CompositionEntity>> getAllObservable();

    @Query("SELECT * FROM compositions WHERE id = :id")
    Observable<CompositionEntity> getCompoisitionObservable(long id);

//    @Query("SELECT * FROM compositions " +
//            "ORDER BY :orderField")
//    Observable<List<CompositionEntity>> getAllObservable(String orderField,
//                                                       String asc);

    @RawQuery(observedEntities = CompositionEntity.class)
    Observable<List<CompositionEntity>> getAllObservable(SupportSQLiteQuery query);

    @Query("SELECT * FROM compositions")
    List<CompositionEntity> getAll();

    @Query("SELECT " +
            "compositions.artist as artist, " +
            "compositions.title as title, " +
            "compositions.album as album, " +
            "compositions.filePath as filePath, " +
            "compositions.duration as duration, " +
            "compositions.size as size, " +
            "compositions.storageId as id, " +
            "compositions.dateAdded as dateAdded, " +
            "compositions.dateModified as dateModified, " +
            "(SELECT storageId FROM artists WHERE id = compositions.artist) as artistId " +
            "FROM compositions WHERE storageId NOTNULL")
    List<StorageComposition> selectAllAsStorageCompositions();

    @Insert
    long insert(CompositionEntity entity);

    @Insert
    void insert(List<CompositionEntity> entities);

    @Update
    void update(List<CompositionEntity> entities);

    @Query("UPDATE compositions SET " +
            "artist = :artist, " +
            "title = :title, " +
            "album = :album, " +
            "filePath = :filePath, " +
            "duration = :duration, " +
            "size = :size, " +
            "dateAdded = :dateAdded, " +
            "dateModified = :dateModified " +
            "WHERE storageId = :storageId")
    void update(String artist,
                String title,
                String album,
                String filePath,
                long duration,
                long size,
                Date dateAdded,
                Date dateModified,
                long storageId);

    @Query("DELETE FROM compositions WHERE id = :id")
    void delete(long id);

    @Query("DELETE FROM compositions WHERE id in (:ids)")
    void delete(List<Long> ids);

    @Query("DELETE FROM compositions WHERE storageId in (:ids)")
    void deleteByStorageId(List<Long> ids);

    @Query("DELETE FROM compositions")
    void deleteAll();

    @Query("UPDATE compositions SET filePath = :filePath WHERE id = :id")
    void updateFilePath(long id, String filePath);

    @Query("UPDATE compositions SET artist = :artist WHERE id = :id")
    void updateArtist(long id, String artist);

    @Query("UPDATE compositions SET title = :title WHERE id = :id")
    void updateTitle(long id, String title);

    @Query("SELECT id FROM compositions WHERE storageId = :storageId")
    long selectIdByStorageId(long storageId);
}
