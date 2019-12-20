package com.github.anrimian.musicplayer.data.database.dao.compositions;

import androidx.collection.LongSparseArray;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapToMap;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

//TODO delete empty authors, albums, genres after delete?
public class CompositionsDaoWrapper {

    private final AppDatabase appDatabase;
    private final CompositionsDao compositionsDao;
    private final ArtistsDao artistsDao;
    private final AlbumsDao albumsDao;

    public CompositionsDaoWrapper(AppDatabase appDatabase,
                                  ArtistsDao artistsDao,
                                  CompositionsDao compositionsDao,
                                  AlbumsDao albumsDao) {
        this.appDatabase = appDatabase;
        this.artistsDao = artistsDao;
        this.compositionsDao = compositionsDao;
        this.albumsDao = albumsDao;
    }

    public Observable<List<Composition>> getAllObservable() {
        return compositionsDao.getAllObservable();
    }

    public Observable<FullComposition> getCompositionObservable(long id) {
        return compositionsDao.getCompositionObservable(id)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public Observable<List<Composition>> getAllObservable(Order order,
                                                          @Nullable String searchText) {
        String query = "SELECT " +
                "(SELECT name FROM artists WHERE id = artistId) as artist,  " +
                "(SELECT name FROM albums WHERE id = albumId) as album,  " +
                "title as title,  " +
                "filePath as filePath,  " +
                "duration as duration,  " +
                "size as size,  " +
                "id as id,  " +
                "storageId as storageId,  " +
                "dateAdded as dateAdded,  " +
                "dateModified as dateModified,  " +
                "corruptionType as corruptionType  " +
                "FROM compositions";
        query += getSearchQuery(searchText);
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return compositionsDao.getAllObservable(sqlQuery);
    }

    public List<Composition> getAll() {
        return compositionsDao.getAll();
    }

    public Map<Long, Composition> getAllMap() {
        return mapToMap(getAll(), new HashMap<>(), Composition::getId);
    }

    public LongSparseArray<StorageComposition> selectAllAsStorageCompositions() {
        return AndroidCollectionUtils.mapToSparseArray(compositionsDao.selectAllAsStorageCompositions(),
                StorageComposition::getId);
    }

    public void delete(long id) {
        compositionsDao.delete(id);
    }

    public void deleteAll(List<Long> ids) {
        compositionsDao.delete(ids);
    }

    public void deleteAll() {
        compositionsDao.deleteAll();
    }

    public void updateFilePath(long id, String filePath) {
        compositionsDao.updateFilePath(id, filePath);
    }

    public void updateFilesPath(List<Composition> compositions) {
        appDatabase.runInTransaction(() -> {
            for (Composition composition: compositions) {
                compositionsDao.updateFilePath(composition.getId(), composition.getFilePath());
            }
        });
    }

    public void updateAlbum(long compositionId, String albumName) {
        appDatabase.runInTransaction(() -> {
            Long artistId = compositionsDao.getArtistId(compositionId);

            // 1) find new album by artist and name from albums
            Long albumId = albumsDao.findAlbum(artistId, albumName);

            // 2) if album not exists - create album
            if (albumId == null && albumName != null) {
                albumId = albumsDao.insert(new AlbumEntity(artistId, null, albumName, 0, 0));//hmm, storage?
            }

            // 3) set new albumId
            Long oldAlbumId = compositionsDao.getAlbumId(compositionId);
            compositionsDao.updateAlbum(compositionId, albumId);

            if (oldAlbumId != null) {
                albumsDao.deleteEmptyAlbum(oldAlbumId);
            }
        });
    }

    public void updateArtist(long id, String authorName) {
        appDatabase.runInTransaction(() -> {
            // 1) find new artist by name from artists
            Long artistId = artistsDao.findArtistIdByName(authorName);

            // 2) if artist not exists - create artist
            if (artistId == null && authorName != null) {
                artistId = artistsDao.insertArtist(new ArtistEntity(null, authorName));//hmm, storage?
            }
            // 3) set new artistId
            long oldArtistId = compositionsDao.getArtistId(id);
            compositionsDao.updateArtist(id, artistId);

            // 4) if OLD artist exists and has no references - delete him
            if (oldArtistId != 0) {
                artistsDao.deleteEmptyArtist(oldArtistId);
            }
        });
    }

    public void updateAlbumArtist(long id, String artistName) {
        appDatabase.runInTransaction(() -> {
            //find album
            Long albumId = compositionsDao.getAlbumId(id);
            if (albumId != null) {
                // 1) find new artist by name from artists
                Long artistId = artistsDao.findArtistIdByName(artistName);

                // 2) if artist not exists - create artist
                if (artistId == null && artistName != null) {
                    artistId = artistsDao.insertArtist(new ArtistEntity(null, artistName));//hmm, storage?
                }

                AlbumEntity albumEntity = albumsDao.getAlbumEntity(albumId);
                Long oldArtistId = albumEntity.getArtistId();

                //find new album with author id and name
                Long newAlbumId = albumsDao.findAlbum(artistId, albumEntity.getName());
                //if not exists, create

                if (newAlbumId == null) {
                    newAlbumId = albumsDao.insert(new AlbumEntity(
                            artistId,
                            null,
                            albumEntity.getName(),
                            albumEntity.getFirstYear(),
                            albumEntity.getLastYear()
                    ));
                }
                //set new album to composition
                compositionsDao.setAlbumId(id, newAlbumId);

                //if album is empty, delete
                albumsDao.deleteEmptyAlbum(albumId);

                // 4) if OLD artist exists and has no references - delete him
                if (oldArtistId != null) {
                    artistsDao.deleteEmptyArtist(oldArtistId);
                }
            }
        });
    }

    public void updateTitle(long id, String title) {
        compositionsDao.updateTitle(id, title);
    }

    public void applyChanges(List<StorageComposition> addedCompositions,
                             List<StorageComposition> deletedCompositions,
                             List<StorageComposition> changedCompositions) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.insert(mapList(addedCompositions, this::toCompositionEntity));
            compositionsDao.deleteByStorageId(mapList(
                    deletedCompositions,
                    StorageComposition::getId)
            );
            for (StorageComposition composition: changedCompositions) {
                compositionsDao.update(
                        composition.getTitle(),
                        composition.getFilePath(),
                        composition.getDuration(),
                        composition.getSize(),
                        composition.getDateAdded(),
                        composition.getDateModified(),
                        composition.getId()
                );
            }
            //TODO also delete authors, albums, genres without references
        });
    }

    public void setCorruptionType(CorruptionType corruptionType, long id) {
        compositionsDao.setCorruptionType(corruptionType, id);
    }

    private CompositionEntity toCompositionEntity(StorageComposition composition) {
        Long artistId = artistsDao.selectIdByStorageId(composition.getArtistId());
        Long albumId = albumsDao.selectIdByStorageId(composition.getAlbumId());
        return CompositionMapper.toEntity(composition, artistId, albumId);
    }

    public long selectIdByStorageId(long compositionId) {
        return compositionsDao.selectIdByStorageId(compositionId);
    }

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case ALPHABETICAL: {
                orderQuery.append("title");
                break;
            }
            case ADD_TIME: {
                orderQuery.append("dateAdded");
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
        sb.append("title LIKE '%");
        sb.append(searchText);
        sb.append("%'");
        sb.append(" OR artist NOTNULL AND artist LIKE '%");
        sb.append(searchText);
        sb.append("%'");

        return sb.toString();
    }
}
