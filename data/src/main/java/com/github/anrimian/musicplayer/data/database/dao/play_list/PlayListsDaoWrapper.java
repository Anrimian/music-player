package com.github.anrimian.musicplayer.data.database.dao.play_list;

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

import javax.annotation.Nonnull;
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

    public void insertPlayList(@Nullable Long storageId,
                               @Nonnull String name,
                               @Nonnull Date dateAdded,
                               @Nonnull Date dateModified) {
        PlayListEntity entity = new PlayListEntity(
                storageId,
                name,
                dateAdded,
                dateModified);
        playListDao.insertPlayListEntity(entity);
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

    public Observable<List<PlayList>> getPlayLists() {
        return playListDao.getPlayListsObservable()
                .map(entities -> mapList(entities, this::toPlayList));
    }

    public Observable<PlayList> getPlayListsObservable(long id) {
        return playListDao.getPlayListObservable(id)
                .flatMap(entities -> Observable.create(emitter -> {
                    if (entities.isEmpty()) {
                        emitter.onComplete();
                    } else {
                        emitter.onNext(toPlayList(entities.get(0)));
                    }
                }));
    }

    public Observable<List<PlayListItem>> getPlayListItemsObservable(long playListId) {
        return playListDao.getPlayListItemsObservable(playListId)
                .map(entities -> mapList(entities, this::toItem));
    }

    public List<StoragePlayListItem> getPlayListItemsAsStorageItems(long playlistId) {
        return playListDao.getPlayListItemsAsStorageItems(playlistId);
    }

    public void deletePlayListEntry(long id) {
        playListDao.deletePlayList(id);
    }

    public void insertPlayListEntry(@Nullable Long storageItemId,
                                    @Nullable Long storagePlayListId,
                                    long audioId,
                                    long playListId) {
        appDatabase.runInTransaction(() -> {
            int maxOrder = playListDao.selectMaxOrder(playListId);
            playListDao.insertPlayListEntry(new PlayListEntryEntity(storageItemId,
                    storagePlayListId,
                    audioId,
                    playListId,
                    ++maxOrder));
            playListDao.updatePlayListModifyTime(playListId, new Date(System.currentTimeMillis()));
        });
    }

    public void insertPlayListItems(List<RawPlayListItem> items,
                                    long playListId,
                                    @Nullable Long storagePlayListId) {
        appDatabase.runInTransaction(() -> {
            List<PlayListEntryEntity> entities = new ArrayList<>(items.size());
            int position = playListDao.selectMaxOrder(playListId);
            for (RawPlayListItem item : items) {
                PlayListEntryEntity entryEntity = new PlayListEntryEntity(
                        item.getStorageItemId(),
                        storagePlayListId,
                        item.getAudioId(),
                        playListId,
                        ++position
                );
                entities.add(entryEntity);
            }
            playListDao.insertPlayListEntries(entities);
        });
    }

    public void swapItems(long firstItemId,
                          int firstPosition,
                          long secondItemId,
                          int secondPosition) {
        appDatabase.runInTransaction(() -> {
            playListDao.updateItemPosition(firstItemId, secondPosition);
            playListDao.updateItemPosition(secondItemId, firstPosition);
        });
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
