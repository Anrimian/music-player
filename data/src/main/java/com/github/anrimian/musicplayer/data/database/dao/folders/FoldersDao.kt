package com.github.anrimian.musicplayer.data.database.dao.folders

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderInfo
import com.github.anrimian.musicplayer.domain.models.folders.Volume
import io.reactivex.rxjava3.core.Observable

@Dao
interface FoldersDao {

    @RawQuery(observedEntities = [ CompositionEntity::class, FolderEntity::class ])
    fun getFoldersObservable(query: SupportSQLiteQuery): Observable<List<FolderFileSource>>

    @RawQuery
    fun getFoldersIds(query: SupportSQLiteQuery): List<Long>


    @Query("""
        WITH RECURSIVE path(level, name, parentId, id) AS (
            SELECT 0, name, parentId, id
            FROM folders
            WHERE id = :folderId
            UNION ALL
            SELECT path.level + 1,
                   folders.name,
                   folders.parentId,
                   folders.id
            FROM folders
            JOIN path ON folders.id = path.parentId
        ),
        path_from_root AS (
            SELECT name
            FROM path
            WHERE parentId IS NOT NULL
            ORDER BY level DESC
        ),
        volume_path AS (
            SELECT v.path
            FROM volumes AS v
            JOIN folders AS f ON v.id = f.volumeId
            JOIN path ON f.id = path.id AND path.parentId IS NULL
            LIMIT 1
        )
        SELECT
            IFNULL((SELECT path FROM volume_path) || (SELECT IFNULL(group_concat('/' || name, ''), '') FROM path_from_root), '') AS path,
            id AS id,
            (SELECT count() FROM path_from_root) AS level,
            (SELECT id FROM path WHERE parentId IS NULL) AS volumeFolderId
        FROM folders
        WHERE id = :folderId OR (id IS NULL AND :folderId IS NULL)
        LIMIT 1
    """)
    fun getFolderObservable(folderId: Long?): Observable<List<FolderInfo>>

    @Query("""
        WITH RECURSIVE all_folders_in_volume(volumeRootId, folderId) AS (
            SELECT f.id, f.id
            FROM folders AS f
            WHERE f.volumeId IS NOT NULL
            UNION ALL
            SELECT afv.volumeRootId, f_child.id
            FROM folders AS f_child
            JOIN all_folders_in_volume AS afv ON f_child.parentId = afv.folderId
        )
        SELECT
            v.id AS id,
            f.id AS rootFolderId,
            v.storageKey AS storageKey,
            v.path AS path,
            (SELECT COUNT(c.id)
             FROM compositions AS c
             WHERE c.folderId IN (SELECT afv.folderId FROM all_folders_in_volume AS afv WHERE afv.volumeRootId = f.id)
            ) AS compositionsCount
        FROM volumes AS v
        JOIN folders AS f ON f.volumeId = v.id AND f.parentId IS NULL
        ORDER BY v.isPrimary DESC, compositionsCount DESC
    """)
    fun getVolumes(): Observable<List<Volume>>

    @Query("""
        SELECT 
            v.id AS id,
            f.id AS rootFolderId,
            v.storageKey AS storageKey,
            v.path AS path,
            0 AS compositionsCount --not used
        FROM volumes AS v
        JOIN folders AS f ON f.volumeId = v.id AND f.parentId IS NULL
        WHERE :path LIKE v.path || '%'
        ORDER BY length(v.path) DESC
        LIMIT 1
    """)
    fun findVolumeByPath(path: String): Volume?

    @Query("SELECT id FROM volumes WHERE path = :path LIMIT 1")
    fun getVolumeByPath(path: String): Long?

    @Query("INSERT INTO volumes (storageKey, path, isPrimary) VALUES (:storageKey, :path, :isPrimary)")
    fun insertVolume(storageKey: String, path: String, isPrimary: Boolean): Long

    @Query("DELETE FROM volumes WHERE id NOT IN (SELECT volumeId FROM folders WHERE volumeId IS NOT NULL)")
    fun deleteOrphanedVolumes(): Int

