package com.github.anrimian.musicplayer.data.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.github.anrimian.musicplayer.data.database.entities.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.ShuffledPlayQueueEntity;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface PlayQueueDao {

    @Query("SELECT * FROM play_queue ORDER BY position")
    List<PlayQueueEntity> getPlayQueue();

    @Query("SELECT * FROM shuffled_play_queue ORDER BY position")
    List<ShuffledPlayQueueEntity> getShuffledPlayQueue();

    @Insert
    long[] insertPlayQueue(List<PlayQueueEntity> playQueueEntityList);

    @Insert
    long[] insertShuffledPlayQueue(List<ShuffledPlayQueueEntity> entities);

    @Query("DELETE FROM play_queue")
    void deletePlayQueue();

    @Query("DELETE FROM shuffled_play_queue")
    void deleteShuffledPlayQueue();

    @Query("DELETE FROM play_queue WHERE id = :id")
    void deleteItem(long id);

    @Query("DELETE FROM shuffled_play_queue WHERE id = :id")
    void deleteShuffledItem(long id);
}
