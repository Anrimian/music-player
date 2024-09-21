package com.github.anrimian.musicplayer.data.database.dao.play_queue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import io.reactivex.rxjava3.core.Observable

@Dao
interface PlayQueueDao {

    @Query("SELECT * FROM play_queue ORDER BY position")
    fun getPlayQueue(): MutableList<PlayQueueEntity>

    @RawQuery(observedEntities = [
        PlayQueueEntity::class,
        ArtistEntity::class,
        CompositionEntity::class,
        AlbumEntity::class
    ])
    fun getPlayQueueObservable(query: SupportSQLiteQuery): Observable<List<PlayQueueItem>>

    @Query("SELECT id FROM play_queue WHERE position >= :position LIMIT 1")
    fun getItemIdAtPosition(position: Int): Long?

    @Query("SELECT id FROM play_queue WHERE shuffledPosition >= :position LIMIT 1")
    fun getItemIdAtShuffledPosition(position: Int): Long?

    @RawQuery(observedEntities = [
        PlayQueueEntity::class,
        ArtistEntity::class,
        CompositionEntity::class,
        AlbumEntity::class
    ])
    fun getItemObservable(query: SupportSQLiteQuery): Observable<Array<PlayQueueItem>>

    @Insert
    fun insertItems(playQueueEntityList: List<PlayQueueEntity>): LongArray

    @Insert
    fun insertItem(entity: PlayQueueEntity): Long

    @Query("DELETE FROM play_queue")
    fun deletePlayQueue()

    @Query("DELETE FROM play_queue WHERE id = :id")
    fun deleteItem(id: Long)

    @Query("DELETE FROM play_queue WHERE audioId in (:audioIds)")
    fun delete(audioIds: List<Long>)

    @Query("SELECT position FROM play_queue WHERE id = :id")
    fun getPosition(id: Long): Int

    @Query("SELECT shuffledPosition FROM play_queue WHERE id = :id")
    fun getShuffledPosition(id: Long): Int

    @Query("SELECT * FROM play_queue WHERE id = :id")
    fun getItem(id: Long): PlayQueueEntity

    @Query("SELECT position FROM play_queue WHERE id = :id")
    fun getPositionObservable(id: Long): Observable<Int>

    @Query("SELECT shuffledPosition FROM play_queue WHERE id = :id")
    fun getShuffledPositionObservable(id: Long): Observable<Int>

    @Query("""
        WITH item AS (SELECT position FROM play_queue WHERE id = :id) 
            SELECT CASE WHEN item.position IS NULL   
                THEN -1   
                ELSE (SELECT count() FROM play_queue WHERE position < item.position)
            END 
        AS result 
        FROM play_queue, item 
        LIMIT 1
    """)
    fun getIndexPositionObservable(id: Long): Observable<Int>

    @Query("""
        WITH item AS (SELECT shuffledPosition FROM play_queue WHERE id = :id) 
            SELECT CASE WHEN item.shuffledPosition IS NULL   
                THEN -1   
                ELSE (SELECT count() FROM play_queue WHERE shuffledPosition < item.shuffledPosition)
            END 
        AS result 
        FROM play_queue, item 
        LIMIT 1
    """)
    fun getShuffledIndexPositionObservable(id: Long): Observable<Int>

    @Query("UPDATE play_queue SET shuffledPosition = :shuffledPosition WHERE id = :id")
    fun updateShuffledPosition(id: Long, shuffledPosition: Int)

    @Query("UPDATE play_queue SET position = :position WHERE id = :itemId")
    fun updateItemPosition(itemId: Long, position: Int)

    @Query("UPDATE play_queue SET position = position + :increaseBy WHERE position = :position")
    fun increasePosition(increaseBy: Int, position: Int)

    @Query("""
        UPDATE play_queue 
        SET shuffledPosition = shuffledPosition + :increaseBy 
        WHERE shuffledPosition = :position
    """)
    fun increaseShuffledPosition(increaseBy: Int, position: Int)