    @Query("INSERT INTO folders (parentId, volumeId, name) VALUES (:parentId, :volumeId, :name)")
    fun insertFolder(name: String, parentId: Long?, volumeId: Long?): Long

    @Query("DELETE FROM folders WHERE id IN(:ids)")
    fun deleteFolders(ids: List<Long>)

    @Query("DELETE FROM folders WHERE id = :id")
    fun deleteFolder(id: Long)

    @Query("UPDATE folders SET name = :newName WHERE id = :folderId")
    fun changeFolderName(folderId: Long, newName: String)

    @Query("UPDATE folders SET parentId = :toFolderId WHERE id = :id")
    fun updateParentId(id: Long, toFolderId: Long?)

    @Query("UPDATE folders SET parentId = :toFolderId WHERE parentId = :fromParentId")
    fun replaceParentId(fromParentId: Long, toFolderId: Long?)

    @Query("""
        WITH RECURSIVE path(level, name, parentId, id) AS (
            SELECT 0, name, parentId, id
            FROM folders
            WHERE id = :folderId
            UNION ALL
            SELECT path.level + 1,
                   folders.name,
                   folders.parentId,
                   folders.id
            FROM folders
            JOIN path ON folders.id = path.parentId
        ),
        path_from_root AS (
            SELECT name
            FROM path
            WHERE parentId IS NOT NULL
            ORDER BY level DESC
        ),
        volume_path AS (
            SELECT v.path
            FROM volumes AS v
            JOIN folders AS f ON v.id = f.volumeId
            JOIN path ON f.id = path.id AND path.parentId IS NULL
            ORDER BY path.level DESC
            LIMIT 1
        )
        SELECT IFNULL((SELECT path FROM volume_path) || (SELECT IFNULL(group_concat('/' || name, ''), '') FROM path_from_root), '')
    """)
    fun getFullFolderPath(folderId: Long): String

    @Query("""
        WITH RECURSIVE path(level, id, parentId) AS (
            SELECT 0, id, parentId
            FROM folders
            WHERE id = :folderId OR (id IS NULL AND :folderId IS NULL)
            UNION ALL
            SELECT path.level + 1,
                   folders.id,
                   folders.parentId
            FROM folders
            JOIN path ON folders.id = path.parentId
        )
        SELECT id FROM path ORDER BY level DESC
    """)
    fun getAllParentFoldersId(folderId: Long?): List<Long>

    @Query("SELECT parentId FROM folders WHERE id = :folderId")
    fun getFolderParentId(folderId: Long): Long?

    @Query("""
        SELECT id
        FROM folders
        WHERE (parentId = :parentId OR (parentId IS NULL AND :parentId IS NULL AND volumeId = :volumeId))
        AND name = :name
        LIMIT 1
    """)
    fun getFolderByName(name: String, parentId: Long?, volumeId: Long? = null): Long?

    @Query("""
        WITH parentIds AS (SELECT parentId FROM folders)
        DELETE FROM folders
        WHERE (SELECT count() FROM parentIds WHERE parentIds.parentId = folders.id) = 0
        AND (SELECT count() FROM compositions WHERE folderId = folders.id) = 0
    """)
    fun deleteFoldersWithoutContainment(): Int

    companion object {

        fun getRecursiveFolderQuery(parentFolderId: Long?): String {
            return getRecursiveFolderQuery(parentFolderId, false)
        }

        fun getRecursiveFolderQuery(parentFolderId: Long?, selectAll: Boolean): String {
            return """
                WITH RECURSIVE allChildFolders(childFolderId, rootFolderId) AS (
                    SELECT id as childFolderId, id as rootFolderId FROM folders
                    ${ if (!selectAll) "WHERE parentId = $parentFolderId OR (parentId IS NULL AND $parentFolderId IS NULL)" else "" }
                    UNION
                    SELECT id as childFolderId, allChildFolders.rootFolderId as rootFolderId FROM folders INNER JOIN allChildFolders ON parentId = allChildFolders.childFolderId
                )
            """
        }
    }
}
