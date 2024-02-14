package com.github.anrimian.musicplayer.data.database.dao.compositions;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs;
import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;

import androidx.collection.LongSparseArray;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.LibraryDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.models.composition.ExternalComposition;
import com.github.anrimian.musicplayer.data.models.exceptions.CompositionNotFoundException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.models.CompositionMoveData;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource;
import com.github.anrimian.musicplayer.domain.models.composition.change.CompositionPath;
import com.github.anrimian.musicplayer.domain.models.composition.tags.AudioFileInfo;
import com.github.anrimian.musicplayer.domain.models.composition.tags.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.CollectionUtilsKt;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CompositionsDaoWrapper {

    private final LibraryDatabase libraryDatabase;
    private final CompositionsDao compositionsDao;
    private final ArtistsDao artistsDao;
    private final AlbumsDao albumsDao;
    private final GenreDao genreDao;
    private final FoldersDao foldersDao;

    private final BehaviorSubject<Object> updateSubject = BehaviorSubject.createDefault(TRIGGER);

    public CompositionsDaoWrapper(LibraryDatabase libraryDatabase,
                                  ArtistsDao artistsDao,
                                  CompositionsDao compositionsDao,
                                  AlbumsDao albumsDao,
                                  GenreDao genreDao,
                                  FoldersDao foldersDao) {
        this.libraryDatabase = libraryDatabase;
        this.artistsDao = artistsDao;
        this.compositionsDao = compositionsDao;
        this.albumsDao = albumsDao;
        this.genreDao = genreDao;
        this.foldersDao = foldersDao;
    }

    public Observable<Composition> getCompositionObservable(long id, boolean useFileName) {
        StringBuilder query = CompositionsDao.getCompositionQuery(useFileName);
        query.append(" WHERE id = ? LIMIT 1");
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query.toString(),
                new String[]{ String.valueOf(id) });
        return compositionsDao.getCompositionsObservable(sqlQuery)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public Observable<FullComposition> getFullCompositionObservable(long id) {
        return compositionsDao.getFullCompositionObservable(id)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public Observable<String> getLyricsObservable(long id) {
        return compositionsDao.getLyricsObservable(id);
    }

    public FullComposition getFullComposition(long id) {
        return compositionsDao.getFullComposition(id);
    }

    public CompositionMoveData getCompositionMoveData(long id) {
        return compositionsDao.getCompositionMoveData(id);
    }

    public List<CompositionMoveData> getCompositionsMoveData(List<Long> ids) {
        return ListUtils.mapList(ids, compositionsDao::getCompositionMoveData);
    }

    public Observable<List<Composition>> getAllObservable(Order order,
                                                          boolean useFileName,
                                                          @Nullable String searchText) {
        StringBuilder query = CompositionsDao.getCompositionQuery(useFileName);
        query.append(CompositionsDao.getSearchWhereQuery(useFileName));
        query.append(getOrderQuery(order));
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query.toString(), getSearchArgs(searchText, 3));
        return updateSubject.switchMap(o -> compositionsDao.getCompositionsObservable(sqlQuery));
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
        query.append(" AND (? IS NOT NULL OR ");
        query.append("(folderId = ");
        query.append(folderId);
        query.append(" OR (folderId IS NULL AND ");
        query.append(folderId);
        query.append(" IS NULL)))");
        query.append(getOrderQuery(order));
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query.toString(), getSearchArgs(searchText, 4));
        return compositionsDao.getCompositionsInFolderObservable(sqlQuery);
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

    public List<CompositionMoveData> getAllCompositionsInFolder(Long parentFolderId) {
        String query = FoldersDao.getRecursiveFolderQuery(parentFolderId);
        query += CompositionsDao.getMoveCompositionQuery();
        query += " WHERE folderId IN (SELECT childFolderId FROM allChildFolders) ";
        query += "OR folderId = ";
        query += parentFolderId;
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return compositionsDao.executeQueryForMove(sqlQuery);
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
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.delete(id);
            albumsDao.deleteEmptyAlbums();
            artistsDao.deleteEmptyArtists();
            genreDao.deleteEmptyGenres();
            foldersDao.deleteFoldersWithoutContainment();
        });
    }

    public void deleteAll(Long[] ids) {
        libraryDatabase.runInTransaction(() -> {
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

    public void replaceFolderId(long fromFolderId, Long folderId) {
        compositionsDao.replaceFolderId(fromFolderId, folderId);
    }

    public void updateStorageId(long id, Long storageId) {
        compositionsDao.updateStorageId(id, storageId);
    }

    public void updateAlbum(long compositionId, @Nullable String albumName) {
        libraryDatabase.runInTransaction(() -> {

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
                albumId = albumsDao.insertAlbum(artistId, albumName);
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
        libraryDatabase.runInTransaction(() -> {
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
        libraryDatabase.runInTransaction(() -> {
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
                newAlbumId = albumsDao.insertAlbum(artistId, albumEntity.getName());
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

    public void setCompositionGenres(long compositionId, String[] genres) {
        genreDao.removeCompositionGenres(compositionId);
        for(String genre: genres) {
            Long genreId = genreDao.findGenre(genre);
            if (genreId == null) {
                genreId = genreDao.insertGenre(genre);
            }
            genreDao.insertGenreEntry(compositionId, genreId);
        }
    }

    public void updateTitle(long id, String title) {
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.updateTitle(id, title);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateDuration(long id, long duration) {
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.updateDuration(id, duration);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateTrackNumber(long id, Long trackNumber) {
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.updateTrackNumber(id, trackNumber);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateDiscNumber(long id, Long discNumber) {
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.updateDiscNumber(id, discNumber);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateComment(long id, String text) {
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.updateComment(id, text);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateLyrics(long id, String text) {
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.updateLyrics(id, text);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateFileSize(long id, long fileSize) {
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.updateFileSize(id, fileSize);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateModifyTime(long id, Date date) {
        compositionsDao.setUpdateTime(id, date);
    }

    public void updateCoverModifyTimeAndSize(long id, long size, Date date) {
        compositionsDao.setCoverModifyTimeAndSize(id, size, date);
    }

    public void updateCoverModifyTime(long id, long time) {
        compositionsDao.setCoverModifyTime(id, time);
    }

    public void updateCompositionFileName(long id, String fileName) {
        compositionsDao.updateCompositionFileName(id, fileName);
    }

    public void setCorruptionType(CorruptionType corruptionType, long id) {
        compositionsDao.setCorruptionType(corruptionType, id);
    }

    public Single<List<FullComposition>> selectNextCompositionsToScan(long lastCompleteScanTime,
                                                                      int filesCount) {
        return compositionsDao.selectNextCompositionsToScan(lastCompleteScanTime, filesCount);
    }

    public void setCompositionLastFileScanTime(FullComposition composition, Date time) {
        compositionsDao.setCompositionLastFileScanTime(composition.getId(), time);
    }

    public void cleanLastFileScanTime() {
        compositionsDao.cleanLastFileScanTime();
    }

    public void updateCompositionsByFileInfo(
            List<kotlin.Pair<FullComposition, AudioFileInfo>> scannedCompositions,
            List<FullComposition> allCompositions
    ) {
        libraryDatabase.runInTransaction(() -> {
            for (kotlin.Pair<FullComposition, AudioFileInfo> scannedComposition: scannedCompositions) {
                updateCompositionByFileInfo(scannedComposition.getFirst(), scannedComposition.getSecond());
            }
            Date currentDate = new Date();
            for (FullComposition composition: allCompositions) {
                setCompositionLastFileScanTime(composition, currentDate);
            }
        });
    }

    public void updateCompositionByFileInfo(FullComposition composition, AudioFileInfo fileInfo) {
        libraryDatabase.runInTransaction(() -> {
            long id = composition.getId();
            CompositionSourceTags tags = fileInfo.getAudioTags();

            boolean wasChanges = false;

            String tagTitle = tags.getTitle();
            if (!isEmpty(tagTitle) && !Objects.equals(composition.getTitle(), tagTitle)) {
                compositionsDao.updateTitle(id, tagTitle);
                wasChanges = true;
            }

            String tagArtist = tags.getArtist();
            if (!isEmpty(tagArtist) && !Objects.equals(composition.getArtist(), tagArtist)) {
                updateArtist(id, tagArtist);
                wasChanges = true;
            }

            String tagAlbum = tags.getAlbum();
            if (!isEmpty(tagAlbum) && !Objects.equals(composition.getAlbum(), tagAlbum)) {
                updateAlbum(id, tagAlbum);
                wasChanges = true;
            }

            String tagAlbumArtist = tags.getAlbumArtist();
            if (!isEmpty(tagAlbumArtist) && !Objects.equals(composition.getAlbumArtist(), tagAlbumArtist)) {
                updateAlbumArtist(id, tagAlbumArtist);
                wasChanges = true;
            }

            //if we just update duration, we'll lose milliseconds part. So just update 0 values
            int tagDuration = tags.getDurationSeconds();
            long duration = composition.getDuration();
            if (duration == 0L && tagDuration != 0) {
                long tagDurationMillis = tagDuration * 1000L;
                compositionsDao.updateDuration(id, tagDurationMillis);
                wasChanges = true;
            }

            Long tagTrackNumber = tags.getTrackNumber();
            if (!Objects.equals(composition.getTrackNumber(), tagTrackNumber)) {
                compositionsDao.updateTrackNumber(id, tagTrackNumber);
                wasChanges = true;
            }

            Long tagDiscNumber = tags.getDiscNumber();
            if (!Objects.equals(composition.getDiscNumber(), tagDiscNumber)) {
                compositionsDao.updateDiscNumber(id, tagDiscNumber);
                wasChanges = true;
            }

            String tagComment = tags.getComment();
            if (!isEmpty(tagComment) && !Objects.equals(composition.getComment(), tagComment)) {
                compositionsDao.updateComment(id, tagComment);
                wasChanges = true;
            }

            String tagLyrics = tags.getLyrics();
            if (!isEmpty(tagLyrics) && !Objects.equals(composition.getLyrics(), tagLyrics)) {
                compositionsDao.updateLyrics(id, tagLyrics);
                wasChanges = true;
            }
            String[] tagGenres = tags.getGenres();
            String[] compositionGenres = CompositionHelper.splitGenres(composition.getGenres());
            if (!Arrays.equals(compositionGenres, tagGenres)) {
                setCompositionGenres(id, tagGenres);
                wasChanges = true;
            }

            long fileSize = fileInfo.getFileSize();
            if (composition.getSize() != fileSize) {
                compositionsDao.updateFileSize(id, fileSize);
                wasChanges = true;
            }

            if (wasChanges) {
                compositionsDao.setUpdateTime(id, new Date());
            }
        });
    }

    @Nullable
    public Long getFolderId(long id) {
        return compositionsDao.getFolderId(id);
    }

    public List<ExternalComposition> getAllAsExternalCompositions(String parentPath) {
        Long folderId = findFolderId(parentPath);
        return compositionsDao.getAllAsExternalCompositions(folderId);
    }

    @Nullable
    public Long findCompositionIdByFilePath(String parentPath, String fileName) {
        Long folderId = findFolderId(parentPath);
        return compositionsDao.findCompositionByFileName(fileName, folderId);
    }

    public long requireCompositionIdByFilePath(String parentPath, String fileName) {
        Long id = findCompositionIdByFilePath(parentPath, fileName);
        if (id == null) {
            throw new CompositionNotFoundException(fileName + " not found");
        }
        return id;
    }

    public CompositionPath getCompositionNameAndPath(long id) {
        String fileName = compositionsDao.getCompositionFileName(id);
        if (fileName == null) {
            throw new CompositionNotFoundException("composition not found");
        }
        String parentPath = compositionsDao.getCompositionParentPath(id);
        return new CompositionPath(fileName, parentPath);
    }

    public long getCompositionSize(long id) {
        return compositionsDao.getCompositionSize(id);
    }

    public void updateCompositionIdsInitialSource(List<Long> compositionsIds,
                                                  InitialSource initialSource,
                                                  InitialSource updateFrom) {
        libraryDatabase.runInTransaction(() -> {
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

    public List<DeletedComposition> selectDeletedComposition(Long[] ids, boolean useFileName) {
        String query = CompositionsDao.getDeletedCompositionQuery(useFileName, ids.length).toString();
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, ids);
        return compositionsDao.selectDeletedComposition(sqlQuery);
    }

    public DeletedComposition selectDeletedComposition(Long id, boolean useFileName) {
        return selectDeletedComposition(new Long[]{ id }, useFileName).get(0);
    }

    public List<Long> getCompositionIds(List<PlayListEntry> fileEntries,
                                        HashMap<String, Long> pathIdMapCache) {
        return ListUtils.mapListNotNull(fileEntries, entry -> {
            String path = entry.getFilePath();
            return CollectionUtilsKt.getOrPut(pathIdMapCache, path, () -> {
                String parentPath = FileUtils.getParentDirPath(path);
                String fileName = FileUtils.getFileName(path);
                long[] nameIds = compositionsDao.findCompositionsByFileName(fileName);
                for (long nameId: nameIds) {
                    String dbPath = compositionsDao.getCompositionParentPath(nameId);
                    if (parentPath.endsWith(dbPath)) {
                        return nameId;
                    }
                }
                return null;
            });
        });
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
