package com.github.anrimian.musicplayer.data.database.dao.playlist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlaylistEntryEntity
import com.github.anrimian.musicplayer.data.repositories.playlists.models.PlaylistEntryPosition
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlaylist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import io.reactivex.rxjava3.core.Observable

@Dao
interface PlaylistDao {

    @Query("""
        INSERT OR ABORT INTO play_lists (storageId, name, addedTime, modifiedTime) 
        VALUES (:storageId, :name, :addedTime, :modifiedTime)
    """)
    fun insertPlaylist(storageId: Long?, name: String, addedTime: Long, modifiedTime: Long): Long

    @Query("DELETE FROM play_lists WHERE id = :id")
    fun deletePlaylist(id: Long)

    @Query("UPDATE play_lists SET name = :name WHERE id = :id")
    fun updatePlaylistName(id: Long, name: String)

    @Query("UPDATE play_lists SET modifiedTime = :modifyTime WHERE id = :id")
    fun updatePlayListModifyTime(id: Long, modifyTime: Long)

    @Query("""
        WITH entries(playlistId, duration) AS ( 
            SELECT 
                playlistId AS playlistId, 
                (SELECT duration FROM compositions WHERE compositions.id = play_lists_entries.audioId) AS duration 
            FROM play_lists_entries
        )
        SELECT 
            play_lists.id AS id, 
            play_lists.name AS name, 
            play_lists.addedTime AS addedTime, 
            play_lists.modifiedTime AS modifiedTime, 
            (SELECT count() FROM entries WHERE playListId = play_lists.id) AS compositionsCount, 
            (SELECT sum(duration) FROM entries WHERE playlistId = play_lists.id) AS totalDuration 
        FROM play_lists 
        WHERE (:searchQuery IS NULL OR name LIKE :searchQuery)
        ORDER BY modifiedTime DESC
    """)
    fun getPlayListsObservable(searchQuery: String?): Observable<List<Playlist>>

    @Query("""
        SELECT 
            play_lists.id AS id, 
            play_lists.storageId AS storageId, 
            play_lists.name AS name, 
            play_lists.addedTime AS addedTime, 
            play_lists.modifiedTime AS modifiedTime, 
            (SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) AS compositionsCount 
        FROM play_lists
    """)
    fun getAllPlayLists(): List<AppPlaylist>

    @Query("""
        SELECT 
            play_lists.id AS id, 
            play_lists.storageId AS storageId, 
            play_lists.name AS name, 
            play_lists.addedTime AS addedTime, 
            play_lists.modifiedTime AS modifiedTime, 
            (SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) AS compositionsCount 
        FROM play_lists 
        WHERE id = :playlistId
    """)
    fun getPlayList(playlistId: Long): AppPlaylist?

    @Query("""
        SELECT 
            play_lists.id AS id, 
            play_lists.storageId AS storageId, 
            play_lists.name AS name, 
            play_lists.addedTime AS addedTime, 
            play_lists.modifiedTime AS modifiedTime, 
            (SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) AS compositionsCount 
        FROM play_lists
    """)
    fun getAllAsStoragePlayLists(): List<AppPlaylist>

    @Query("""
        WITH entries(playlistId, duration) AS ( 
            SELECT 
                playlistId AS playlistId, 
                (SELECT duration FROM compositions WHERE compositions.id = play_lists_entries.audioId) AS duration 
            FROM play_lists_entries
        )
        SELECT 
            play_lists.id AS id, 
            play_lists.name AS name, 
            play_lists.addedTime AS addedTime, 
            play_lists.modifiedTime AS modifiedTime, 
            (SELECT count() FROM entries WHERE playListId = play_lists.id) AS compositionsCount, 
            (SELECT sum(duration) FROM entries WHERE playlistId = play_lists.id) AS totalDuration 
        FROM play_lists 
        WHERE play_lists.id = :id 
        LIMIT 1
    """)
    fun getPlayListObservable(id: Long): Observable<List<Playlist>>

    @RawQuery(observedEntities = [ PlaylistEntryEntity::class, ArtistEntity::class, CompositionEntity::class, AlbumEntity::class ])
    fun getPlayListItemsObservable(query: SimpleSQLiteQuery): Observable<List<PlaylistEntry>>

    @Query("SELECT playlistId FROM play_lists_entries WHERE audioId = :compositionId")
    fun getPlaylistsForComposition(compositionId: Long): List<Long>

    @Query("SELECT audioId FROM play_lists_entries WHERE playListId = :playlistId ORDER BY orderPosition")
    fun getCompositionIdsInPlaylist(playlistId: Long): List<Long>

    @RawQuery
    fun getCompositionsInPlaylist(query: SimpleSQLiteQuery): List<Composition>

    @Query("""
        SELECT
        (
            WITH RECURSIVE path(level, name, parentId, id) AS (
                SELECT 0, name, parentId, id FROM folders WHERE id = compositions.folderId
                UNION ALL
                SELECT path.level + 1, folders.name, folders.parentId, folders.id FROM folders JOIN path ON folders.id = path.parentId
            ),
            path_from_root AS (
                SELECT name FROM path WHERE parentId IS NOT NULL ORDER BY level DESC
            ),
            volume_path AS (
                SELECT v.path FROM volumes AS v
                JOIN folders AS f ON v.id = f.volumeId
                JOIN path ON f.id = path.id AND path.parentId IS NULL
                LIMIT 1
            )
            SELECT IFNULL((SELECT path FROM volume_path) || (SELECT IFNULL(group_concat('/' || name, ''), '') FROM path_from_root), '')
        ) || '/' || fileName AS filePath
        FROM play_lists_entries
        JOIN compositions ON play_lists_entries.audioId = compositions.id
        WHERE play_lists_entries.playListId = :playListId
        ORDER BY orderPosition
    """)
    fun getPlayListItemsAsFileEntries(playListId: Long): List<PlayListEntry>

