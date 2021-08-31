package com.github.anrimian.musicplayer.data.database.dao.artist;

import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.models.composition.CompositionId;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.order.Order;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class ArtistsDaoWrapper {

    private final ArtistsDao artistsDao;

    public ArtistsDaoWrapper(ArtistsDao artistsDao) {
        this.artistsDao = artistsDao;
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
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, new Object[] {artistId} );
        return artistsDao.getCompositionsByArtistObservable(sqlQuery);
    }

    public List<CompositionId> getCompositionsByArtist(long artistId) {
        return artistsDao.getCompositionsByArtist(artistId);
    }

    public Observable<Artist> getArtistObservable(long artistId) {
        return artistsDao.getArtistObservable(artistId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public String[] getAuthorNames() {
        return artistsDao.getAuthorNames();
    }

    public void updateArtistName(String name, long id) {
        artistsDao.updateArtistName(name, id);
    }

    public boolean isArtistExists(String name) {
        return artistsDao.isArtistExists(name);
    }

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case ALPHABETICAL: {
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
