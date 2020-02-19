package com.github.anrimian.musicplayer.data.database.dao.folders;


import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.IgnoredFolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.domain.models.composition.folders.CompositionFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class FoldersDaoWrapper {

    private final AppDatabase appDatabase;
    private final FoldersDao foldersDao;
    private final CompositionsDaoWrapper compositionsDao;

    public FoldersDaoWrapper(AppDatabase appDatabase,
                             FoldersDao foldersDao,
                             CompositionsDaoWrapper compositionsDao) {
        this.appDatabase = appDatabase;
        this.foldersDao = foldersDao;
        this.compositionsDao = compositionsDao;
    }

    @Nullable
    public Long getFolderIdToInsert(String filePath) {
        return null;
    }

    public Observable<List<FileSource2>> getFilesObservable(Long parentFolderId,
                                                            Order order,
                                                            @Nullable String searchText) {
        Observable<List<FolderFileSource2>> folderObservable = getFoldersObservable(
                parentFolderId,
                order,
                searchText);

        Observable<List<CompositionFileSource2>> compositionsObservable =
                compositionsDao.getCompositionsInFolderObservable(
                        parentFolderId,
                        order,
                        searchText
                ).map(list -> mapList(list, CompositionFileSource2::new));

        return Observable.combineLatest(folderObservable,
                compositionsObservable,
                (folders, compositions) -> {
                    List<FileSource2> list = new ArrayList<>(folders.size() + compositions.size());
                    list.addAll(folders);
                    list.addAll(compositions);
                    return list;
                });
    }

    public Observable<FolderFileSource2> getFolderObservable(long folderId) {
        return foldersDao.getFolderObservable(folderId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public List<StorageFolder> getAllFolders() {
        return foldersDao.getAllFolders();
    }

    public IgnoredFolder insert(String path) {
        Date addDate = new Date();
        foldersDao.insert(new IgnoredFolderEntity(path, addDate));
        return new IgnoredFolder(path, addDate);
    }

    public void insert(IgnoredFolder folder) {
        foldersDao.insert(new IgnoredFolderEntity(folder.getRelativePath(), folder.getAddDate()));
    }

    public LongSparseArray<Long> insertFolders(List<AddedNode> foldersToInsert) {
        return appDatabase.runInTransaction(() -> {
            LongSparseArray<Long> compositionsIdMap = new LongSparseArray<>();
            for (AddedNode node: foldersToInsert) {
                insertNode(node.getFolderDbId(), node.getNode(), compositionsIdMap);
            }
            return compositionsIdMap;
        });
    }

    public void deleteFolders(List<Long> ids) {
        foldersDao.deleteFolders(ids);
    }

    public String[] getIgnoredFolders() {
        return foldersDao.getIgnoredFolders();
    }

    public Observable<List<IgnoredFolder>> getIgnoredFoldersObservable() {
        return foldersDao.getIgnoredFoldersObservable();
    }

    public void deleteIgnoredFolder(IgnoredFolder folder) {
        foldersDao.deleteIgnoredFolder(folder.getRelativePath());
    }

    private Observable<List<FolderFileSource2>> getFoldersObservable(Long parentFolderId,
                                                                     Order order,
                                                                     @Nullable String searchText) {
        String query = "WITH RECURSIVE allChildFolders(childFolderId, rootFolderId) AS (" +
                "SELECT id as childFolderId, id as rootFolderId FROM folders WHERE parentId = " + parentFolderId + " OR (parentId IS NULL AND " + parentFolderId + " IS NULL)" +
                "UNION " +
                "SELECT id as childFolderId, allChildFolders.rootFolderId as rootFolderId FROM folders INNER JOIN allChildFolders ON parentId = allChildFolders.childFolderId" +
                ")" +
                "SELECT id, name, " +
                "(SELECT count() FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id)) as filesCount " +
                "FROM folders " +
                "WHERE parentId = " + parentFolderId + " OR (parentId IS NULL AND " + parentFolderId + " IS NULL)";

        query += getSearchQuery(searchText);
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return foldersDao.getFoldersObservable(sqlQuery);
    }

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case ALPHABETICAL: {
                orderQuery.append("name");
                break;
            }
            case ADD_TIME: {
                orderQuery.append("(SELECT max(dateAdded) FROM compositions WHERE folderId IN (SELECT childFolderId FROM allChildFolders WHERE rootFolderId = folders.id))");
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
        StringBuilder sb = new StringBuilder(" AND ");
        sb.append("name NOTNULL AND name LIKE '%");
        sb.append(searchText);
        sb.append("%'");

        return sb.toString();
    }

    private void insertNode(Long dbParentId,
                            FolderNode<Long> nodeToInsert,
                            LongSparseArray<Long> compositionsIdMap) {
        String name = nodeToInsert.getKeyPath();
        if (name == null) {
            return;
        }

        long id = foldersDao.insertFolder(new FolderEntity(dbParentId, name));
        for (Long compositionId : nodeToInsert.getFiles()) {
            compositionsIdMap.put(compositionId, id);
        }
        for (FolderNode<Long> node: nodeToInsert.getFolders()) {
            insertNode(id, node, compositionsIdMap);
        }
    }

}
