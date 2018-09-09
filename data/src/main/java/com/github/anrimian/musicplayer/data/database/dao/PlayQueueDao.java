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

    @Deprecated
    @Query("SELECT * FROM play_queue ORDER BY position")
    List<PlayQueueEntity> getPlayQueue();

    @Query("SELECT * FROM play_queue ORDER BY position")
    Flowable<List<PlayQueueEntity>> getPlayQueueObservable();

    @Deprecated
    @Query("SELECT * FROM shuffled_play_queue ORDER BY position")
    List<ShuffledPlayQueueEntity> getShuffledPlayQueue();

    @Query("SELECT * FROM shuffled_play_queue ORDER BY position")
    Flowable<List<ShuffledPlayQueueEntity>> getShuffledPlayQueueObservable();

    @Insert
    void insertPlayQueue(List<PlayQueueEntity> playQueueEntityList);

    @Query("DELETE FROM play_queue")
    void deletePlayQueue();

    @Query("DELETE FROM shuffled_play_queue")
    void deleteShuffledPlayQueue();

    @Insert
    void insertShuffledPlayQueue(List<ShuffledPlayQueueEntity> entities);

    @Query("DELETE FROM play_queue WHERE audioId = :audioId")
    void deleteComposition(long audioId);

    @Query("DELETE FROM shuffled_play_queue WHERE audioId = :audioId")
    void deleteShuffledComposition(long audioId);

    @Query("DELETE FROM play_queue WHERE id = :id")
    void deleteQueueItem(long id);

    @Query("DELETE FROM shuffled_play_queue WHERE id = :id")
    void deleteShuffledQueueItem(long id);

    @Query("UPDATE play_queue SET position = :newPosition WHERE id = :id")
    void updatePosition(long id, int newPosition);
}
