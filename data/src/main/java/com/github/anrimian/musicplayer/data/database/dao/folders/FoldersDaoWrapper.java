package com.github.anrimian.musicplayer.data.database.dao.folders;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.domain.models.composition.folders.CompositionFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

public class FoldersDaoWrapper {

    private final FoldersDao foldersDao;
    private final CompositionsDaoWrapper compositionsDao;

    public FoldersDaoWrapper(FoldersDao foldersDao, CompositionsDaoWrapper compositionsDao) {
        this.foldersDao = foldersDao;
        this.compositionsDao = compositionsDao;
    }

    @Nullable
    public Long getFolderIdToInsert(String filePath) {
        return null;
    }

    public Observable<List<FileSource2>> getFilesObservable(Long parentFolderId) {
        Observable<List<FolderFileSource2>> folderObservable =
                foldersDao.getFolderObservable(parentFolderId);

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

    public String[] getIgnoredFolders() {
        return new String[0];
    }

}
