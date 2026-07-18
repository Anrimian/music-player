package com.github.anrimian.musicplayer.data.database.dao.folders;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderInfo;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

@Dao
public interface FoldersDao {

    @RawQuery(observedEntities = { CompositionEntity.class, FolderEntity.class })
    Observable<List<FolderFileSource>> getFoldersObservable(SupportSQLiteQuery query);

    @RawQuery
    List<Long> getFoldersIds(SupportSQLiteQuery query);

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
            "SELECT " +
            "(SELECT IFNULL(group_concat(name, '/'), '') FROM path_from_root) AS path," +
            "id AS id," +
            "(SELECT count() < 2 FROM path_from_root) AS isParentOfParentRoot " +
            "FROM folders " +
            "WHERE id = :folderId OR (id IS NULL AND :folderId IS NULL) " +
            "LIMIT 1")
    Observable<List<FolderInfo>> getFolderObservable(Long folderId);

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

    @Query("UPDATE folders SET parentId = :toFolderId WHERE parentId = :fromParentId")
    void replaceParentId(long fromParentId, Long toFolderId);

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
            "SELECT IFNULL(group_concat(name, '/'), '')" +
            "FROM path_from_root")
    String getFullFolderPath(long folderId);

    @Query("WITH RECURSIVE path(level, id, parentId) AS (" +
            "    SELECT 0, id, parentId" +
            "    FROM folders" +
            "    WHERE id = :folderId OR (id IS NULL AND :folderId IS NULL)" +
            "    UNION ALL" +
            "    SELECT path.level + 1," +
            "           folders.id," +
            "           folders.parentId" +
            "    FROM folders" +
            "    JOIN path ON folders.id = path.parentId" +
            ")" +
            "SELECT id FROM path ORDER BY level DESC")
    List<Long> getAllParentFoldersId(Long folderId);

    @Query("SELECT name " +
            "FROM folders " +
            "WHERE parentId = :parentId OR (parentId IS NULL AND :parentId IS NULL)")
    List<String> getFolderNamesInFolder(Long parentId);

    @Nullable
    @Query("SELECT parentId FROM folders WHERE id = :folderId")
    Long getFolderParentId(long folderId);

    @Query("SELECT exists(" +
            "SELECT 1 " +
            "FROM folders " +
            "WHERE (parentId = :parentId OR (parentId IS NULL AND :parentId IS NULL)) " +
            "AND name = :name " +
            "LIMIT 1)")
    boolean isFolderWithNameExists(Long parentId, String name);

    @Nullable
    @Query("SELECT id " +
            "FROM folders " +
            "WHERE (parentId = :parentId OR (parentId IS NULL AND :parentId IS NULL)) " +
            "AND name = :name " +
            "LIMIT 1")
    Long getFolderByName(Long parentId, String name);

    @Query("WITH parentIds AS (SELECT parentId FROM folders)" +
            "DELETE FROM folders " +
            "WHERE (SELECT count() FROM parentIds WHERE parentIds.parentId = folders.id) = 0 " +
            "AND (SELECT count() FROM compositions WHERE folderId = folders.id) = 0")
    int deleteFoldersWithoutContainment();

    static String getRecursiveFolderQuery(Long parentFolderId) {
        return getRecursiveFolderQuery(parentFolderId, false);
    }
    static String getRecursiveFolderQuery(Long parentFolderId, boolean selectAll) {
        return "WITH RECURSIVE allChildFolders(childFolderId, rootFolderId) AS (" +
                "SELECT id as childFolderId, id as rootFolderId FROM folders " +
                    (!selectAll? "WHERE parentId = " + parentFolderId + " OR (parentId IS NULL AND " + parentFolderId + " IS NULL)": "") +
                "UNION " +
                "SELECT id as childFolderId, allChildFolders.rootFolderId as rootFolderId FROM folders INNER JOIN allChildFolders ON parentId = allChildFolders.childFolderId" +
                ")";
    }

}
