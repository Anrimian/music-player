package com.github.anrimian.musicplayer.data.database.dao.play_list;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlayList;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;

@Dao
public interface PlayListDao {

    @Query("INSERT OR ABORT INTO play_lists (storageId, name, dateAdded, dateModified) " +
            "VALUES (:storageId, :name, :dateAdded, :dateModified)")
    long insertPlayList(Long storageId, String name, Date dateAdded, Date dateModified);

    @Query("DELETE FROM play_lists WHERE id = :id")
    void deletePlayList(long id);

    @Query("UPDATE play_lists SET name = :name WHERE id = :id")
    void updatePlayListName(long id, String name);

    @Query("UPDATE play_lists SET dateModified = :modifyTime WHERE id = :id")
    void updatePlayListModifyTime(long id, Date modifyTime);

    @Query("WITH entries(playlistId, duration) AS ( " +
            "   SELECT " +
            "   playlistId AS playlistId, " +
            "   (SELECT duration FROM compositions WHERE compositions.id = play_lists_entries.audioId) AS duration " +
            "   FROM play_lists_entries" +
            ")" +
            "SELECT " +
            "play_lists.id as id, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM entries WHERE playListId = play_lists.id) as compositionsCount, " +
            "(SELECT sum(duration) FROM entries WHERE playlistId = play_lists.id) as totalDuration " +
            "FROM play_lists " +
            "WHERE (:searchQuery IS NULL OR name LIKE :searchQuery)" +
            "ORDER BY dateModified DESC")
    Observable<List<PlayList>> getPlayListsObservable(String searchQuery);

    @Query("SELECT " +
            "play_lists.id as id, " +
            "play_lists.storageId as storageId, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) as compositionsCount " +
            "FROM play_lists")
    List<AppPlayList> getAllPlayLists();

    @Query("SELECT " +
            "play_lists.id as id, " +
            "play_lists.storageId as storageId, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) as compositionsCount " +
            "FROM play_lists " +
            "WHERE id = :playlistId")
    AppPlayList getPlayList(long playlistId);

    @Query("SELECT " +
            "play_lists.id as id, " +
            "play_lists.storageId as storageId, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) as compositionsCount " +
            "FROM play_lists")
    List<AppPlayList> getAllAsStoragePlayLists();

    @Query("WITH entries(playlistId, duration) AS ( " +
            "   SELECT " +
            "   playlistId AS playlistId, " +
            "   (SELECT duration FROM compositions WHERE compositions.id = play_lists_entries.audioId) AS duration " +
            "   FROM play_lists_entries" +
            ")" +
            "SELECT " +
            "play_lists.id as id, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM entries WHERE playListId = play_lists.id) as compositionsCount, " +
            "(SELECT sum(duration) FROM entries WHERE playlistId = play_lists.id) as totalDuration " +
            "FROM play_lists " +
            "WHERE play_lists.id = :id " +
            "LIMIT 1")
    Observable<List<PlayList>> getPlayListObservable(long id);

    @RawQuery(observedEntities = { PlayListEntryEntity.class, ArtistEntity.class, CompositionEntity.class, AlbumEntity.class })
    Observable<List<PlayListItem>> getPlayListItemsObservable(SimpleSQLiteQuery query);

    @Query("SELECT playlistId FROM play_lists_entries WHERE audioId = :compositionId")
    List<Long> getPlaylistsForComposition(long compositionId);

    @Query("SELECT audioId FROM play_lists_entries WHERE playListId = :playlistId ORDER BY orderPosition")
    List<Long> getCompositionIdsInPlaylist(long playlistId);

    @RawQuery
    List<Composition> getCompositionsInPlaylist(SimpleSQLiteQuery query);

    @Query("SELECT " +
            "(" +
            "WITH RECURSIVE path(level, name, parentId) AS (" +
            "                SELECT 0, name, parentId " +
            "                FROM folders " +
            "                WHERE id = compositions.folderId " +
            "                UNION ALL " +
            "                SELECT path.level + 1, " +
            "                       folders.name, " +
            "                       folders.parentId " +
            "                FROM folders " +
            "                JOIN path ON folders.id = path.parentId " +
            "            ), " +
            "            path_from_root AS ( " +
            "                SELECT name " +
            "                FROM path " +
            "                ORDER BY level DESC " +
            "            ) " +
            "            SELECT ifnull(group_concat(name, '/') || '/' , '') " +
            "            FROM path_from_root" +
            ") || fileName AS filePath " +
            "FROM play_lists_entries " +
            "JOIN compositions ON play_lists_entries.audioId = compositions.id " +
            "WHERE play_lists_entries.playListId = :playListId " +
            "ORDER BY orderPosition")
    List<PlayListEntry> getPlayListItemsAsFileEntries(long playListId);

