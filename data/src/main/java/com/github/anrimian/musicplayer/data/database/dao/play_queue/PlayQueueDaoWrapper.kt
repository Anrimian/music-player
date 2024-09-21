package com.github.anrimian.musicplayer.data.database.dao.play_queue

import android.database.sqlite.SQLiteCantOpenDatabaseException
import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity
import com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import io.reactivex.rxjava3.core.Observable
import java.util.Random

/**
 * Created on 02.07.2018.
 */
class PlayQueueDaoWrapper(
    private val libraryDatabase: LibraryDatabase,
    private val playQueueDao: PlayQueueDao
) {

    private var deletedItem: PlayQueueEntity? = null

    fun getPlayQueueObservable(
        isRandom: Boolean,
        useFileName: Boolean
    ): Observable<List<PlayQueueItem>> {
        var query = PlayQueueDao.getCompositionQuery(useFileName)
        query += if (isRandom) "ORDER BY shuffledPosition" else "ORDER BY position"
        val sqlQuery = SimpleSQLiteQuery(query)
        return playQueueDao.getPlayQueueObservable(sqlQuery)
    }

    fun reshuffleQueue(currentItemId: Long) {
        libraryDatabase.runInTransaction {
            val list = playQueueDao.getPlayQueue()
            if (list.isEmpty()) {
                return@runInTransaction
            }

            list.shuffle()

            val firstItemId = list[0].id
            var currentItemPosition = -1
            for (i in list.indices) {
                val entity = list[i]

                if (entity.id == currentItemId) {
                    currentItemPosition = i
                }
                entity.shuffledPosition = i
            }
            if (currentItemPosition != -1 && firstItemId != currentItemId) {
                list[currentItemPosition].shuffledPosition = 0
                list[0].shuffledPosition = currentItemPosition
            }

            playQueueDao.deletePlayQueue()
            playQueueDao.insertItems(list)
        }
    }

    fun insertNewPlayQueue(
        compositionIds: List<Long>,
        randomPlayingEnabled: Boolean,
        startPosition: Int
    ): Long {
        return libraryDatabase.runInTransaction<Long> {
            val shuffledList = ArrayList(compositionIds)
            val randomSeed = System.nanoTime()
            shuffledList.shuffle(Random(randomSeed))

            val shuffledPositionList = ArrayList<Int>(compositionIds.size)
            for (i in compositionIds.indices) {
                shuffledPositionList.add(i)
            }
            shuffledPositionList.shuffle(Random(randomSeed))

            val entities = ArrayList<PlayQueueEntity>(compositionIds.size)
            var shuffledStartPosition = 0
            for (i in compositionIds.indices) {
                val id = compositionIds[i]
                val playQueueEntity = PlayQueueEntity()
                playQueueEntity.audioId = id
                playQueueEntity.position = i
                val shuffledPosition = shuffledPositionList[i]
                playQueueEntity.shuffledPosition = shuffledPosition

                if (startPosition != Constants.NO_POSITION && i == startPosition) {
                    shuffledStartPosition = shuffledPosition
                }

                entities.add(playQueueEntity)
            }

            playQueueDao.deletePlayQueue()
            playQueueDao.insertItems(entities)
            return@runInTransaction if (randomPlayingEnabled) {
                playQueueDao.getItemIdAtShuffledPosition(shuffledStartPosition)!!
            } else {
                playQueueDao.getItemIdAtPosition(if (startPosition == Constants.NO_POSITION) 0 else startPosition)!!
            }
        }
    }

    fun getItemObservable(id: Long, useFileName: Boolean): Observable<Optional<PlayQueueItem>> {
        var query = PlayQueueDao.getCompositionQuery(useFileName)
        query += "WHERE itemId = ? LIMIT 1"
        val sqlQuery = SimpleSQLiteQuery(query, arrayOf<Any>(id))
        return playQueueDao.getItemObservable(sqlQuery)
            .map { itemArray -> Optional(itemArray.firstOrNull()) }
    }

    fun deleteItem(itemId: Long) {
        deletedItem = playQueueDao.getItem(itemId)
        playQueueDao.deleteItem(itemId)
    }

    fun restoreDeletedItem(): Long? {
        if (deletedItem != null) {
            return playQueueDao.insertItem(deletedItem!!)
        }
        return null
    }

    fun swapItems(firstItem: PlayQueueItem, secondItem: PlayQueueItem, shuffleMode: Boolean) {
        libraryDatabase.runInTransaction {
            val firstId = firstItem.itemId
            val secondId = secondItem.itemId
            if (shuffleMode) {
                val firstPosition = playQueueDao.getShuffledPosition(firstId)
                val secondPosition = playQueueDao.getShuffledPosition(secondId)

                playQueueDao.updateShuffledPosition(secondId, Int.MIN_VALUE)
                playQueueDao.updateShuffledPosition(firstId, secondPosition)
                playQueueDao.updateShuffledPosition(secondId, firstPosition)
            } else {
                val firstPosition = playQueueDao.getPosition(firstId)
                val secondPosition = playQueueDao.getPosition(secondId)

                playQueueDao.updateItemPosition(secondId, Int.MIN_VALUE)
                playQueueDao.updateItemPosition(firstId, secondPosition)
                playQueueDao.updateItemPosition(secondId, firstPosition)
            }
        }
    }

    fun addCompositionsToEndQueue(compositions: List<Composition>): Long {
        return libraryDatabase.runInTransaction<Long> {
            val positionToInsert = playQueueDao.getLastPosition() + 1
            val shuffledPositionToInsert = playQueueDao.getLastShuffledPosition() + 1
            val entities = toEntityList(compositions, positionToInsert, shuffledPositionToInsert)
            val ids = playQueueDao.insertItems(entities)
            return@runInTransaction ids[0]
        }
    }

    fun addCompositionsToQueue(compositions: List<Composition>, currentItemId: Long): Long {
        return libraryDatabase.runInTransaction<Long> {
            var positionToInsert = 0
            var shuffledPositionToInsert = 0
            if (currentItemId != UiStateRepositoryImpl.NO_ITEM) {
                val currentPosition = playQueueDao.getPosition(currentItemId)
                val currentShuffledPosition = playQueueDao.getShuffledPosition(currentItemId)

                val increaseBy = compositions.size
                val lastPosition = playQueueDao.getLastPosition()
                for (pos in lastPosition downTo currentPosition + 1) {
                    playQueueDao.increasePosition(increaseBy, pos)
                }
                val lastShuffledPosition = playQueueDao.getLastShuffledPosition()
                for (pos in lastShuffledPosition downTo currentShuffledPosition + 1) {
                    playQueueDao.increaseShuffledPosition(increaseBy, pos)
                }

                positionToInsert = currentPosition + 1
                shuffledPositionToInsert = currentShuffledPosition + 1
            }

            val entities = toEntityList(compositions, positionToInsert, shuffledPositionToInsert)
            val ids = playQueueDao.insertItems(entities)
            return@runInTransaction ids[0]
        }
    }

    fun getPosition(id: Long, isShuffle: Boolean): Int {
        return if (isShuffle) {
            playQueueDao.getShuffledPosition(id)
        } else {
            playQueueDao.getPosition(id)
        }
    }

    fun getLastPosition(isShuffled: Boolean): Int {
        return if (isShuffled) {
            playQueueDao.getLastShuffledPosition()
        } else {
            playQueueDao.getLastPosition()
        }
    }

    fun getPositionObservable(id: Long, isShuffle: Boolean): Observable<Int> {
        val observable = if (isShuffle) {
            playQueueDao.getShuffledPositionObservable(id)
        } else {
            playQueueDao.getPositionObservable(id)
        }
        return observable
            .retry(
                DB_OBSERVABLE_RETRY_COUNT.toLong(),
                { t -> t is SQLiteCantOpenDatabaseException }
            )
            .distinctUntilChanged()
    }

    fun getIndexPositionObservable(id: Long, isShuffle: Boolean): Observable<Int> {
        val observable = if (isShuffle) {
            playQueueDao.getShuffledIndexPositionObservable(id)
        } else {
            playQueueDao.getIndexPositionObservable(id)
        }
        return observable.filter { pos -> pos >= 0 }
            .distinctUntilChanged()
    }

    fun getNextQueueItemId(currentItemId: Long, isShuffled: Boolean): Long {
        return if (isShuffled) {
            playQueueDao.getNextShuffledQueueItemId(currentItemId) ?: playQueueDao.getFirstShuffledItem()
        } else {
            playQueueDao.getNextQueueItemId(currentItemId) ?: playQueueDao.getFirstItem()
        }
    }

    fun getPreviousQueueItemId(currentItemId: Long, isShuffled: Boolean): Long {
        return if (isShuffled) {
            playQueueDao.getPreviousShuffledQueueItemId(currentItemId) ?: playQueueDao.getLastShuffledItem()
        } else {
            playQueueDao.getPreviousQueueItemId(currentItemId) ?: playQueueDao.getLastItem()
        }
    }

    fun getItemAtPosition(position: Int, isShuffled: Boolean): Long? {
        return if (isShuffled) {
            playQueueDao.getItemIdAtShuffledPosition(position)
        } else {
            playQueueDao.getItemIdAtPosition(position)
        }
    }

    fun deletePlayQueue() {
        playQueueDao.deletePlayQueue()
    }

    fun getPlayQueueSize() = playQueueDao.getPlayQueueSize()

    fun getPlayQueueSizeObservable() = playQueueDao.getPlayQueueSizeObservable()

    fun insertTrackPosition(itemId: Long, trackPosition: Long) {
        libraryDatabase.runInTransaction {
            val currentPosition = playQueueDao.getTrackPosition(itemId)
            if (currentPosition == trackPosition) {
                return@runInTransaction
            }
            playQueueDao.insertTrackPosition(itemId, trackPosition, System.currentTimeMillis())
            playQueueDao.deleteOldestTrackPosition()
        }
    }

    fun getTrackPosition(itemId: Long): Long {
        return playQueueDao.getTrackPosition(itemId)
    }

    private fun toEntityList(
        compositions: List<Composition>,
        position: Int,
        shuffledPosition: Int
    ): List<PlayQueueEntity> {
        var currentPosition = position
        var currentShuffledPosition = shuffledPosition
        val entityList = ArrayList<PlayQueueEntity>(compositions.size)

        for (composition in compositions) {
            val playQueueEntity = PlayQueueEntity()
            playQueueEntity.audioId = composition.id
            playQueueEntity.position = currentPosition++
            playQueueEntity.shuffledPosition = currentShuffledPosition++

            entityList.add(playQueueEntity)
        }
        return entityList
    }

    companion object {
        private const val DB_OBSERVABLE_RETRY_COUNT = 5
    }
}
