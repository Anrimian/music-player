package com.github.anrimian.simplemusicplayer.data.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.github.anrimian.simplemusicplayer.data.database.models.PlayQueueEntity;

import java.util.List;

@Dao
public interface PlayQueueDao {

    @Query("SELECT * FROM play_queue")
    List<PlayQueueEntity> getPlayQueue();

    @Insert
    void setPlayQueue(List<PlayQueueEntity> playQueueEntityList);

    @Update
    void updatePlayQueue(List<PlayQueueEntity> playQueueEntityList);

    @Query("DELETE FROM play_queue")
    void deletePlayQueue();

    @Query("DELETE FROM play_queue WHERE id = :id")
    void deletePlayQueueEntity(long id);

    @Query("UPDATE play_queue SET shuffledPosition = :newPosition WHERE id = :id")
    void updateShuffledPosition(long id, int newPosition);

    @Query("UPDATE play_queue SET position = :newPosition WHERE id = :id")
    void updatePosition(long id, int newPosition);
}
