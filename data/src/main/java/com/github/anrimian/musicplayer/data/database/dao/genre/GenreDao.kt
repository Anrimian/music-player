package com.github.anrimian.musicplayer.data.database.dao.genre

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntity
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntryEntity
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.Date

@Dao
interface GenreDao {

    @Query("INSERT INTO genres (name) VALUES (:name)")
    fun insertGenre(name: String): Long

    fun insertGenreEntry(compositionId: Long, genreId: Long) {
        insertGenreEntry(compositionId, genreId, null)
    }

    @Query("""
        INSERT OR IGNORE INTO genre_entries (
            compositionId, 
            genreId, 
            position
        ) 
        VALUES (
            :compositionId, 
            :genreId, 
            coalesce(
                :position, 
                (SELECT max(position) + 1 FROM genre_entries WHERE compositionId = :compositionId), 
                0
            )
        )
    """)
    fun insertGenreEntry(compositionId: Long, genreId: Long, position: Int?)

    @Query("""
        UPDATE genre_entries 
        SET position = CASE     
            WHEN position < :fromPos THEN position + 1   
            WHEN position > :fromPos THEN position - 1   
            ELSE :toPos 
        END 
        WHERE (position BETWEEN min(:fromPos, :toPos) 
            AND max(:fromPos,:toPos)) 
            AND compositionId = :compositionId
    """)
    fun moveGenres(compositionId: Long, fromPos: Int, toPos: Int)

    @RawQuery(observedEntities = [GenreEntity::class, GenreEntryEntity::class, CompositionEntity::class])
    fun getAllObservable(query: SupportSQLiteQuery): Observable<List<Genre>>

    @RawQuery(observedEntities = [ArtistEntity::class, CompositionEntity::class, AlbumEntity::class, GenreEntryEntity::class])
    fun getCompositionsInGenreObservable(query: SimpleSQLiteQuery): Observable<List<Composition>>

    @RawQuery
    fun getCompositionsInGenre(query: SimpleSQLiteQuery): List<Composition>

    @Query("""
        SELECT 
        id AS id 
        FROM compositions 
        WHERE id IN (SELECT compositionId FROM genre_entries WHERE genreId = :genreId)
    """)
    fun getAllCompositionsByGenre(genreId: Long): Single<List<Long>>

    @Query("""
        SELECT 
        id AS id,
        name AS name, 
        (SELECT count() FROM genre_entries WHERE genreId = genres.id) AS compositionsCount, 
        (SELECT sum(duration) FROM compositions WHERE compositions.id IN (SELECT compositionId FROM genre_entries WHERE genreId = genres.id)) AS totalDuration 
        FROM genres 
        WHERE id = :genreId 
        LIMIT 1
    """)
    fun getGenreObservable(genreId: Long): Observable<List<Genre>>

    @Query("SELECT id FROM genres WHERE name = :name")
    fun findGenre(name: String): Long?

    @Query("""
        SELECT exists(
            SELECT 1 
            FROM genre_entries 
            WHERE genreId = (SELECT id FROM genres WHERE name = :genreName)   
                AND compositionId = :compositionId
        )
    """)
    fun containsCompositionGenre(compositionId: Long, genreName: String): Boolean

    @Query("""
        DELETE FROM genres 
        WHERE id = :id AND (SELECT count() FROM genre_entries WHERE genreId = :id) = 0
    """)
    fun deleteEmptyGenre(id: Long)

    @Query("""
        DELETE FROM genres 
        WHERE (SELECT count() FROM genre_entries WHERE genreId = genres.id) = 0
    """)
    fun deleteEmptyGenres()

    @Query("DELETE FROM genre_entries WHERE compositionId = :compositionId AND genreId = :genreId")
    fun removeGenreEntry(compositionId: Long, genreId: Long)

    @Query("""
        UPDATE genre_entries 
        SET position = position - 1 
        WHERE position > :position AND compositionId = :compositionId
    """)
    fun decreasePositionsAfter(position: Int, compositionId: Long)

    @Query("""
        UPDATE genre_entries 
        SET position = position + 1 
        WHERE position >= :position AND compositionId = :compositionId
    """)
    fun increasePositionsAfter(position: Int, compositionId: Long)

    @Query("DELETE FROM genre_entries WHERE compositionId = :compositionId")
    fun removeCompositionGenres(compositionId: Long)

    @Query("""
        SELECT name 
        FROM genres 
        WHERE id NOT IN (SELECT genreId FROM genre_entries WHERE compositionId = :forCompositionId)
    """)
    fun getGenreNames(forCompositionId: Long): Array<String>

    @Query("UPDATE genres SET name = :name WHERE id = :genreId")
    fun updateGenreName(name: String, genreId: Long)

    @Query("SELECT name FROM genres WHERE id = :genreId")
    fun getGenreName(genreId: Long): String

    @Query("UPDATE genre_entries SET genreId = :newGenreId WHERE genreId = :oldGenreId")
    fun changeCompositionsGenre(oldGenreId: Long, newGenreId: Long)

    @Query("""
        UPDATE compositions 
        SET dateModified = :dateModified 
        WHERE id IN (SELECT compositionId FROM genre_entries WHERE genreId = :genreId)
    """)
    fun updateGenreCompositionsModifyTime(genreId: Long, dateModified: Date)

    @Query("""
        SELECT position 
        FROM genre_entries 
        WHERE compositionId = :compositionId AND genreId = :genreId
    """)
    fun getGenrePosition(compositionId: Long, genreId: Long): Int

    companion object {
        @JvmStatic
        fun getCompositionsQuery(useFileName: Boolean): String {
            return """
                SELECT ${CompositionsDao.getCompositionSelectionQuery(useFileName)}
                FROM compositions 
                WHERE id IN (SELECT compositionId FROM genre_entries WHERE genreId = :genreId)
                """
        }
    }

}