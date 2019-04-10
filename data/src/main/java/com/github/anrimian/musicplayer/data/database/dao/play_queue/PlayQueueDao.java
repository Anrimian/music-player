package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface PlayQueueDao {

    @Query("SELECT * FROM play_queue ORDER BY position")
    List<PlayQueueEntity> getPlayQueue();

    @Insert
    List<Long> insertItems(List<PlayQueueEntity> playQueueEntityList);

    @Query("DELETE FROM play_queue")
    void deletePlayQueue();

    @Query("DELETE FROM play_queue WHERE id = :id")
    void deleteItem(long id);

    @Query("DELETE FROM play_queue WHERE audioId = :id")
    void deleteComposition(long id);

    @Query("SELECT position FROM play_queue WHERE id = :id")
    int getPosition(long id);

    @Query("SELECT shuffledPosition FROM play_queue WHERE id = :id")
    int getShuffledPosition(long id);

    @Query("SELECT id FROM play_queue WHERE shuffledPosition = :shuffledPosition")
    long getQueueItemId(int shuffledPosition);

    @Query("UPDATE play_queue SET shuffledPosition = :shuffledPosition WHERE id = :id")
    void updateShuffledPosition(long id, int shuffledPosition);

    @Query("UPDATE play_queue SET position = :position WHERE id = :itemId")
    void updateItemPosition(long itemId, int position);

    @Query("UPDATE play_queue SET position = position + :increaseBy WHERE position > :after")
    void increasePositions(int increaseBy, int after);

    @Query("UPDATE play_queue " +
            "SET shuffledPosition = shuffledPosition + :increaseBy " +
            "WHERE shuffledPosition > :after")
    void increaseShuffledPositions(int increaseBy, int after);

    @Query("SELECT MAX(position) FROM play_queue")
    int getLastPosition();
}