    @Query("DELETE FROM play_lists_entries WHERE itemId = :id")
    void deletePlayListEntry(long id);

    @Query("DELETE FROM play_lists_entries WHERE playListId = :playlistId")
    void clearPlayListEntries(long playlistId);

    @Insert
    void insertPlayListEntries(List<PlayListEntryEntity> entities);

    @Query("INSERT OR IGNORE INTO play_lists_entries (storageItemId, audioId, playListId, orderPosition) " +
            "VALUES (:storageItemId, :compositionId, :playListId, :orderPosition)")
    void insertPlayListEntry(Long storageItemId, long compositionId, long playListId, int orderPosition);

    @Query("SELECT MAX(orderPosition) FROM play_lists_entries WHERE playListId = :playListId")
    int selectMaxOrder(long playListId);

    @Query("SELECT orderPosition FROM play_lists_entries WHERE itemId = :id")
    int selectPositionById(long id);

    @Query("UPDATE play_lists_entries SET orderPosition = " +
            "  CASE " +
            "    WHEN orderPosition < :fromPos THEN orderPosition + 1" +
            "    WHEN orderPosition > :fromPos THEN orderPosition - 1" +
            "    ELSE :toPos" +
            "  END " +
            "WHERE (orderPosition BETWEEN min(:fromPos, :toPos) AND max(:fromPos,:toPos)) " +
            "AND playListId = :playListId")
    void moveItems(long playListId, int fromPos, int toPos);

    @Query("UPDATE play_lists_entries " +
            "SET orderPosition = orderPosition + :increaseBy " +
            "WHERE orderPosition >= :position AND playListId = :playListId")
    void increasePositionsByCountAfter(int increaseBy, int position, long playListId);

    @Query("UPDATE play_lists_entries " +
            "SET orderPosition = orderPosition - 1 " +
            "WHERE orderPosition > :position AND playListId = :playListId")
    void decreasePositionsAfter(int position, long playListId);

    @Nullable
    @Query("SELECT storageId FROM play_lists WHERE id = :id")
    Long selectStorageId(long id);

    @Nullable
    @Query("SELECT storageItemId FROM play_lists_entries WHERE itemId = :itemId")
    Long selectStorageItemId(long itemId);

    @Query("SELECT exists(SELECT 1 FROM play_lists WHERE name = :name LIMIT 1)")
    boolean isPlayListWithNameExists(String name);

    @Query("SELECT exists(SELECT 1 FROM play_lists WHERE storageId = :storageId LIMIT 1)")
    boolean isPlayListExistsByStorageId(long storageId);

    @Query("SELECT name FROM play_lists WHERE id = :playListId")
    String selectPlayListName(long playListId);

    @Query("SELECT id FROM play_lists WHERE name = :name")
    long findPlaylist(String name);

    @Query("SELECT exists(SELECT 1 FROM play_lists WHERE id = :playlistId)")
    boolean isPlaylistExists(long playlistId);

    @Query("SELECT count() FROM play_lists_entries WHERE playListId = :playListId")
    int getPlaylistSize(long playListId);

    static String getPlaylistItemsQuery(boolean useFileName) {
        return "SELECT " +
                "play_lists_entries.itemId AS itemId," +
                CompositionsDao.getCompositionSelectionQuery(useFileName) +
                "FROM play_lists_entries " +
                "INNER JOIN compositions ON play_lists_entries.audioId = compositions.id " +
                "WHERE play_lists_entries.playListId = ? AND " +
                CompositionsDao.getSearchQuery(useFileName) +
                "ORDER BY orderPosition";
    }

    static String getCompositionsQuery(boolean useFileName) {
        return "SELECT " +
                CompositionsDao.getCompositionSelectionQuery(useFileName) +
                "FROM play_lists_entries " +
                "INNER JOIN compositions ON play_lists_entries.audioId = compositions.id " +
                "WHERE play_lists_entries.playListId = ? " +
                "ORDER BY orderPosition";
    }

}
