package com.github.anrimian.musicplayer.data.database.dao.folders;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface FoldersDao {

    @Query("SELECT id, name FROM folders WHERE parentId = :parentId")//+ order by - later
    Observable<List<FolderFileSource2>> getFoldersObservable(Long parentId);

    @Query("SELECT id, name FROM folders WHERE id = :folderId LIMIT 1")
    Observable<List<FolderFileSource2>> getFolderObservable(long folderId);

    @Query("SELECT * FROM folders")
    List<FolderEntity> getAllFolders();

    @Insert
    long insertFolder(FolderEntity entity);
}
