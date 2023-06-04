package com.github.anrimian.musicplayer.data.database.dao.artist;

import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs;
import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.toArgs;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.order.Order;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class ArtistsDaoWrapper {

    private final AppDatabase appDatabase;
    private final ArtistsDao artistsDao;
    private final AlbumsDao albumsDao;

    public ArtistsDaoWrapper(AppDatabase appDatabase, ArtistsDao artistsDao, AlbumsDao albumsDao) {
        this.appDatabase = appDatabase;
        this.artistsDao = artistsDao;
        this.albumsDao = albumsDao;
    }

    public Observable<List<Artist>> getAllObservable(Order order, String searchText) {
        String query = "SELECT id as id," +
                "name as name, " +
                "(SELECT count() FROM compositions WHERE artistId = artists.id) as compositionsCount, " +
                "(SELECT count() FROM albums WHERE artistId = artists.id) as albumsCount " +
                "FROM artists";
        query += getSearchQuery();
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, getSearchArgs(searchText, 2));
        return artistsDao.getAllObservable(sqlQuery);
    }

    public Observable<List<Composition>> getCompositionsByArtistObservable(long artistId, boolean useFileName) {
        String query = ArtistsDao.getCompositionsQuery(useFileName);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, new Object[] { artistId } );
        return artistsDao.getCompositionsByArtistObservable(sqlQuery);
    }

    /**
     * Selection logic should be the same as in getAllCompositionIdsByArtist()
     */
    public List<Composition> getAllCompositionsByArtist(long artistId, boolean useFileName) {
        String query = ArtistsDao.getAllCompositionsQuery(useFileName);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, toArgs(artistId, 2));
        return artistsDao.getCompositionsByArtist(sqlQuery);
    }

    /**
     * Selection logic should be the same as in getAllCompositionsByArtist()
     */
    public Single<List<Long>> getAllCompositionIdsByArtist(long artistId) {
        return artistsDao.getAllCompositionIdsByArtist(artistId);
    }

    public Observable<Artist> getArtistObservable(long artistId) {
        return artistsDao.getArtistObservable(artistId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public String[] getAuthorNames() {
        return artistsDao.getAuthorNames();
    }

    public String getAuthorName(long artistId) {
        return artistsDao.getAuthorName(artistId);
    }

    public void updateArtistName(String name, long id) {
        appDatabase.runInTransaction(() -> {
            artistsDao.updateArtistCompositionsModifyTime(id, new Date());

            Long existArtistId = artistsDao.findArtistIdByName(name);
            if (existArtistId == null) {
                artistsDao.updateArtistName(name, id);
                return;
            }

            artistsDao.changeCompositionsArtist(id, existArtistId);

            List<Long> albums = artistsDao.getAllAlbumsWithArtist(id);
            for (Long albumId: albums) {
                String albumName = albumsDao.getAlbumName(albumId);
                Long existAlbumId = albumsDao.findAlbum(existArtistId, albumName);
                if (existAlbumId != null) {
                    albumsDao.changeCompositionsAlbum(albumId, existAlbumId);
                    albumsDao.deleteEmptyAlbum(albumId);
                } else {
                    albumsDao.setAuthorId(albumId, existArtistId);
                }
            }
            artistsDao.deleteEmptyArtist(id);
        });
    }

    @Nullable
    public Long getOrInsertArtist(String artist, Map<String, Long> artistsCache) {
        Long artistId = artistsCache.get(artist);
        if (artistId != null) {
            return artistId;
        }
        if (artist != null) {
            artistId = artistsDao.findArtistIdByName(artist);
            if (artistId == null) {
                artistId = artistsDao.insertArtist(artist);
            }
            artistsCache.put(artist, artistId);
        }
        return artistId;
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
        return " WHERE (? IS NULL OR name LIKE ?)";
    }

}
