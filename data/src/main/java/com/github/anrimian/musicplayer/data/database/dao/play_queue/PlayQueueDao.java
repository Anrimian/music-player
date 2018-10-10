package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntityNew;
import com.github.anrimian.musicplayer.data.database.entities.ShuffledPlayQueueEntity;

import java.util.List;

@Dao
public interface PlayQueueDao {

    @Deprecated
    @Query("SELECT * FROM play_queue ORDER BY position")
    List<PlayQueueEntity> getPlayQueue();

    @Query("SELECT * FROM play_queue_new ORDER BY position")
    List<PlayQueueEntityNew> getPlayQueueNew();

    @Deprecated
    @Query("SELECT * FROM shuffled_play_queue ORDER BY position")
    List<ShuffledPlayQueueEntity> getShuffledPlayQueue();

    @Insert
    long[] insertPlayQueueNew(List<PlayQueueEntityNew> playQueueEntityList);

    @Deprecated
    @Insert
    long[] insertPlayQueue(List<PlayQueueEntity> playQueueEntityList);

    @Deprecated
    @Insert
    long[] insertShuffledPlayQueue(List<ShuffledPlayQueueEntity> entities);

    @Query("DELETE FROM play_queue_new")
    void deletePlayQueueNew();

    @Deprecated
    @Query("DELETE FROM play_queue")
    void deletePlayQueue();

    @Deprecated
    @Query("DELETE FROM shuffled_play_queue")
    void deleteShuffledPlayQueue();

    @Query("DELETE FROM play_queue_new WHERE id = :id")
    void deleteItemNew(long id);

    @Deprecated
    @Query("DELETE FROM play_queue WHERE id = :id")
    void deleteItem(long id);

    @Deprecated
    @Query("DELETE FROM shuffled_play_queue WHERE id = :id")
    void deleteShuffledItem(long id);

    @Query("DELETE FROM play_queue_new WHERE audioId = :id")
    void deleteComposition(long id);

    @Query("SELECT shuffledPosition FROM play_queue_new WHERE id = :id")
    int getShuffledPosition(long id);

    @Query("SELECT id FROM play_queue_new WHERE shuffledPosition = :shuffledPosition")
    long getQueueItemId(int shuffledPosition);

    @Query("UPDATE play_queue_new SET shuffledPosition = :shuffledPosition WHERE id = :id")
    void updateShuffledPosition(long id, int shuffledPosition);
}
