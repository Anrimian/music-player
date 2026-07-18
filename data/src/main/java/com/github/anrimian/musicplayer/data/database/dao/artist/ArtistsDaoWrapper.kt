package com.github.anrimian.musicplayer.data.database.dao.artist

import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao
import com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.utils.rx.firstListItemOrComplete
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class ArtistsDaoWrapper(
    private val libraryDatabase: LibraryDatabase,
    private val artistsDao: ArtistsDao,
    private val albumsDao: AlbumsDao
) {

    fun getAllObservable(order: Order, searchText: String?): Observable<List<Artist>> {
        val query = """
            SELECT id as id,
                name as name,
                (SELECT count() FROM compositions WHERE artistId = artists.id) as compositionsCount,
                (SELECT count() FROM albums WHERE artistId = artists.id) as albumsCount
            FROM artists
            $SEARCH_QUERY
            ${getOrderQuery(order)}
        """
        val sqlQuery = SimpleSQLiteQuery(query, DatabaseUtils.getSearchArgs(searchText, 2))
        return artistsDao.getAllObservable(sqlQuery)
    }

    fun getCompositionsByArtistObservable(
        artistId: Long,
        useFileName: Boolean
    ): Observable<List<Composition>> {
        val query = ArtistsDao.getCompositionsQuery(useFileName)
        val sqlQuery = SimpleSQLiteQuery(query, arrayOf<Any>(artistId))
        return artistsDao.getCompositionsByArtistObservable(sqlQuery)
    }

    /**
     * Selection logic should be the same as in getAllCompositionIdsByArtist()
     */
    fun getAllCompositionsByArtist(
        artistId: Long,
        useFileName: Boolean
    ): List<Composition> {
        val query = ArtistsDao.getAllCompositionsQuery(useFileName)
        val sqlQuery = SimpleSQLiteQuery(query, Array(2) { artistId })
        return artistsDao.getCompositionsByArtist(sqlQuery)
    }

    /**
     * Selection logic should be the same as in getAllCompositionsByArtist()
     */
    fun getAllCompositionIdsByArtist(artistId: Long): Single<List<Long>> {
        return artistsDao.getAllCompositionIdsByArtist(artistId)
    }

    fun getArtistObservable(artistId: Long): Observable<Artist> {
        return artistsDao.getArtistObservable(artistId)
            .firstListItemOrComplete()
    }

    fun getAuthorNames(): Array<String> {
        return artistsDao.getAuthorNames()
    }

    fun getAuthorName(artistId: Long): String {
        return artistsDao.getAuthorName(artistId)
    }

    fun updateArtistName(artistId: Long, name: String) {
        libraryDatabase.runInTransaction {
            artistsDao.updateArtistCompositionsModifyTime(artistId, System.currentTimeMillis())
            val existArtistId = artistsDao.findArtistIdByName(name)
            if (existArtistId == null) {
                artistsDao.updateArtistName(artistId, name)
                return@runInTransaction
            }

            artistsDao.changeCompositionsArtist(artistId, existArtistId)

            val albums = artistsDao.getAllAlbumsWithArtist(artistId)
            for (albumId in albums) {
                val albumName = albumsDao.getAlbumName(albumId)
                val existAlbumId = albumsDao.findAlbum(existArtistId, albumName)
                if (existAlbumId != null) {
                    albumsDao.changeCompositionsAlbum(albumId, existAlbumId)
                    albumsDao.deleteEmptyAlbum(albumId)
                } else {
                    albumsDao.setAuthorId(albumId, existArtistId)
                }
            }
            artistsDao.deleteEmptyArtist(artistId)
        }
    }

    fun getOrInsertArtist(artist: String?, artistsCache: MutableMap<String, Long>): Long? {
        if (artist == null) {
            return null
        }
        val cachedId = artistsCache[artist]
        if (cachedId != null) {
            return cachedId
        }
        val artistId = artistsDao.findArtistIdByName(artist) ?: artistsDao.insertArtist(artist)
        artistsCache[artist] = artistId
        return artistId
    }

    fun getOrCreateArtist(artistName: String?): Long? {
        return if (artistName.isNullOrBlank()) {
            null
        } else {
            artistsDao.findArtistIdByName(artistName) ?: artistsDao.insertArtist(artistName)
        }
    }

    private fun getOrderQuery(order: Order): String {
        val column = when (order.orderType) {
            OrderType.NAME -> "name"
            OrderType.COMPOSITION_COUNT -> "compositionsCount"
            else -> throw IllegalStateException("unknown order type: $order")
        }
        val direction = if (order.isReversed) "DESC" else "ASC"
        return "ORDER BY $column $direction"
    }

    companion object {
        private const val SEARCH_QUERY = "WHERE (? IS NULL OR name LIKE ?)"
    }

}