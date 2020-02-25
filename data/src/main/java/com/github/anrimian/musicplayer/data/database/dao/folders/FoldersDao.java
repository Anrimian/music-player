package com.github.anrimian.musicplayer.data.database.dao.folders;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
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

    @RawQuery(observedEntities = { CompositionEntity.class, FolderEntity.class })
    Observable<List<FolderFileSource2>> getFoldersObservable(SupportSQLiteQuery query);

    @Query("SELECT id, name, " +
            "0 as filesCount " +//we don't use it for now
            "FROM folders " +
            "WHERE id = :folderId OR (id IS NULL AND :folderId IS NULL) " +
            "LIMIT 1")
    Observable<List<FolderFileSource2>> getFolderObservable(Long folderId);

    @Query("SELECT * FROM folders")
    List<StorageFolder> getAllFolders();

    @Insert
    long insertFolder(FolderEntity entity);

    @Query("DELETE FROM folders WHERE id IN(:ids)")
    void deleteFolders(List<Long> ids);

    @Query("DELETE FROM folders WHERE id = :id")
    void deleteFolder(Long id);

    @Query("UPDATE folders SET name = :newName WHERE id = :folderId")
    void changeFolderName(long folderId, String newName);

    @Query("UPDATE folders SET parentId = :toFolderId WHERE id = :id")
    void updateParentId(long id, Long toFolderId);

    @SuppressWarnings("AndroidUnresolvedRoomSqlReference")//room can't on recursive queries now
    @Query("WITH RECURSIVE path(level, name, parentId) AS (" +
            "    SELECT 0, name, parentId" +
            "    FROM folders" +
            "    WHERE id = :folderId" +
            "    UNION ALL" +
            "    SELECT path.level + 1," +
            "           folders.name," +
            "           folders.parentId" +
            "    FROM folders" +
            "    JOIN path ON folders.id = path.parentId" +
            ")," +
            "path_from_root AS (" +
            "    SELECT name" +
            "    FROM path" +
            "    ORDER BY level DESC" +
            ")" +
            "SELECT group_concat(name, '/')" +
            "FROM path_from_root")
    String getFullFolderPath(long folderId);

    static String getRecursiveFolderQuery(Long parentFolderId) {
        return "WITH RECURSIVE allChildFolders(childFolderId, rootFolderId) AS (" +
                "SELECT id as childFolderId, id as rootFolderId FROM folders WHERE parentId = " + parentFolderId + " OR (parentId IS NULL AND " + parentFolderId + " IS NULL)" +
                "UNION " +
                "SELECT id as childFolderId, allChildFolders.rootFolderId as rootFolderId FROM folders INNER JOIN allChildFolders ON parentId = allChildFolders.childFolderId" +
                ")";
    }

}
