package com.github.anrimian.simplemusicplayer.data.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.github.anrimian.simplemusicplayer.data.database.models.CompositionItemEntity;

import java.util.List;

/**
 * Created on 18.11.2017.
 */

@Dao
public interface CompositionsDao {

    @Query("SELECT * FROM current_play_list")
    List<CompositionItemEntity> getCurrentPlayList();

    @Insert
    void setCurrentPlayList(List<CompositionItemEntity> compositionItemEntity);

    @Query("DELETE FROM current_play_list")
    void deleteCurrentPlayList();
}
