package com.github.anrimian.musicplayer.data.database.dao.compositions;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs;
import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;

import androidx.collection.LongSparseArray;
import androidx.core.util.Pair;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.models.composition.ExternalComposition;
import com.github.anrimian.musicplayer.data.models.exceptions.CompositionNotFoundException;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CompositionsDaoWrapper {

    private final AppDatabase appDatabase;
    private final CompositionsDao compositionsDao;
    private final ArtistsDao artistsDao;
    private final AlbumsDao albumsDao;
    private final GenreDao genreDao;
    private final FoldersDao foldersDao;

    private final BehaviorSubject<Object> updateSubject = BehaviorSubject.createDefault(TRIGGER);

    public CompositionsDaoWrapper(AppDatabase appDatabase,
                                  ArtistsDao artistsDao,
                                  CompositionsDao compositionsDao,
                                  AlbumsDao albumsDao,
                                  GenreDao genreDao,
                                  FoldersDao foldersDao) {
        this.appDatabase = appDatabase;
        this.artistsDao = artistsDao;
        this.compositionsDao = compositionsDao;
        this.albumsDao = albumsDao;
        this.genreDao = genreDao;
        this.foldersDao = foldersDao;
    }

    public Observable<FullComposition> getCompositionObservable(long id) {
        return compositionsDao.getCompositionObservable(id)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public Observable<String> getLyricsObservable(long id) {
        return compositionsDao.getLyricsObservable(id);
    }

    public FullComposition getFullComposition(long id) {
        return compositionsDao.getFullComposition(id);
    }

    public Observable<List<Composition>> getAllObservable(Order order,
                                                          boolean useFileName,
                                                          @Nullable String searchText) {
        StringBuilder query = CompositionsDao.getCompositionQuery(useFileName);
        query.append(CompositionsDao.getSearchWhereQuery(useFileName));
        query.append(getOrderQuery(order));
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query.toString(), getSearchArgs(searchText, 3));
        return updateSubject.switchMap(o -> compositionsDao.getAllObservable(sqlQuery));
    }

    public void launchManualUpdate() {
        updateSubject.onNext(TRIGGER);
    }

    public Observable<List<Composition>> getCompositionsInFolderObservable(Long folderId,
                                                                           Order order,
                                                                           boolean useFileName,
                                                                           @Nullable String searchText) {
        StringBuilder query = CompositionsDao.getCompositionQuery(useFileName);
        query.append(CompositionsDao.getSearchWhereQuery(useFileName));
        query.append(" AND ");
        query.append("folderId = ");
        query.append(folderId);
        query.append(" OR (folderId IS NULL AND ");
        query.append(folderId);
        query.append(" IS NULL)");
        query.append(getOrderQuery(order));
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query.toString(), getSearchArgs(searchText, 3));
        return compositionsDao.getAllInFolderObservable(sqlQuery);
    }

    public List<Composition> getAllCompositionsInFolder(Long parentFolderId, boolean useFileName) {
        String query = FoldersDao.getRecursiveFolderQuery(parentFolderId);
        query += CompositionsDao.getCompositionQuery(useFileName);
        query += " WHERE folderId IN (SELECT childFolderId FROM allChildFolders) ";
        query += "OR folderId = ";
        query += parentFolderId;
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return compositionsDao.executeQuery(sqlQuery);
    }

    public List<Composition> getCompositionsInFolder(Long parentFolderId, Order order, boolean useFileName) {
        StringBuilder query = CompositionsDao.getCompositionQuery(useFileName);
        query.append(" WHERE folderId = ");
        query.append(parentFolderId);
        query.append(" OR (folderId IS NULL AND ");
        query.append(parentFolderId);
        query.append(" IS NULL)");
        query.append(getOrderQuery(order));
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query.toString());
        return compositionsDao.executeQuery(sqlQuery);
    }

    public LongSparseArray<StorageComposition> selectAllAsStorageCompositions() {
        LongSparseArray<StorageComposition> result = new LongSparseArray<>();
        final int pageSize = 1000;
        int index = 0;
        LongSparseArray<StorageComposition> pageResult;
        do {
            pageResult = AndroidCollectionUtils.mapToSparseArray(
                    compositionsDao.selectAllAsStorageCompositions(pageSize, index),
                    StorageComposition::getStorageId
            );
            result.putAll(pageResult);
            index++;
        } while (pageResult.size() == pageSize);
        return result;
    }

    public long getStorageId(long compositionId) {
        Long storageId = compositionsDao.getStorageId(compositionId);
        if (storageId == null) {
            throw new CompositionNotFoundException("composition not found");
        }
        return storageId;
    }

    public Maybe<Long> selectStorageId(long compositionId) {
        return Maybe.fromCallable(() -> compositionsDao.getStorageId(compositionId));
    }

    public void delete(long id) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.delete(id);
            albumsDao.deleteEmptyAlbums();
            artistsDao.deleteEmptyArtists();
            genreDao.deleteEmptyGenres();
            foldersDao.deleteFoldersWithoutContainment();
        });
    }

    public void deleteAll(List<Long> ids) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.delete(ids);
            albumsDao.deleteEmptyAlbums();
            artistsDao.deleteEmptyArtists();
            genreDao.deleteEmptyGenres();
            foldersDao.deleteFoldersWithoutContainment();
        });
    }

    public void updateFolderId(long id, Long folderId) {
        compositionsDao.updateFolderId(id, folderId);
    }

    public void updateStorageId(long id, Long storageId) {
        compositionsDao.updateStorageId(id, storageId);
    }

    public void updateAlbum(long compositionId, @Nullable String albumName) {
        appDatabase.runInTransaction(() -> {

            Long artistId = null;
            Long existsAlbumId = compositionsDao.getAlbumId(compositionId);
            if (existsAlbumId != null) {
                artistId = albumsDao.getArtistId(existsAlbumId);
            }
            if (artistId == null) {
                artistId = compositionsDao.getArtistId(compositionId);
            }

            // find new album by artist and name from albums
            Long albumId = albumsDao.findAlbum(artistId, albumName);

            // if album not exists - create album
            if (albumId == null && !TextUtils.isEmpty(albumName)) {
                albumId = albumsDao.insert(new AlbumEntity(artistId, albumName, 0, 0));
            }

            // set new albumId
            Long oldAlbumId = compositionsDao.getAlbumId(compositionId);
            compositionsDao.updateAlbum(compositionId, albumId);
            compositionsDao.setUpdateTime(compositionId, new Date());

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
            if (artistId == null && !TextUtils.isEmpty(authorName)) {
                artistId = artistsDao.insertArtist(authorName);
            }
            // 3) set new artistId
            Long oldArtistId = compositionsDao.getArtistId(id);
            compositionsDao.updateArtist(id, artistId);
            compositionsDao.setUpdateTime(id, new Date());

            // 4) if OLD artist exists and has no references - delete him
            if (oldArtistId != null) {
                artistsDao.deleteEmptyArtist(oldArtistId);
            }
        });
    }

    public void updateAlbumArtist(long id, String artistName) {
        appDatabase.runInTransaction(() -> {
            //find album
            Long albumId = compositionsDao.getAlbumId(id);
            if (albumId == null) {
                return;
            }
            // 1) find new artist by name from artists
            Long artistId = artistsDao.findArtistIdByName(artistName);

            // 2) if artist not exists - create artist
            if (artistId == null && !TextUtils.isEmpty(artistName)) {
                artistId = artistsDao.insertArtist(artistName);
            }

            AlbumEntity albumEntity = albumsDao.getAlbumEntity(albumId);
            Long oldArtistId = albumEntity.getArtistId();

            //find new album with author id and name
            Long newAlbumId = albumsDao.findAlbum(artistId, albumEntity.getName());
            //if not exists, create

            if (newAlbumId == null) {
                newAlbumId = albumsDao.insert(new AlbumEntity(
                        artistId,
                        albumEntity.getName(),
                        albumEntity.getFirstYear(),
                        albumEntity.getLastYear()
                ));
            }
            //set new album to composition
            compositionsDao.setAlbumId(id, newAlbumId);
            compositionsDao.setUpdateTime(id, new Date());

            //if album is empty, delete
            albumsDao.deleteEmptyAlbum(albumId);

            // 4) if OLD artist exists and has no references - delete him
            if (oldArtistId != null) {
                artistsDao.deleteEmptyArtist(oldArtistId);
            }

        });
    }

    public void updateTitle(long id, String title) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.updateTitle(id, title);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateLyrics(long id, String text) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.updateLyrics(id, text);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateModifyTime(long id, Date date) {
        compositionsDao.setUpdateTime(id, date);
    }

    public void updateModifyTimeAndSize(long id, long size, Date date) {
        compositionsDao.setModifyTimeAndSize(id, size, date);
    }

    public void updateCompositionFileName(long id, String fileName) {
        compositionsDao.updateCompositionFileName(id, fileName);
    }

    public void setCorruptionType(CorruptionType corruptionType, long id) {
        compositionsDao.setCorruptionType(corruptionType, id);
    }

    public Maybe<FullComposition> selectNextCompositionToScan(long lastCompleteScanTime) {
        return compositionsDao.selectNextCompositionToScan(lastCompleteScanTime);
    }

    public void setCompositionLastFileScanTime(FullComposition composition, Date time) {
        compositionsDao.setCompositionLastFileScanTime(composition.getId(), time);
    }

    public void cleanLastFileScanTime() {
        compositionsDao.cleanLastFileScanTime();
    }

    public void updateCompositionBySourceTags(FullComposition composition, CompositionSourceTags tags) {
        appDatabase.runInTransaction(() -> {
            long id = composition.getId();

            String tagTitle = tags.getTitle();
            if (!isEmpty(tagTitle) && !Objects.equals(composition.getTitle(), tagTitle)) {
                updateTitle(id, tagTitle);
            }

            String tagArtist = tags.getArtist();
            if (!isEmpty(tagArtist) && !Objects.equals(composition.getArtist(), tagArtist)) {
                updateArtist(id, tagArtist);
            }

            String tagAlbum = tags.getAlbum();
            if (!isEmpty(tagAlbum) && !Objects.equals(composition.getAlbum(), tagAlbum)) {
                updateAlbum(id, tagAlbum);
            }

            String tagAlbumArtist = tags.getAlbumArtist();
            if (!isEmpty(tagAlbumArtist) && !Objects.equals(composition.getAlbumArtist(), tagAlbumArtist)) {
                updateAlbumArtist(id, tagAlbumArtist);
            }

            String tagLyrics = tags.getLyrics();
            if (!isEmpty(tagLyrics) && !Objects.equals(composition.getLyrics(), tagLyrics)) {
                updateLyrics(id, tagLyrics);
            }
        });
    }

    @Nullable
    public Long getFolderId(long id) {
        return compositionsDao.getFolderId(id);
    }

    public Single<List<ExternalComposition>> getAllAsExternalCompositions() {
        return compositionsDao.getAllAsExternalCompositions();
    }

    public Long findCompositionIdByFilePath(String parentPath, String fileName) {
        Long folderId = findFolderId(parentPath);
        long id = compositionsDao.findCompositionByFileName(fileName, folderId);
        if (id == 0) {
            throw new CompositionNotFoundException(fileName + " not found");
        }
        return id;
    }

    public Pair<String, String> getCompositionNameAndPath(long id) {
        String fileName = compositionsDao.getCompositionFileName(id);
        if (fileName == null) {
            throw new CompositionNotFoundException("composition not found");
        }
        String parentPath = compositionsDao.getCompositionParentPath(id);
        return new Pair<>(fileName, parentPath);
    }

    public long getCompositionSize(long id) {
        return compositionsDao.getCompositionSize(id);
    }

    public void updateCompositionsInitialSource(List<Composition> compositions,
                                                InitialSource initialSource,
                                                InitialSource updateFrom) {
        appDatabase.runInTransaction(() -> {
            for (Composition composition: compositions) {
                updateCompositionInitialSource(composition.getId(), initialSource, updateFrom);
            }
        });
    }

    public void updateCompositionIdsInitialSource(List<Long> compositionsIds,
                                                  InitialSource initialSource,
                                                  InitialSource updateFrom) {
        appDatabase.runInTransaction(() -> {
            for (long id: compositionsIds) {
                updateCompositionInitialSource(id, initialSource, updateFrom);
            }
        });
    }

    public void updateCompositionInitialSource(long id,
                                               InitialSource initialSource,
                                               InitialSource updateFrom) {
        compositionsDao.updateCompositionInitialSource(id, initialSource, updateFrom);
    }

    @Nullable
    private Long findFolderId(String filePath) {
        return findFolderId(filePath, null);
    }

    @Nullable
    private Long findFolderId(String filePath, @Nullable Long parentId) {
        if (filePath.isEmpty()) {
            return parentId;
        }

        Long folderId;
        int delimiterIndex = filePath.indexOf('/');
        if (delimiterIndex == -1) {
            folderId = foldersDao.getFolderByName(parentId, filePath);
        } else {
            String folderName = filePath.substring(0, delimiterIndex);
            Long parentFolderId = foldersDao.getFolderByName(parentId, folderName);
            if (parentFolderId == null) {
                return null;
            }
            String folderPath = filePath.substring(delimiterIndex + 1);
            folderId = findFolderId(folderPath, parentFolderId);
        }
        return folderId;
    }

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case NAME: {
                orderQuery.append("CASE WHEN title IS NULL OR title = '' THEN fileName ELSE title END");
                break;
            }
            case FILE_NAME: {
                orderQuery.append("fileName");
                break;
            }
            case ADD_TIME: {
                orderQuery.append("dateAdded");
                break;
            }
            case SIZE: {
                orderQuery.append("size");
                break;
            }
            case DURATION: {
                orderQuery.append("duration");
                break;
            }
            default: throw new IllegalStateException("unknown order type" + order);
        }
        orderQuery.append(" ");
        orderQuery.append(order.isReversed()? "DESC" : "ASC");
        return orderQuery.toString();
    }
}
