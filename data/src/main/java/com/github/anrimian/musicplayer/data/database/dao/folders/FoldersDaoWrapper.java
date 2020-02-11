package com.github.anrimian.musicplayer.data.database.dao.folders;


import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.IgnoredFolderEntity;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.FolderNode;
import com.github.anrimian.musicplayer.domain.models.composition.folders.CompositionFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.IgnoredFolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

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

    public Observable<List<FileSource2>> getFilesObservable(Long parentFolderId) {
        Observable<List<FolderFileSource2>> folderObservable =
                foldersDao.getFoldersObservable(parentFolderId);

        Observable<List<CompositionFileSource2>> compositionsObservable =
                compositionsDao.getCompositionsInFolderObservable(parentFolderId)
                        .map(list -> mapList(list, CompositionFileSource2::new));

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

    public List<FolderEntity> getAllFolders() {
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

    private void insertNode(Long parentId,
                            FolderNode<Long> nodeToInsert,
                            LongSparseArray<Long> compositionsIdMap) {
        for (Long id : nodeToInsert.getFiles()) {
            compositionsIdMap.put(id, parentId);
        }
        String name = nodeToInsert.getKeyPath();
        if (name == null) {
            return;
        }

        long id = foldersDao.insertFolder(new FolderEntity(parentId, name));
        for (FolderNode<Long> node: nodeToInsert.getFolders()) {
            insertNode(id, node, compositionsIdMap);
        }
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

}
