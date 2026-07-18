package com.github.anrimian.musicplayer.data.database.dao.albums

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@Dao
interface AlbumsDao {

    @Query("INSERT OR REPLACE INTO albums (artistId, name) VALUES (:artistId, :name)")
    fun insertAlbum(artistId: Long?, name: String): Long

    @RawQuery(observedEntities = [ ArtistEntity::class, CompositionEntity::class, AlbumEntity::class ])
    fun getAllObservable(query: SupportSQLiteQuery): Observable<List<Album>>

    @RawQuery(observedEntities = [ ArtistEntity::class, CompositionEntity::class, AlbumEntity::class ])
    fun getCompositionsInAlbumObservable(query: SimpleSQLiteQuery): Observable<List<AlbumComposition>>

    @RawQuery
    fun getCompositionsInAlbum(query: SimpleSQLiteQuery): List<Composition>

    @Query("""
        SELECT id FROM compositions WHERE albumId = :albumId
        ORDER BY discNumber, trackNumber, fileName
    """)
    fun getCompositionIdsInAlbum(albumId: Long): Single<List<Long>>

    @Query("""
        SELECT id AS id,
            name AS name, 
            (SELECT name FROM artists WHERE artists.id = albums.artistId) AS artist, 
            (SELECT count() FROM compositions WHERE albumId = albums.id) AS compositionsCount 
        FROM albums 
        WHERE albums.artistId = :artistId
    """)
    fun getAllAlbumsForArtistObservable(artistId: Long): Observable<List<Album>>

    @Query("""
        SELECT id AS id,
            name AS name, 
            (SELECT name FROM artists WHERE artists.id = albums.artistId) AS artist, 
            (SELECT count() FROM compositions WHERE albumId = albums.id) AS compositionsCount 
        FROM albums 
        WHERE id = :albumId 
        LIMIT 1
    """)
    fun getAlbumObservable(albumId: Long): Observable<List<Album>>

    @Query("""
        SELECT id AS id,
            name AS name, 
            (SELECT name FROM artists WHERE artists.id = albums.artistId) AS artist, 
            (SELECT count() FROM compositions WHERE albumId = albums.id) AS compositionsCount 
        FROM albums 
        WHERE id = :albumId 
        LIMIT 1
    """)
    fun getAlbum(albumId: Long): Album?

    @Query("SELECT name AS name FROM albums WHERE id = :albumId LIMIT 1")
    fun getAlbumName(albumId: Long): String

    @Query("""
        SELECT id FROM albums 
        WHERE (artistId = :artistId OR (artistId IS NULL AND :artistId IS NULL)) 
        AND name = :name
    """)
    fun findAlbum(artistId: Long?, name: String): Long?

    @Query("UPDATE albums SET artistId = :artistId WHERE id = :albumId")
    fun setAuthorId(albumId: Long, artistId: Long?)

    @Query("SELECT * FROM albums WHERE id = :id")
    fun getAlbumEntity(id: Long): AlbumEntity

    @Query("""
        DELETE FROM albums 
        WHERE id = :id AND (SELECT count() FROM compositions WHERE albumId = albums.id) = 0
    """)
    fun deleteEmptyAlbum(id: Long)

    @Query("DELETE FROM albums WHERE (SELECT count() FROM compositions WHERE albumId = albums.id) = 0")
    fun deleteEmptyAlbums()

    @Query("SELECT name FROM albums")
    fun getAlbumNames(): Array<String>

    @Query("UPDATE albums SET name = :name WHERE id = :id")
    fun updateAlbumName(name: String, id: Long)

    @Query("SELECT artistId FROM albums WHERE id = :albumId")
    fun getArtistId(albumId: Long): Long?

    @Query("""
        UPDATE compositions SET albumId = :newAlbumId WHERE id IN (
            SELECT id FROM compositions WHERE albumId = :oldAlbumId
        )
    """)
    fun changeCompositionsAlbum(oldAlbumId: Long, newAlbumId: Long)

    @Query("""
        UPDATE compositions SET modifiedTime = :modifiedTime WHERE id IN (
            SELECT id FROM compositions WHERE albumId = :albumId
        )
    """)
    fun updateAlbumCompositionsModifyTime(albumId: Long, modifiedTime: Long)

    companion object {

        private const val ALBUM_ITEMS_ORDER = "ORDER BY discNumber, trackNumber, fileName"

        fun getAlbumCompositionsQuery(useFileName: Boolean): String {
            return """
                SELECT ${CompositionsDao.getCompositionSelectionQuery(useFileName)}, 
                        trackNumber AS trackNumber, 
                        discNumber AS discNumber 
                    FROM compositions 
                    WHERE albumId = ? 
                    $ALBUM_ITEMS_ORDER
            """
        }

        fun getCompositionsQuery(useFileName: Boolean): String {
            return """
                SELECT ${CompositionsDao.getCompositionSelectionQuery(useFileName)} 
                    FROM compositions 
                    WHERE albumId = ? 
                    $ALBUM_ITEMS_ORDER
            """
        }
    }

}