    @Query("SELECT MAX(position) FROM play_queue")
    fun getLastPosition(): Int

    @Query("SELECT MAX(shuffledPosition) FROM play_queue")
    fun getLastShuffledPosition(): Int

    @Query("SELECT id FROM play_queue WHERE position = (SELECT MAX(position) FROM play_queue)")
    fun getLastItem(): Long

    @Query("""
        SELECT id 
        FROM play_queue 
        WHERE shuffledPosition = (SELECT MAX(shuffledPosition) FROM play_queue)
    """)
    fun getLastShuffledItem(): Long

    @Query("""
        SELECT id 
        FROM play_queue 
        WHERE position = (SELECT MIN(position) FROM play_queue)
    """)
    fun getFirstItem(): Long

    @Query("""
        SELECT id 
        FROM play_queue 
        WHERE shuffledPosition = (SELECT MIN(shuffledPosition) FROM play_queue)
    """)
    fun getFirstShuffledItem(): Long

    @Update
    fun update(list: List<PlayQueueEntity>)

    @Query("""
        SELECT id 
        FROM play_queue 
        WHERE position = (
            SELECT MIN(position)    
            FROM play_queue    
            WHERE position > (SELECT position FROM play_queue WHERE id = :currentItemId)
        )
    """)
    fun getNextQueueItemId(currentItemId: Long): Long?

    @Query("""
        SELECT id 
        FROM play_queue 
        WHERE shuffledPosition = (
            SELECT MIN(shuffledPosition)    
            FROM play_queue    
            WHERE shuffledPosition > (
                SELECT shuffledPosition FROM play_queue WHERE id = :currentItemId
            )
        )
    """)
    fun getNextShuffledQueueItemId(currentItemId: Long): Long?

    @Query("""
        SELECT id 
        FROM play_queue 
        WHERE position = (
            SELECT MAX(position)   
            FROM play_queue    
            WHERE position < (SELECT position FROM play_queue WHERE id = :currentItemId) 
                AND (SELECT corruptionType FROM compositions WHERE id = audioId) IS NULL)
    """)
    fun getPreviousQueueItemId(currentItemId: Long): Long?

    @Query("""
        SELECT id 
        FROM play_queue 
        WHERE shuffledPosition = (
            SELECT MAX(shuffledPosition)    
            FROM play_queue   
            WHERE shuffledPosition < (SELECT shuffledPosition FROM play_queue WHERE id = :currentItemId)      
                AND (SELECT corruptionType FROM compositions WHERE id = audioId) IS NULL)
    """)
    fun getPreviousShuffledQueueItemId(currentItemId: Long): Long?

    @Query("SELECT count() FROM play_queue")
    fun getPlayQueueSize(): Int

    @Query("SELECT count() FROM play_queue")
    fun getPlayQueueSizeObservable(): Observable<Int>

    @Query("""
        INSERT OR REPLACE INTO track_positions (
            queueItemId, 
            trackPosition, 
            writeTime
        ) VALUES (
            :itemId, 
            :position, 
            :time
        )
    """)
    fun insertTrackPosition(itemId: Long, position: Long, time: Long)

    @Query("""
        DELETE FROM track_positions 
        WHERE writeTime = (SELECT min(writeTime) FROM track_positions)
            AND (SELECT count() FROM track_positions) > 7
    """)
    fun deleteOldestTrackPosition()

    @Query("SELECT IFNULL(trackPosition, 0) FROM track_positions WHERE queueItemId = :itemId")
    fun getTrackPosition(itemId: Long): Long

    companion object {
        fun getCompositionQuery(useFileName: Boolean): String {
            return """
                SELECT play_queue.id AS itemId,
                ${CompositionsDao.getCompositionSelectionQuery(useFileName)}
                FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id 
                """
        }
    }
}
