package com.github.anrimian.musicplayer.data.database.dao.albums;

import androidx.collection.LongSparseArray;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;

import java.util.List;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class AlbumsDaoWrapper {

    private final AppDatabase appDatabase;
    private final AlbumsDao albumsDao;
    private final ArtistsDao artistsDao;

    public AlbumsDaoWrapper(AppDatabase appDatabase, AlbumsDao albumsDao, ArtistsDao artistsDao) {
        this.appDatabase = appDatabase;
        this.albumsDao = albumsDao;
        this.artistsDao = artistsDao;
    }

    public void insertAll(List<StorageAlbum> albums) {
        albumsDao.insertAll(mapList(albums, this::toEntity));
    }

    public LongSparseArray<StorageAlbum> selectAllAsStorageAlbums() {
        return AndroidCollectionUtils.mapToSparseArray(
                albumsDao.selectAllAsStorageAlbums(),
                StorageAlbum::getId);
    }

    public Observable<List<Album>> getAllObservable(Order order, String searchText) {
        String query = "SELECT id as id," +
                "storageId as storageId, " +
                "name as name, " +
                "(SELECT name FROM artists WHERE artists.id = albums.artistId) as artist, " +
                "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
                "FROM albums";
        query += getSearchQuery(searchText);
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return albumsDao.getAllObservable(sqlQuery);
    }

    public Observable<List<Album>> getAllAlbumsForArtist(long artistId) {
        return albumsDao.getAllAlbumsForArtist(artistId);
    }

    public Observable<List<Composition>> getCompositionsInAlbumObservable(long albumId) {
        return albumsDao.getCompositionsInAlbumObservable(albumId);
    }

    public List<Composition> getCompositionsInAlbum(long albumId) {
        return albumsDao.getCompositionsInAlbum(albumId);
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
        albumsDao.updateAlbumName(name, id);
    }

    public void updateAlbumArtist(long albumId, String artistName) {
        appDatabase.runInTransaction(() -> {
            Long artistId = artistsDao.findArtistIdByName(artistName);

            if (artistId == null && artistName != null) {
                artistId = artistsDao.insertArtist(new ArtistEntity(null, artistName));//hmm, storage?
            }

            Long oldArtistId = albumsDao.getArtistId(albumId);

            albumsDao.setAuthorId(albumId, artistId);

            if (oldArtistId != null) {
                artistsDao.deleteEmptyArtist(oldArtistId);
            }
        });
    }

    private AlbumEntity toEntity(StorageAlbum album) {
        Long artistId = artistsDao.selectIdByStorageId(album.getArtistId());
        return new AlbumEntity(
                artistId,
                album.getId(),
                album.getAlbum(),
                album.getFirstYear(),
                album.getLastYear()
        );
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

    private String getSearchQuery(String searchText) {
        if (isEmpty(searchText)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" WHERE ");
        sb.append("name LIKE '%");
        sb.append(searchText);
        sb.append("%'");
        sb.append(" OR artist NOTNULL AND artist LIKE '%");
        sb.append(searchText);
        sb.append("%'");

        return sb.toString();
    }
}
