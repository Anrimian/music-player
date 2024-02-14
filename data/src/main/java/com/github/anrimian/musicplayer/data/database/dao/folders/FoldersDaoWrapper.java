package com.github.anrimian.musicplayer.data.database.dao.folders;


import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.LibraryDatabase;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.repositories.library.edit.models.CompositionMoveData;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class FoldersDaoWrapper {

    private final LibraryDatabase libraryDatabase;
    private final FoldersDao foldersDao;
    private final CompositionsDaoWrapper compositionsDao;

    public FoldersDaoWrapper(LibraryDatabase libraryDatabase,
                             FoldersDao foldersDao,
                             CompositionsDaoWrapper compositionsDao) {
        this.libraryDatabase = libraryDatabase;
        this.foldersDao = foldersDao;
        this.compositionsDao = compositionsDao;
    }

    public Observable<List<FileSource>> getFilesObservable(Long parentFolderId,
                                                           Order order,
                                                           boolean useFileName,
                                                           @Nullable String searchText) {
        Observable<List<FolderFileSource>> folderObservable = getFoldersObservable(
                parentFolderId,
                order,
                searchText);

        Observable<List<CompositionFileSource>> compositionsObservable =
                compositionsDao.getCompositionsInFolderObservable(
                        parentFolderId,
                        order,
                        useFileName,
                        searchText
                ).map(list -> mapList(list, CompositionFileSource::new));

        return Observable.combineLatest(folderObservable,
                compositionsObservable,
                (folders, compositions) -> {
                    List<FileSource> list = new ArrayList<>(folders.size() + compositions.size());
                    list.addAll(folders);
                    list.addAll(compositions);
                    return list;
                });
    }

    public Observable<FolderFileSource> getFolderObservable(long folderId) {
        return foldersDao.getFolderObservable(folderId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public Single<List<Long>> extractAllCompositionsFromFiles(Iterable<FileSource> fileSources) {
        return Observable.fromIterable(fileSources)
                .flatMap(this::fileSourceToCompositionId)
                .collect(ArrayList::new, List::add);
    }
    public Single<List<Composition>> extractAllCompositionsFromFiles(Iterable<FileSource> fileSources,
                                                                     Order order,
                                                                     boolean useFileName) {
        return Observable.fromIterable(fileSources)
                .flatMap(fileSource -> fileSourceToComposition(fileSource, order, useFileName))
                .collect(ArrayList::new, List::add);
    }

    public List<Composition> getAllCompositionsInFolder(Long parentFolderId, Order order, boolean useFileName) {
        List<Composition> result = new LinkedList<>();

        String query = FoldersDao.getRecursiveFolderQuery(parentFolderId) +
                "SELECT id " +
                "FROM folders " +
                "WHERE parentId = " + parentFolderId + " OR (parentId IS NULL AND " + parentFolderId + " IS NULL)";
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        List<Long> folders = foldersDao.getFoldersIds(sqlQuery);
        for (Long id: folders) {
            result.addAll(getAllCompositionsInFolder(id, order, useFileName));
        }

        result.addAll(compositionsDao.getCompositionsInFolder(parentFolderId, order, useFileName));
        return result;
    }

    public void deleteFolder(Long folderId, Long[] childCompositions) {
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.deleteAll(childCompositions);
            foldersDao.deleteFolder(folderId);
        });
    }

    public void deleteFolders(List<Long> folders, Long[] childCompositions) {
        libraryDatabase.runInTransaction(() -> {
            compositionsDao.deleteAll(childCompositions);
            foldersDao.deleteFolders(folders);
        });
    }

    public void changeFolderName(long folderId, String newName) {
        Long parentId = foldersDao.getFolderParentId(folderId);
        Long existingFolderId = foldersDao.getFolderByName(parentId, newName);
        foldersDao.changeFolderName(folderId, newName);
        if (existingFolderId != null) {
            //move all references out from existing folder and delete it
            foldersDao.replaceParentId(existingFolderId, folderId);
            compositionsDao.replaceFolderId(existingFolderId, folderId);
            foldersDao.deleteFolder(existingFolderId);
        }
    }

    public String getFullFolderPath(long folderId) {
        return foldersDao.getFullFolderPath(folderId);
    }

    @NonNull
    public String getFolderRelativePath(@Nullable Long folderId) {
        return folderId == null? "" : foldersDao.getFullFolderPath(folderId);
    }

    public List<Long> getAllParentFoldersId(@Nullable Long currentFolder) {
        List<Long> result = new LinkedList<>();
        result.add(null);//null folder is always first
        if (currentFolder != null) {
            result.addAll(foldersDao.getAllParentFoldersId(currentFolder));
        }
        return result;
    }

    public void updateFolderId(Collection<FileSource> files, @Nullable Long toFolderId) {
        libraryDatabase.runInTransaction(() -> {
            for (FileSource fileSource: files) {
                if (fileSource instanceof CompositionFileSource) {
                    long id = ((CompositionFileSource) fileSource).getComposition().getId();
                    compositionsDao.updateFolderId(id, toFolderId);
                }
                if (fileSource instanceof FolderFileSource) {
                    long id = ((FolderFileSource) fileSource).getId();
                    foldersDao.updateParentId(id, toFolderId);
                }
            }
            deleteEmptyFolders();
        });
    }

    public long moveCompositionsIntoFolder(@Nullable Long parentFolderId,
                                           String directoryName,
                                           Collection<FileSource> files) {
        return libraryDatabase.runInTransaction(() -> {
            Long folderId = foldersDao.getFolderByName(parentFolderId, directoryName);
            if (folderId == null) {
                folderId = createFolder(parentFolderId, directoryName);
            }
            updateFolderId(files, folderId);
            return folderId;
        });
    }

    public long createFolder(Long parentId, String name) {
        return foldersDao.insertFolder(new FolderEntity(parentId, name));
    }

    public boolean isFolderWithNameExists(Long parentId, String name) {
        return foldersDao.isFolderWithNameExists(parentId, name);
    }

    public void deleteEmptyFolders() {
        int deletedRows;
        do {
            deletedRows = foldersDao.deleteFoldersWithoutContainment();
        } while (deletedRows != 0);
    }

    @Nullable
    public Long getOrCreateFolder(String filePath, Map<String, Long> folderCache) {
        return getOrCreateFolder(filePath, filePath, null, folderCache);
    }

    @Nullable
    private Long getOrCreateFolder(String fullPath,
                                   String filePath,
                                   @Nullable Long parentId,
                                   Map<String, Long> folderCache) {
        if (filePath.isEmpty()) {
            return parentId;
        }

        Long cachedId = folderCache.get(fullPath);
        if (cachedId != null) {
            return cachedId;
        }

        Long folderId;
        int delimiterIndex = filePath.indexOf('/');
        if (delimiterIndex == -1) {
            folderId = getFolderId(filePath, parentId);
        } else {
            String folderName = filePath.substring(0, delimiterIndex);
            long parentFolderId = getFolderId(folderName, parentId);
            String folderPath = filePath.substring(delimiterIndex + 1);
            folderId = getOrCreateFolder(fullPath, folderPath, parentFolderId, folderCache);
        }
        folderCache.put(fullPath, folderId);
        return folderId;
    }

    private long getFolderId(String folderName, @Nullable Long parentId) {
        Long folderId = foldersDao.getFolderByName(parentId, folderName);
        if (folderId != null) {
            return folderId;
        }
        return foldersDao.insertFolder(new FolderEntity(parentId, folderName));
    }

    private Observable<List<FolderFileSource>> getFoldersObservable(Long parentFolderId,
                                                                    Order order,
                                                                    @Nullable String searchText) {
        boolean selectAllFolders = !TextUtils.isEmpty(searchText);
        String query = FoldersDao.getRecursiveFolderQuery(parentFolderId, selectAllFolders) +
                ", childCompositions(storageId, initialSource) AS (SELECT storageId, initialSource FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))" +
                "SELECT id, name, " +
                "(SELECT count() FROM childCompositions) as filesCount, " +
                "(SELECT exists(SELECT 1 FROM childCompositions WHERE storageId IS NOT NULL AND initialSource = 1 LIMIT 1)) as hasAnyStorageFile " +
                "FROM folders " +
                "WHERE (? IS NULL AND (parentId = " + parentFolderId + " OR (parentId IS NULL AND " + parentFolderId + " IS NULL)))";

        query += getSearchQuery();
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, getSearchArgs(searchText, 3));
        return foldersDao.getFoldersObservable(sqlQuery);
    }

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case NAME:
            case FILE_NAME: {
                orderQuery.append("name");
                break;
            }
            case ADD_TIME: {
                orderQuery.append("(SELECT max(dateAdded) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))");
                break;
            }
            case DURATION: {
                orderQuery.append("(SELECT sum(duration) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))");
                break;
            }
            case SIZE: {
                orderQuery.append("(SELECT sum(size) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))");
                break;
            }
            default: throw new IllegalStateException("unknown order type" + order);
        }
        orderQuery.append(" ");
        orderQuery.append(order.isReversed()? "DESC" : "ASC");
        return orderQuery.toString();
    }

    private String getSearchQuery() {
        return " OR (? IS NOT NULL AND name LIKE ?)";
    }

    private Observable<Composition> fileSourceToComposition(FileSource fileSource, Order order, boolean useFileName) {
        if (fileSource instanceof CompositionFileSource) {
            return Observable.just(((CompositionFileSource) fileSource).getComposition());
        }
        if (fileSource instanceof FolderFileSource) {
            return Observable.fromIterable(getAllCompositionsInFolder(
                    ((FolderFileSource) fileSource).getId(),
                    order,
                    useFileName
            ));
        }
        throw new IllegalStateException("unexpected file source: " + fileSource);
    }

    private Observable<Long> fileSourceToCompositionId(FileSource fileSource) {
        if (fileSource instanceof CompositionFileSource) {
            return Observable.just(((CompositionFileSource) fileSource).getComposition().getId());
        }
        if (fileSource instanceof FolderFileSource) {
            return Observable.fromIterable(ListUtils.mapList(
                    compositionsDao.getAllCompositionsInFolder(((FolderFileSource) fileSource).getId()),
                    CompositionMoveData::getId)
            );
        }
        throw new IllegalStateException("unexpected file source: " + fileSource);
    }

}
