package com.github.anrimian.musicplayer.data.database.dao.albums

import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.anrimian.musicplayer.data.database.LibraryDatabase
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper
import com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.utils.rx.firstListItemOrComplete
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class AlbumsDaoWrapper(
    private val libraryDatabase: LibraryDatabase,
    private val albumsDao: AlbumsDao,
    private val artistsDao: ArtistsDao,
    private val artistsDaoWrapper: ArtistsDaoWrapper
) {

    fun getAllObservable(order: Order, searchText: String?): Observable<List<Album>> {
        val query = """
            SELECT id AS id,
                name AS name, 
                (SELECT name FROM artists WHERE artists.id = albums.artistId) AS artist, 
                (SELECT count() FROM compositions WHERE albumId = albums.id) AS compositionsCount 
            FROM albums
            $SEARCH_QUERY
            ${getOrderQuery(order)}
        """
        val sqlQuery = SimpleSQLiteQuery(query, DatabaseUtils.getSearchArgs(searchText, 3))
        return albumsDao.getAllObservable(sqlQuery)
    }

    fun getAllAlbumsForArtistObservable(artistId: Long): Observable<List<Album>> {
        return albumsDao.getAllAlbumsForArtistObservable(artistId)
    }

    fun getCompositionsInAlbumObservable(
        albumId: Long,
        useFileName: Boolean
    ): Observable<List<AlbumComposition>> {
        val query = AlbumsDao.getAlbumCompositionsQuery(useFileName)
        val sqlQuery = SimpleSQLiteQuery(query, arrayOf<Any>(albumId))
        return albumsDao.getCompositionsInAlbumObservable(sqlQuery)
    }

    fun getCompositionsInAlbum(albumId: Long, useFileName: Boolean): List<Composition> {
        val query = AlbumsDao.getCompositionsQuery(useFileName)
        val sqlQuery = SimpleSQLiteQuery(query, arrayOf<Any>(albumId))
        return albumsDao.getCompositionsInAlbum(sqlQuery)
    }

    fun getCompositionIdsInAlbum(albumId: Long): Single<List<Long>> {
        return albumsDao.getCompositionIdsInAlbum(albumId)
    }

    fun getAlbumObservable(albumId: Long): Observable<Album> {
        return albumsDao.getAlbumObservable(albumId)
            .firstListItemOrComplete()
    }

    fun getAlbumNames(): Array<String> {
        return albumsDao.getAlbumNames()
    }

    fun updateAlbumName(albumId: Long, name: String) {
        libraryDatabase.runInTransaction {
            albumsDao.updateAlbumCompositionsModifyTime(albumId, System.currentTimeMillis())
            val artistId = albumsDao.getArtistId(albumId)
            val existAlbumId = albumsDao.findAlbum(artistId, name)
            if (existAlbumId == null) {
                albumsDao.updateAlbumName(name, albumId)
            } else {
                albumsDao.changeCompositionsAlbum(albumId, existAlbumId)
                albumsDao.deleteEmptyAlbum(albumId)
            }
        }
    }

    fun updateAlbumArtist(albumId: Long, artistName: String?) {
        libraryDatabase.runInTransaction {
            albumsDao.updateAlbumCompositionsModifyTime(albumId, System.currentTimeMillis())
            val artistId = artistsDaoWrapper.getOrCreateArtist(artistName)

            val albumName = albumsDao.getAlbumName(albumId)
            val existingAlbumId = albumsDao.findAlbum(artistId, albumName)

            if (existingAlbumId != null && existingAlbumId != albumId) {
                // Merge into the existing other album
                albumsDao.changeCompositionsAlbum(albumId, existingAlbumId)
                albumsDao.deleteEmptyAlbum(albumId)
            } else {
                // If not found - set artist id to album.
                val oldArtistId = albumsDao.getArtistId(albumId)
                albumsDao.setAuthorId(albumId, artistId)
                if (oldArtistId != null) {
                    artistsDao.deleteEmptyArtist(oldArtistId)
                }
            }
        }
    }

    fun getOrInsertAlbum(
        albumName: String?,
        albumArtist: String?,
        artistsCache: MutableMap<String, Long>,
        albumsCache: MutableMap<String, Long>
    ): Long? {
        if (albumName == null) {
            return null
        }
        val albumArtistId = artistsDaoWrapper.getOrInsertArtist(albumArtist, artistsCache)


        val cacheKey = albumName + albumArtist // with low chance, but can be collision here
        var albumId = albumsCache[cacheKey]
        if (albumId != null) {
            return albumId
        }
        albumId = albumsDao.findAlbum(albumArtistId, albumName)
        if (albumId == null) {
            albumId = albumsDao.insertAlbum(albumArtistId, albumName)
        }
        albumsCache[cacheKey] = albumId
        return albumId
    }

    private fun getOrderQuery(order: Order): String {
        val orderColumn = when (order.orderType) {
            OrderType.NAME -> "name"
            OrderType.ARTIST -> "artist"
            OrderType.COMPOSITION_COUNT -> "compositionsCount"
            else -> throw IllegalStateException("unknown order type$order")
        }
        val direction = if (order.isReversed) "DESC" else "ASC"
        return "ORDER BY $orderColumn $direction"
    }

    companion object {
        private const val SEARCH_QUERY =
            " WHERE (? IS NULL OR (name LIKE ? OR artist NOTNULL AND artist LIKE ?))"
    }

}
