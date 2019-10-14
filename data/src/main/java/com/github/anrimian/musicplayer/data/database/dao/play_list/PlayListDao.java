package com.github.anrimian.musicplayer.data.database.dao.play_list;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryDto;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListPojo;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

@Dao
public interface PlayListDao {

    @Insert
    void insertPlayListEntity(PlayListEntity entity);

    @Query("DELETE FROM play_lists WHERE id = :id")
    void deletePlayList(long id);

    @Query("UPDATE play_lists SET name = :name WHERE id = :id")
    void updatePlayListName(long id, String name);

    @Query("UPDATE play_lists SET dateModified = :modifyTime WHERE id = :id")
    void updatePlayListModifyTime(long id, Date modifyTime);

    //can we cache duplicates?
    @Query("SELECT " +
            "play_lists.id as id, " +
            "play_lists.storageId as storageId, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) as compositionsCount, " +
            "sum(compositions.duration) as totalDuration " +
            "FROM play_lists INNER JOIN compositions WHERE compositions.id IN (SELECT storageId FROM play_lists_entries WHERE playListId = play_lists.id) " +
            "ORDER BY dateModified")
    Observable<List<PlayListPojo>> getPlayListsObservable();

    @Query("SELECT " +
            "play_lists.id as id, " +
            "play_lists.storageId as storageId, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) as compositionsCount, " +
            "sum(compositions.duration) as totalDuration " +
            "FROM play_lists " +
            "INNER JOIN compositions ON compositions.id IN (SELECT storageId FROM play_lists_entries WHERE playListId = play_lists.id) " +
            "WHERE play_lists.id = :id " +
            "ORDER BY dateModified")
    Observable<List<PlayListPojo>> getPlayListObservable(long id);

    @Query("SELECT " +
            "play_lists_entries.itemId AS itemId," +
            "play_lists_entries.storageItemId as storageItemId, " +
            "compositions.id AS id, " +
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
    Observable<List<PlayListEntryDto>> getPlayQueueObservable(long playListId);

    @Query("DELETE FROM play_lists_entries WHERE itemId = :id")
    void deletePlayListEntry(long id);

    @Insert
    void addPlayListEntry(PlayListEntryEntity entity);

    @Query("SELECT MAX(orderPosition) FROM play_lists_entries WHERE playListId = :playListId")
    int selectMaxOrder(long playListId);

    @Query("UPDATE play_lists_entries SET orderPosition = :position WHERE itemId = :itemId")
    void updateItemPosition(long itemId, int position);
}
