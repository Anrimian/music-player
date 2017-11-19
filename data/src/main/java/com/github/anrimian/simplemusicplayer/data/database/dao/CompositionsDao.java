package com.github.anrimian.simplemusicplayer.data.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.github.anrimian.simplemusicplayer.data.database.models.CompositionEntity;

import java.util.List;

/**
 * Created on 18.11.2017.
 */

@Dao
public interface CompositionsDao {

    @Query("SELECT * FROM compositions")
    List<CompositionEntity> getCurrentPlayList();

    @Insert
    void insertAll(List<CompositionEntity> compositions);

    @Query("DELETE FROM compositions")
    void deleteCurrentPlayList();
}
