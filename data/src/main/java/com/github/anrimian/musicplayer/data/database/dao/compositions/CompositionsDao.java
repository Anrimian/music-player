package com.github.anrimian.musicplayer.data.database.dao.compositions;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface CompositionsDao {

    @Query("SELECT * FROM compositions")
    Flowable<List<CompositionEntity>> getAllObservable();

    @Query("SELECT * FROM compositions")
    List<CompositionEntity> getAll();

    @Insert
    long insert(CompositionEntity entity);

    @Insert
    void insert(List<CompositionEntity> entities);

    @Update
    void update(List<CompositionEntity> entities);

    @Query("DELETE FROM compositions WHERE id = :id")
    void delete(long id);

    @Query("DELETE FROM compositions WHERE id in (:ids)")
    void delete(List<Long> ids);

    @Query("DELETE FROM compositions")
    void deleteAll();

    @Query("UPDATE compositions SET filePath = :filePath WHERE id = :id")
    void updateFilePath(long id, String filePath);

    @Query("UPDATE compositions SET artist = :artist WHERE id = :id")
    void updateArtist(long id, String artist);

    @Query("UPDATE compositions SET title = :title WHERE id = :id")
    void updateTitle(long id, String title);
}
