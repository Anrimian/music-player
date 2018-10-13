package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;

import java.util.List;

@Dao
public interface PlayQueueDao {

    @Query("SELECT * FROM play_queue ORDER BY position")
    List<PlayQueueEntity> getPlayQueue();

    @Insert
    List<Long> insertPlayQueue(List<PlayQueueEntity> playQueueEntityList);

    @Query("DELETE FROM play_queue")
    void deletePlayQueue();

    @Query("DELETE FROM play_queue WHERE id = :id")
    void deleteItem(long id);

    @Query("DELETE FROM play_queue WHERE audioId = :id")
    void deleteComposition(long id);

    @Query("SELECT shuffledPosition FROM play_queue WHERE id = :id")
    int getShuffledPosition(long id);

    @Query("SELECT id FROM play_queue WHERE shuffledPosition = :shuffledPosition")
    long getQueueItemId(int shuffledPosition);

    @Query("UPDATE play_queue SET shuffledPosition = :shuffledPosition WHERE id = :id")
    void updateShuffledPosition(long id, int shuffledPosition);
}
