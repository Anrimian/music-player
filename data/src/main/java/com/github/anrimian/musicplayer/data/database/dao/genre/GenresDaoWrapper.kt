package com.github.anrimian.musicplayer.data.database.dao.genre

import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao.Companion.getCompositionsQuery
import com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.Date

class GenresDaoWrapper(
    private val appDatabase: LibraryDatabase,
    private val genreDao: GenreDao,
    private val compositionsDao: CompositionsDao,
) {

    fun getAllObservable(order: Order, searchText: String?): Observable<List<Genre>> {
        var query = """
            SELECT id as id,
            name as name, 
            (SELECT count() FROM genre_entries WHERE genreId = genres.id) as compositionsCount, 
            (SELECT sum(duration) FROM compositions WHERE compositions.id IN (SELECT compositionId FROM genre_entries WHERE genreId = genres.id)) as totalDuration 
            FROM genres
        """
        query += getSearchQuery()
        query += getOrderQuery(order)
        val sqlQuery = SimpleSQLiteQuery(query, DatabaseUtils.getSearchArgs(searchText, 2))
        return genreDao.getAllObservable(sqlQuery)
    }

    fun getGenreObservable(genreId: Long): Observable<Genre> {
        return genreDao.getGenreObservable(genreId)
            .takeWhile(List<Genre>::isNotEmpty)
            .map { list -> list[0] }
    }

    fun getCompositionsInGenreObservable(
        genreId: Long,
        useFileName: Boolean,
    ): Observable<List<Composition>> {
        val query = getCompositionsQuery(useFileName)
        val sqlQuery = SimpleSQLiteQuery(query, arrayOf(genreId))
        return genreDao.getCompositionsInGenreObservable(sqlQuery)
    }

    fun getCompositionsInGenre(genreId: Long, useFileName: Boolean): List<Composition> {
        val query: String = getCompositionsQuery(useFileName)
        val sqlQuery = SimpleSQLiteQuery(query, arrayOf(genreId))
        return genreDao.getCompositionsInGenre(sqlQuery)
    }

    fun getAllCompositionIdsByGenre(genreId: Long): Single<List<Long>> {
        return genreDao.getAllCompositionsByGenre(genreId)
    }

    fun containsCompositionGenre(compositionId: Long, genreName: String): Boolean {
        return genreDao.containsCompositionGenre(compositionId, genreName)
    }

    fun moveGenres(compositionId: Long, fromPos: Int, toPos: Int) {
        appDatabase.runInTransaction {
            genreDao.moveGenres(compositionId, fromPos, toPos)
            compositionsDao.setUpdateTime(compositionId, Date())
        }
    }

    @JvmOverloads
    fun addCompositionToGenre(compositionId: Long, genreName: String, position: Int? = null) {
        appDatabase.runInTransaction {
            var genreId = genreDao.findGenre(genreName)
            if (genreId == null) {
                genreId = genreDao.insertGenre(genreName)
            }
            if (position != null) {
                genreDao.increasePositionsAfter(position, compositionId)
            }
            genreDao.insertGenreEntry(compositionId, genreId, position)
            compositionsDao.setUpdateTime(compositionId, Date())
        }
    }

    fun removeCompositionFromGenre(compositionId: Long, genre: String): Int {
        return appDatabase.runInTransaction<Int> {
            val genreId = genreDao.findGenre(genre) ?: throw IllegalStateException("genre not found")
            val position = genreDao.getGenrePosition(compositionId, genreId)

            genreDao.removeGenreEntry(compositionId, genreId)
            genreDao.decreasePositionsAfter(position, compositionId)
            genreDao.deleteEmptyGenre(genreId)
            compositionsDao.setUpdateTime(compositionId, Date())
            return@runInTransaction position
        }
    }

    fun changeCompositionGenre(compositionId: Long, oldGenreName: String, newGenreName: String) {
        appDatabase.runInTransaction {
            val oldGenreId = genreDao.findGenre(oldGenreName)
                ?: throw IllegalStateException("old genre not found")
            val genreId = genreDao.findGenre(newGenreName)
                ?: genreDao.insertGenre(newGenreName)
            val position = genreDao.getGenrePosition(compositionId, oldGenreId)
            genreDao.insertGenreEntry(compositionId, genreId, position)
            genreDao.removeGenreEntry(compositionId, oldGenreId)
            genreDao.deleteEmptyGenre(oldGenreId)
            compositionsDao.setUpdateTime(compositionId, Date())
        }
    }

    fun getGenreNames(forCompositionId: Long): Array<String> {
        return genreDao.getGenreNames(forCompositionId)
    }

    fun updateGenreName(name: String, genreId: Long, compositionIds: List<Long>) {
        appDatabase.runInTransaction {
            genreDao.updateGenreCompositionsModifyTime(genreId, Date())
            val existsGenreId = genreDao.findGenre(name)
            if (existsGenreId == null) {
                genreDao.updateGenreName(name, genreId)
                return@runInTransaction
            }
            genreDao.changeCompositionsGenre(genreId, existsGenreId)
            genreDao.deleteEmptyGenre(genreId)
            val date = Date()
            for (compositionId in compositionIds) {
                compositionsDao.setUpdateTime(compositionId, date)
            }
        }
    }

    fun getGenreName(genreId: Long): String {
        return genreDao.getGenreName(genreId)
    }

    private fun getOrderQuery(order: Order): String {
        val orderQuery = StringBuilder(" ORDER BY ")
        when (order.orderType) {
            OrderType.NAME -> orderQuery.append("name")
            OrderType.COMPOSITION_COUNT -> orderQuery.append("compositionsCount")
            else -> throw IllegalStateException("unknown order type$order")
        }
        orderQuery.append(" ")
        orderQuery.append(if (order.isReversed) "DESC" else "ASC")
        return orderQuery.toString()
    }

    private fun getSearchQuery() = " WHERE (? IS NULL OR name LIKE ?)"
}