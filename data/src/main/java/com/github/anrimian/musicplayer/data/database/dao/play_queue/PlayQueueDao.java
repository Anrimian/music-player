package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueCompositionDto;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueItemDto;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface PlayQueueDao {

    @Query("SELECT * FROM play_queue ORDER BY position")
    List<PlayQueueEntity> getPlayQueue();

    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "play_queue.position AS position," +
            "play_queue.shuffledPosition AS shuffledPosition," +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "compositions.artist AS artist, " +
            "compositions.title AS title, " +
            "compositions.album AS album, " +
            "compositions.filePath AS filePath, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id " +
            "WHERE play_queue.id = :id")
    PlayQueueCompositionDto getPlayQueueEntity(long id);

    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "play_queue.position AS position," +
            "play_queue.shuffledPosition AS shuffledPosition," +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "compositions.artist AS artist, " +
            "compositions.title AS title, " +
            "compositions.album AS album, " +
            "compositions.filePath AS filePath, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id ")
    Observable<List<PlayQueueCompositionDto>> getPlayQueueObservable();

    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "play_queue.position AS position," +
            "play_queue.shuffledPosition AS shuffledPosition," +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "compositions.artist AS artist, " +
            "compositions.title AS title, " +
            "compositions.album AS album, " +
            "compositions.filePath AS filePath, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id ")
    List<PlayQueueCompositionDto> getFullPlayQueue();


    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "compositions.artist AS artist, " +
            "compositions.title AS title, " +
            "compositions.album AS album, " +
            "compositions.filePath AS filePath, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id " +
            "ORDER BY position")
    List<PlayQueueItemDto> getPlayQueueInNormalOrder();

    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "compositions.artist AS artist, " +
            "compositions.title AS title, " +
            "compositions.album AS album, " +
            "compositions.filePath AS filePath, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id " +
            "ORDER BY shuffledPosition")
    List<PlayQueueItemDto> getPlayQueueInShuffledOrder();

    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "compositions.artist AS artist, " +
            "compositions.title AS title, " +
            "compositions.album AS album, " +
            "compositions.filePath AS filePath, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id " +
            "ORDER BY position")
    Observable<List<PlayQueueItemDto>> getPlayQueueInNormalOrderObservable();

    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "compositions.artist AS artist, " +
            "compositions.title AS title, " +
            "compositions.album AS album, " +
            "compositions.filePath AS filePath, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id " +
            "ORDER BY shuffledPosition")
    Observable<List<PlayQueueItemDto>> getPlayQueueInShuffledOrderObservable();

    @Insert
    List<Long> insertItems(List<PlayQueueEntity> playQueueEntityList);

    @Query("DELETE FROM play_queue")
    void deletePlayQueue();

    @Query("DELETE FROM play_queue WHERE id = :id")
    void deleteItem(long id);

    @Query("DELETE FROM play_queue WHERE audioId in (:audioIds)")
    void delete(List<Long> audioIds);

    @Query("SELECT position FROM play_queue WHERE id = :id")
    int getPosition(long id);

    @Query("SELECT shuffledPosition FROM play_queue WHERE id = :id")
    int getShuffledPosition(long id);

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

    @Update
    void update(List<PlayQueueEntity> list);
}
