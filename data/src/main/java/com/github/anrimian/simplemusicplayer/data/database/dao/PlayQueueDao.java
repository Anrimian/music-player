package com.github.anrimian.simplemusicplayer.data.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.github.anrimian.simplemusicplayer.data.database.models.CompositionEntity;

import java.util.List;

import io.reactivex.Single;

/**
 * Created on 18.11.2017.
 */

@Dao
public interface PlayQueueDao {

    @Query("SELECT * FROM CURRENT_PLAY_LIST")
    Single<List<CompositionEntity>> getCurrentPlayList();

    @Insert
    void insertAll(CompositionEntity... compositions);

    @Query("DELETE FROM CURRENT_PLAY_LIST")
    void deleteCurrentPlayList();
}
