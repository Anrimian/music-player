package com.github.anrimian.musicplayer.data.database.dao.artist

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@Dao
interface ArtistsDao {

    @RawQuery(observedEntities = [ ArtistEntity::class, CompositionEntity::class ])
    fun getAllObservable(query: SupportSQLiteQuery): Observable<List<Artist>>

    @Query("""
        SELECT id AS id,
            name AS name,
            (SELECT count() FROM compositions WHERE artistId = artists.id) AS compositionsCount,
            (SELECT count() FROM albums WHERE artistId = artists.id) AS albumsCount
        FROM artists
        WHERE id = :artistId LIMIT 1
    """)
    fun getArtistObservable(artistId: Long): Observable<List<Artist>>

    @RawQuery(observedEntities = [ ArtistEntity::class, CompositionEntity::class, AlbumEntity::class ])
    fun getCompositionsByArtistObservable(query: SimpleSQLiteQuery): Observable<List<Composition>>

    @RawQuery
    fun getCompositionsByArtist(query: SimpleSQLiteQuery): List<Composition>

    @Query("""
        WITH artistCompositions(id) AS (SELECT id FROM compositions WHERE artistId = :artistId)
        SELECT id FROM artistCompositions
        UNION
        SELECT compositions.id FROM compositions
        WHERE (SELECT count(*) FROM artistCompositions) = 0 AND albumId IN(SELECT id FROM albums WHERE artistId = :artistId)
    """)
    fun getAllCompositionIdsByArtist(artistId: Long): Single<List<Long>>

    @Query("SELECT id FROM albums WHERE artistId = :artistId")
    fun getAllAlbumsWithArtist(artistId: Long): List<Long>

    @Query("SELECT name FROM artists")
    fun getAuthorNames(): Array<String>

    @Query("SELECT name FROM artists WHERE id = :artistId")
    fun getAuthorName(artistId: Long): String

    @Query("SELECT id FROM artists WHERE name = :name")
    fun findArtistIdByName(name: String): Long?

    @Query("INSERT OR REPLACE INTO artists (name) VALUES (:name)")
    fun insertArtist(name: String): Long

    @Query("""
        DELETE FROM artists
        WHERE id = :id
        AND (SELECT count() FROM compositions WHERE artistId = artists.id) = 0
        AND (SELECT count() FROM albums WHERE artistId = artists.id) = 0
    """)
    fun deleteEmptyArtist(id: Long)

    @Query("""
        DELETE FROM artists
        WHERE (SELECT count() FROM compositions WHERE artistId = artists.id) = 0
        AND (SELECT count() FROM albums WHERE artistId = artists.id) = 0
    """)
    fun deleteEmptyArtists()

    @Query("UPDATE artists SET name = :name WHERE id = :artistId")
    fun updateArtistName(artistId: Long, name: String)

    @Query("UPDATE compositions SET artistId = :newArtistId WHERE artistId = :oldArtistId")
    fun changeCompositionsArtist(oldArtistId: Long, newArtistId: Long)

    @Query("""
        UPDATE compositions
        SET modifiedTime = :modifiedTime
        WHERE artistId = :artistId OR albumId IN(SELECT id FROM albums WHERE artistId = :artistId)
    """)
    fun updateArtistCompositionsModifyTime(artistId: Long, modifiedTime: Long)

    companion object {
        fun getCompositionsQuery(useFileName: Boolean): String {
            return """
                SELECT ${CompositionsDao.getCompositionSelectionQuery(useFileName)}
                FROM compositions
                WHERE artistId = ?
                """
        }

        fun getAllCompositionsQuery(useFileName: Boolean): String {
            return """
                WITH artistCompositions AS (SELECT ${CompositionsDao.getCompositionSelectionQuery(useFileName)}
                FROM compositions
                WHERE artistId = ?)
                SELECT * FROM artistCompositions
                UNION
                SELECT ${CompositionsDao.getCompositionSelectionQuery(useFileName)}
                FROM compositions
                WHERE (SELECT count(*) FROM artistCompositions) = 0 AND albumId IN(SELECT id FROM albums WHERE artistId = ?)
                """
        }
    }
}