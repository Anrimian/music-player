package com.github.anrimian.musicplayer.data.database.dao.folders;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.folder.IgnoredFolderEntity;
import com.github.anrimian.musicplayer.domain.models.composition.folders.IgnoredFolder;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface FoldersDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(IgnoredFolderEntity entity);

    @Query("SELECT relativePath FROM ignored_folders")
    String[] getIgnoredFolders();

    @Query("SELECT relativePath, addDate FROM ignored_folders ORDER BY addDate")
    Observable<List<IgnoredFolder>> getIgnoredFoldersObservable();

    @Query("DELETE FROM ignored_folders WHERE relativePath = :path")
    void deleteIgnoredFolder(String path);
}
