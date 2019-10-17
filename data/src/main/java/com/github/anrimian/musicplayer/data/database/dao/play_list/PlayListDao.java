package com.github.anrimian.musicplayer.data.database.dao.play_list;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryDto;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListPojo;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

@Dao
public interface PlayListDao {

    @Insert
    long insertPlayListEntity(PlayListEntity entity);

    @Insert
    void insertPlayListEntities(List<PlayListEntity> entities);

    @Query("DELETE FROM play_lists WHERE id = :id")
    void deletePlayList(long id);

    @Query("UPDATE play_lists SET name = :name WHERE id = :id")
    void updatePlayListName(long id, String name);

    @Query("UPDATE play_lists SET dateModified = :modifyTime WHERE id = :id")
    void updatePlayListModifyTime(long id, Date modifyTime);

    @Query("UPDATE play_lists SET name = :name WHERE storageId = :storageId")
    void updatePlayListNameByStorageId(long storageId, String name);

    @Query("UPDATE play_lists SET dateModified = :modifyTime WHERE storageId = :storageId")
    void updatePlayListModifyTimeByStorageId(long storageId, Date modifyTime);

    //can we cache duplicates?
    @Query("SELECT " +
            "play_lists.id as id, " +
            "play_lists.storageId as storageId, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) as compositionsCount, " +
            "(SELECT sum(duration) FROM compositions WHERE compositions.id IN (SELECT audioId FROM play_lists_entries WHERE playListId = play_lists.id)) as totalDuration " +
            "FROM play_lists " +
            "ORDER BY dateModified DESC")
    Observable<List<PlayListPojo>> getPlayListsObservable();

    @Query("SELECT " +
            "play_lists.storageId as id, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified " +
            "FROM play_lists")
    List<StoragePlayList> getAllAsStoragePlayLists();

    @Query("SELECT " +
            "play_lists.id as dbId, " +
            "play_lists.storageId as storageId " +
            "FROM play_lists")
    List<IdPair> getPlayListsIds();

    @Query("SELECT " +
            "play_lists.id as id, " +
            "play_lists.storageId as storageId, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) as compositionsCount, " +
            "(SELECT sum(duration) FROM compositions WHERE compositions.id IN (SELECT audioId FROM play_lists_entries WHERE playListId = play_lists.id)) as totalDuration " +
            "FROM play_lists " +
            "WHERE play_lists.id = :id ")
    Observable<List<PlayListPojo>> getPlayListObservable(long id);

    @Query("SELECT " +
            "play_lists_entries.itemId AS itemId," +
            "play_lists_entries.storageItemId as storageItemId, " +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "compositions.artist AS artist, " +
            "compositions.title AS title, " +
            "compositions.album AS album, " +
            "compositions.filePath AS filePath, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_lists_entries " +
            "INNER JOIN compositions ON play_lists_entries.audioId = compositions.id " +
            "WHERE play_lists_entries.playListId = :playListId " +
            "ORDER BY orderPosition")
    Observable<List<PlayListEntryDto>> getPlayListItemsObservable(long playListId);

    @Query("SELECT " +
            "play_lists_entries.storageItemId as itemId, " +
            "play_lists_entries.audioId as compositionId " +//wrong id
            "FROM play_lists_entries " +
            "WHERE play_lists_entries.playListId = :playListId " +
            "ORDER BY orderPosition")
    List<StoragePlayListItem> getPlayListItemsAsStorageItems(long playListId);

    @Query("DELETE FROM play_lists_entries WHERE itemId = :id")
    void deletePlayListEntry(long id);

    @Insert
    void insertPlayListEntry(PlayListEntryEntity entity);

    @Insert
    void insertPlayListEntries(List<PlayListEntryEntity> entities);

    @Query("SELECT MAX(orderPosition) FROM play_lists_entries WHERE playListId = :playListId")
    int selectMaxOrder(long playListId);

    @Query("UPDATE play_lists_entries SET orderPosition = :position WHERE itemId = :itemId")
    void updateItemPosition(long itemId, int position);

    @Nullable
    @Query("SELECT storageId FROM play_lists WHERE id = :id")
    Long selectStorageId(long id);
}
