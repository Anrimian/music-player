package com.github.anrimian.simplemusicplayer.data.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.github.anrimian.simplemusicplayer.data.database.entities.PlayQueueEntity;
import com.github.anrimian.simplemusicplayer.data.database.entities.ShuffledPlayQueueEntity;

import java.util.List;

@Dao
public interface PlayQueueDao {

    @Query("SELECT * FROM play_queue ORDER BY position")
    List<PlayQueueEntity> getPlayQueue();

    @Query("SELECT * FROM shuffled_play_queue ORDER BY position")
    List<ShuffledPlayQueueEntity> getShuffledPlayQueue();

    @Insert
    void insertPlayQueue(List<PlayQueueEntity> playQueueEntityList);

    @Update
    void updatePlayQueue(List<PlayQueueEntity> playQueueEntityList);

    @Query("DELETE FROM play_queue")
    void deletePlayQueue();

    @Query("DELETE FROM shuffled_play_queue")
    void deleteShuffledPlayQueue();

    @Insert
    void insertShuffledPlayQueue(List<ShuffledPlayQueueEntity> entities);

    @Query("DELETE FROM play_queue WHERE audioId = :audioId")
    void deletePlayQueueEntity(long audioId);

    @Query("DELETE FROM shuffled_play_queue WHERE audioId = :audioId")
    void deleteShuffledPlayQueueEntity(long audioId);

    @Query("UPDATE play_queue SET position = :newPosition WHERE audioId = :audioId")
    void updatePosition(long audioId, int newPosition);
}
