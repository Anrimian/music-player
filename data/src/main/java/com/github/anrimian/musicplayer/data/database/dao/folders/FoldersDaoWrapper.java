package com.github.anrimian.musicplayer.data.database.dao.folders;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.data.database.entities.folder.IgnoredFolderEntity;
import com.github.anrimian.musicplayer.domain.models.composition.folders.IgnoredFolder;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

public class FoldersDaoWrapper {

    private final FoldersDao foldersDao;

    public FoldersDaoWrapper(FoldersDao foldersDao) {
        this.foldersDao = foldersDao;
    }

    @Nullable
    public Long getFolderIdToInsert(String filePath) {
        return null;
    }

    public IgnoredFolder insert(String path) {
        Date addDate = new Date();
        foldersDao.insert(new IgnoredFolderEntity(path, addDate));
        return new IgnoredFolder(path, addDate);
    }

    public void insert(IgnoredFolder folder) {
        foldersDao.insert(new IgnoredFolderEntity(folder.getRelativePath(), folder.getAddDate()));
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