    @Query("DELETE FROM play_lists_entries WHERE itemId = :id")
    fun deletePlayListEntry(id: Long)

    @Query("DELETE FROM play_lists_entries WHERE playListId = :playlistId")
    fun clearPlayListEntries(playlistId: Long)

    @Insert
    fun insertPlayListEntries(entities: List<PlaylistEntryEntity>)

    @Query("""
        INSERT OR IGNORE INTO play_lists_entries (storageItemId, audioId, playListId, orderPosition) 
        VALUES (:storageItemId, :compositionId, :playListId, :orderPosition)
    """)
    fun insertPlayListEntry(storageItemId: Long?, compositionId: Long, playListId: Long, orderPosition: Int)

    @Query("SELECT MAX(orderPosition) FROM play_lists_entries WHERE playListId = :playListId")
    fun selectMaxOrder(playListId: Long): Int?

    @Query("SELECT COALESCE(MAX(orderPosition), -1) + 1 FROM play_lists_entries WHERE playListId = :playListId")
    fun selectNextOrderPosition(playListId: Long): Int

    @Query("SELECT orderPosition FROM play_lists_entries WHERE itemId = :id")
    fun selectPositionById(id: Long): Int

    @Query("""
        UPDATE play_lists_entries SET orderPosition = 
          CASE 
            WHEN orderPosition < :fromPos THEN orderPosition + 1
            WHEN orderPosition > :fromPos THEN orderPosition - 1
            ELSE :toPos
          END 
        WHERE (orderPosition BETWEEN min(:fromPos, :toPos) AND max(:fromPos,:toPos)) 
        AND playListId = :playListId
    """)
    fun moveItems(playListId: Long, fromPos: Int, toPos: Int)

    @Query("""
        UPDATE play_lists_entries 
        SET orderPosition = orderPosition + :increaseBy 
        WHERE orderPosition >= :position AND playListId = :playListId
    """)
    fun increasePositionsByCountAfter(increaseBy: Int, position: Int, playListId: Long)

    @Query("""
        UPDATE play_lists_entries 
        SET orderPosition = orderPosition - 1 
        WHERE orderPosition > :position AND playListId = :playListId
    """)
    fun decreasePositionsAfter(position: Int, playListId: Long)

    @Query("SELECT storageId FROM play_lists WHERE id = :id")
    fun selectStorageId(id: Long): Long?

    @Query("SELECT storageItemId FROM play_lists_entries WHERE itemId = :itemId")
    fun selectStorageItemId(itemId: Long): Long?

    @Query("SELECT EXISTS(SELECT 1 FROM play_lists WHERE name = :name LIMIT 1)")
    fun isPlayListWithNameExists(name: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM play_lists WHERE storageId = :storageId LIMIT 1)")
    fun isPlayListExistsByStorageId(storageId: Long): Boolean

    @Query("SELECT name FROM play_lists WHERE id = :playListId")
    fun selectPlayListName(playListId: Long): String?

    @Query("SELECT id FROM play_lists WHERE name = :name")
    fun findPlaylist(name: String): Long

    @Query("SELECT EXISTS(SELECT 1 FROM play_lists WHERE id = :playlistId)")
    fun isPlaylistExists(playlistId: Long): Boolean

    @Query("SELECT count() FROM play_lists_entries WHERE playListId = :playListId")
    fun getPlaylistSize(playListId: Long): Int

    @Query("SELECT itemId, orderPosition FROM play_lists_entries WHERE playListId = :playlistId ORDER BY orderPosition")
    fun getEntryPositions(playlistId: Long): List<PlaylistEntryPosition>

    @Query("UPDATE play_lists_entries SET orderPosition = :position WHERE itemId = :itemId")
    fun updateEntryPosition(itemId: Long, position: Int)

    @RawQuery
    fun getEntryIdsSorted(query: SimpleSQLiteQuery): List<Long>

    companion object Companion {

        fun getPlaylistEntriesQuery(useFileName: Boolean): String {
            return """
                    SELECT 
                        play_lists_entries.itemId AS entryId,
                        ${CompositionsDao.getCompositionSelectionQuery(useFileName)}
                    FROM play_lists_entries 
                    INNER JOIN compositions ON play_lists_entries.audioId = compositions.id 
                    WHERE play_lists_entries.playListId = ? AND 
                        ${CompositionsDao.getSearchQuery(useFileName)}
                    ORDER BY orderPosition
                    """
        }

        fun getCompositionsQuery(useFileName: Boolean): String {
            return """
                    SELECT 
                        ${CompositionsDao.getCompositionSelectionQuery(useFileName)}
                    FROM play_lists_entries 
                    INNER JOIN compositions ON play_lists_entries.audioId = compositions.id 
                    WHERE play_lists_entries.playListId = ? 
                    ORDER BY orderPosition
                    """
        }
    }

}
