package com.github.anrimian.musicplayer.data.database.dao.albums;

import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs;

import androidx.annotation.Nullable;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class AlbumsDaoWrapper {

    private final AppDatabase appDatabase;
    private final AlbumsDao albumsDao;
    private final ArtistsDao artistsDao;
    private final ArtistsDaoWrapper artistsDaoWrapper;

    public AlbumsDaoWrapper(AppDatabase appDatabase,
                            AlbumsDao albumsDao,
                            ArtistsDao artistsDao,
                            ArtistsDaoWrapper artistsDaoWrapper) {
        this.appDatabase = appDatabase;
        this.albumsDao = albumsDao;
        this.artistsDao = artistsDao;
        this.artistsDaoWrapper = artistsDaoWrapper;
    }

    public Observable<List<Album>> getAllObservable(Order order, String searchText) {
        String query = "SELECT id as id," +
                "name as name, " +
                "(SELECT name FROM artists WHERE artists.id = albums.artistId) as artist, " +
                "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
                "FROM albums";
        query += getSearchQuery();
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, getSearchArgs(searchText, 3));
        return albumsDao.getAllObservable(sqlQuery);
    }

    public Observable<List<Album>> getAllAlbumsForArtistObservable(long artistId) {
        return albumsDao.getAllAlbumsForArtistObservable(artistId);
    }

    public Observable<List<Composition>> getCompositionsInAlbumObservable(long albumId, boolean useFileName) {
        String query = AlbumsDao.getCompositionsQuery(useFileName);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, new Object[] {albumId} );
        return albumsDao.getCompositionsInAlbumObservable(sqlQuery);
    }

    public Single<List<Long>> getCompositionIdsInAlbum(long albumId) {
        return albumsDao.getCompositionIdsInAlbum(albumId);
    }

    public Observable<Album> getAlbumObservable(long albumId) {
        return albumsDao.getAlbumObservable(albumId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public String[] getAlbumNames() {
        return albumsDao.getAlbumNames();
    }

    public void updateAlbumName(String name, long id) {
        appDatabase.runInTransaction(() -> {
            albumsDao.updateAlbumCompositionsModifyTime(id, new Date());

            Long artistId = albumsDao.getArtistId(id);
            Long existAlbumId = albumsDao.findAlbum(artistId, name);
            if (existAlbumId == null) {
                albumsDao.updateAlbumName(name, id);
            } else {
                albumsDao.changeCompositionsAlbum(id, existAlbumId);
                albumsDao.deleteEmptyAlbum(id);
            }
        });
    }

    public void updateAlbumArtist(long albumId, String artistName) {
        appDatabase.runInTransaction(() -> {
            albumsDao.updateAlbumCompositionsModifyTime(albumId, new Date());

            Long artistId = artistsDao.findArtistIdByName(artistName);

            if (artistId != null) {
                String albumName = albumsDao.getAlbumName(albumId);
                Long existAlbumId = albumsDao.findAlbum(artistId, albumName);
                if (existAlbumId != null) {
                    albumsDao.changeCompositionsAlbum(albumId, existAlbumId);
                    albumsDao.deleteEmptyAlbum(albumId);
                    return;
                }
            } else {
                if (!TextUtils.isEmpty(artistName)) {
                    artistId = artistsDao.insertArtist(artistName);
                }
            }
            Long oldArtistId = albumsDao.getArtistId(albumId);
            albumsDao.setAuthorId(albumId, artistId);
            if (oldArtistId != null) {
                artistsDao.deleteEmptyArtist(oldArtistId);
            }
        });
    }

    @Nullable
    public Long getOrInsertAlbum(String albumName,
                                 String albumArtist,
                                 int firstYear,
                                 int lastYear,
                                 Map<String, Long> artistsCache,
                                 Map<String, Long> albumsCache) {
        if (albumName == null) {
            return null;
        }
        Long albumArtistId = artistsDaoWrapper.getOrInsertArtist(albumArtist, artistsCache);

        String cacheKey = albumName + albumArtist;//with low chance, but can be collision here
        Long albumId = albumsCache.get(cacheKey);
        if (albumId != null) {
            return albumId;
        }

        albumId = albumsDao.findAlbum(albumArtistId, albumName);
        if (albumId == null) {
            albumId = albumsDao.insert(new AlbumEntity(albumArtistId,
                    albumName,
                    firstYear,
                    lastYear));
        }
        albumsCache.put(albumName, albumId);
        return albumId;
    }

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case NAME: {
                orderQuery.append("name");
                break;
            }
            case COMPOSITION_COUNT: {
                orderQuery.append("compositionsCount");
                break;
            }
            default: throw new IllegalStateException("unknown order type" + order);
        }
        orderQuery.append(" ");
        orderQuery.append(order.isReversed()? "DESC" : "ASC");
        return orderQuery.toString();
    }

    private String getSearchQuery() {
        return " WHERE (? IS NULL OR (name LIKE ? OR artist NOTNULL AND artist LIKE ?))";
    }

}
