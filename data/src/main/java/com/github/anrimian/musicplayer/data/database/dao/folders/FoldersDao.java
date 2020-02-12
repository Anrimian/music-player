package com.github.anrimian.musicplayer.data.database.dao.folders;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.IgnoredFolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;
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

    /*    WITH RECURSIVE allChildFolders(childFolderId) AS (
    SELECT id FROM folders WHERE parentId = 1
    UNION
    SELECT id FROM folders, allChildFolders WHERE parentId = allChildFolders.childFolderId
)
    SELECT parentId, id, name,
(SELECT count() FROM compositions WHERE folderId IN allChildFolders) as filesCount
    FROM folders, allChildFolders
    WHERE parentId IS NULL AND id = 1*/

    @Query("SELECT id, name, " +
            "(SELECT count() FROM compositions WHERE folderId = folders.id) as filesCount " +//not right, select recursive
            "FROM folders " +
            "WHERE parentId = :parentId OR (parentId IS NULL AND :parentId IS NULL) ")//+ order by - later
    Observable<List<FolderFileSource2>> getFoldersObservable(Long parentId);

    @Query("SELECT id, name, " +
            "(SELECT count() FROM compositions WHERE folderId = folders.id) as filesCount " +
            "FROM folders " +
            "WHERE id = :folderId OR (id IS NULL AND :folderId IS NULL) " +
            "LIMIT 1")
    Observable<List<FolderFileSource2>> getFolderObservable(Long folderId);

    @Query("SELECT * FROM folders")
    List<StorageFolder> getAllFolders();

    @Insert
    long insertFolder(FolderEntity entity);
}
