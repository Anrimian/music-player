package com.github.anrimian.musicplayer.data.database.dao.play_list;

import android.util.Log;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryDto;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListPojo;
import com.github.anrimian.musicplayer.data.database.entities.playlist.RawPlayListItem;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

public class PlayListsDaoWrapper {

    private final PlayListDao playListDao;
    private final AppDatabase appDatabase;

    public PlayListsDaoWrapper(PlayListDao playListDao, AppDatabase appDatabase) {
        this.playListDao = playListDao;
        this.appDatabase = appDatabase;
    }

    public void applyChanges(List<StoragePlayList> addedPlayLists,
                             List<StoragePlayList> changedPlayLists) {
        appDatabase.runInTransaction(() -> {
            playListDao.insertPlayListEntities(mapList(addedPlayLists, this::toEntity));
            for (StoragePlayList playList: changedPlayLists) {
                playListDao.updatePlayListModifyTimeByStorageId(playList.getId(), playList.getDateModified());
                playListDao.updatePlayListNameByStorageId(playList.getId(), playList.getName());
            }
        });
    }

    public PlayList insertPlayList(StoragePlayList playList) {
        PlayListEntity entity = new PlayListEntity(
                playList.getId(),
                playList.getName(),
                playList.getDateAdded(),
                playList.getDateModified());
        long id = playListDao.insertPlayListEntity(entity);
        if (id == -1) {
            throw new IllegalStateException("db not modified");
        }
        return new PlayList(id,
                playList.getId(),
                playList.getName(),
                playList.getDateAdded(),
                playList.getDateModified(),
                0,
                0);
    }

    public List<StoragePlayList> getAllAsStoragePlayLists() {
        return playListDao.getAllAsStoragePlayLists();
    }

    public void deletePlayList(long id) {
        playListDao.deletePlayList(id);
    }

    public void updatePlayListName(long id, String name) {
        playListDao.updatePlayListName(id, name);
    }

    public void updatePlayListModifyTime(long id, Date modifyTime) {
        playListDao.updatePlayListModifyTime(id, modifyTime);
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return playListDao.getPlayListsObservable()
                .map(entities -> mapList(entities, this::toPlayList));
    }

    public List<IdPair> getPlayListsIds() {
        return playListDao.getPlayListsIds();
    }

    public Observable<PlayList> getPlayListsObservable(long id) {
        return playListDao.getPlayListObservable(id)
                .takeWhile(entities -> !entities.isEmpty())
                .map(entities -> toPlayList(entities.get(0)));
    }

    public Observable<List<PlayListItem>> getPlayListItemsObservable(long playListId) {
        return playListDao.getPlayListItemsObservable(playListId)
                .map(entities -> mapList(entities, this::toItem));
    }

    public List<StoragePlayListItem> getPlayListItemsAsStorageItems(long playlistId) {
        return playListDao.getPlayListItemsAsStorageItems(playlistId);
    }

    public void deletePlayListEntry(long id, long playListId) {
        appDatabase.runInTransaction(() -> {
            int position = playListDao.selectPositionById(id);
            playListDao.deletePlayListEntry(id);
            playListDao.decreasePositionsAfter(position, playListId);
        });
    }

//    public void insertPlayListEntry(@Nullable Long storageItemId,
//                                    @Nullable Long storagePlayListId,
//                                    long audioId,
//                                    long playListId) {
//        appDatabase.runInTransaction(() -> {
//            int maxOrder = playListDao.selectMaxOrder(playListId);
//            playListDao.insertPlayListEntry(new PlayListEntryEntity(storageItemId,
//                    storagePlayListId,
//                    audioId,
//                    playListId,
//                    ++maxOrder));
//            playListDao.updatePlayListModifyTime(playListId, new Date(System.currentTimeMillis()));
//        });
//    }

    public void insertPlayListItems(List<RawPlayListItem> items,
                                    long playListId,
                                    @Nullable Long storagePlayListId) {
        insertPlayListItems(items,
                playListId,
                storagePlayListId,
                playListDao.selectMaxOrder(playListId)
        );
    }

    public void insertPlayListItems(List<RawPlayListItem> items,
                                    long playListId,
                                    @Nullable Long storagePlayListId,
                                    int position) {
        appDatabase.runInTransaction(() -> {
            List<PlayListEntryEntity> entities = new ArrayList<>(items.size());
            int orderPosition = position;
            for (RawPlayListItem item : items) {
                PlayListEntryEntity entryEntity = new PlayListEntryEntity(
                        item.getStorageItemId(),
                        storagePlayListId,
                        item.getAudioId(),
                        playListId,
                        orderPosition++
                );
                entities.add(entryEntity);
            }
            playListDao.insertPlayListEntries(entities);
            playListDao.increasePositionsByCountAfter(items.size(), --orderPosition, playListId);
        });
    }

    public Long selectStorageId(long id) {
        return playListDao.selectStorageId(id);
    }

    public void moveItems(long playListId,
                          long fromItemId,
                          int fromPos1,
                          long toItemId,
                          int toPos1) {
        Log.d("KEK7", "moveItems, from: " + fromPos1 + ", to: " + toPos1);

        int fromPos = playListDao.selectPositionById(fromItemId);
        int toPos = playListDao.selectPositionById(toItemId);//mmmmm
//        Log.d("KEK7", "moveItems, from(db): " + fromPos + ", to(db): " + toPos);

        playListDao.moveItems(playListId, fromPos, toPos);

//        appDatabase.runInTransaction(() -> {
//            // increment position
//            int fromPos = playListDao.selectPositionById(fromItemId);
//            int toPos = playListDao.selectPositionById(toItemId);
//
//            if (toPos > fromPos) {
//                // move other items
//                playListDao.moveItemsToBottom(playListId, fromPos, toPos);
//                playListDao.updateItemPosition(playListId, fromPos, toPos);
//            }
//            else if (toPos < fromPos) {
//                playListDao.moveItemsToTop(playListId, fromPos, toPos);
//                playListDao.updateItemPosition(playListId, fromPos, toPos);
//            }
//
//        });
    }

    private void swapItems(long playListId, int fromPos, int toPos) {
//        long prio1 = DatabaseUtils.longForQuery(db,
//                "SELECT priority FROM rules WHERE rule_id = " + 1, null);
//        long prio4 = playListDao.selectPositionById().longForQuery(db,
//                "SELECT priority FROM rules WHERE rule_id = " + 4, null);
//        db.execSQL("UPDATE rules SET priority = " + prio4 + " WHERE rule_id = " + 1);
//        db.execSQL("UPDATE rules SET priority = " + prio1 + " WHERE rule_id = " + 4);
//
//        playListDao.updateItemPosition(playListId, fromPos, toPos);
//        playListDao.updateItemPosition(playListId, fromPos, toPos);
    }

    private PlayListItem toItem(PlayListEntryDto entryDto) {
        return new PlayListItem(entryDto.getItemId(),
                entryDto.getStorageItemId(),
                CompositionMapper.toComposition(entryDto.getComposition()));
    }

    private PlayList toPlayList(PlayListPojo pojo) {
        return new PlayList(pojo.getId(),
                pojo.getStorageId(),
                pojo.getName(),
                pojo.getDateAdded(),
                pojo.getDateModified(),
                pojo.getCompositionsCount(),
                pojo.getTotalDuration());
    }

    private PlayListEntity toEntity(StoragePlayList storagePlayList) {
        return new PlayListEntity(storagePlayList.getId(),
                storagePlayList.getName(),
                storagePlayList.getDateAdded(),
                storagePlayList.getDateModified());
    }
}
