package com.github.anrimian.musicplayer.data.database.dao.play_list;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

import androidx.core.util.Pair;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryDto;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListAlreadyExistsException;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.utils.functions.Function;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class PlayListsDaoWrapper {

    private final PlayListDao playListDao;
    private final CompositionsDao compositionsDao;
    private final AppDatabase appDatabase;

    public PlayListsDaoWrapper(PlayListDao playListDao,
                               CompositionsDao compositionsDao,
                               AppDatabase appDatabase) {
        this.playListDao = playListDao;
        this.compositionsDao = compositionsDao;
        this.appDatabase = appDatabase;
    }

    public void applyChanges(List<Pair<StoragePlayList, List<StoragePlayListItem>>> addedPlayLists,
                             List<Change<AppPlayList, StoragePlayList>> changedPlayLists,
                             List<Pair<AppPlayList, List<StoragePlayListItem>>> itemsToInsert) {
        appDatabase.runInTransaction(() -> {
            //add
            for (Pair<StoragePlayList, List<StoragePlayListItem>> addedPlaylist: addedPlayLists) {
                StoragePlayList storagePlayList = addedPlaylist.first;
                assert storagePlayList != null;
                long storageId = storagePlayList.getStorageId();
                if (playListDao.isPlayListExistsByStorageId(storageId)) {
                    continue;
                }

                long id = playListDao.insertPlayListEntity(new PlayListEntity(
                        storageId,
                        getUniquePlayListName(storagePlayList.getName()),
                        storagePlayList.getDateAdded(),
                        storagePlayList.getDateModified()
                ));
                insertPlayListItems(addedPlaylist.second, id);
            }

            //update
            for (Change<AppPlayList, StoragePlayList> change: changedPlayLists) {
                AppPlayList oldItem = change.getOld();
                StoragePlayList newItem = change.getObj();
                long id = newItem.getStorageId();

                Date newDateModified = newItem.getDateModified();
                if (!oldItem.getDateModified().equals(newDateModified)) {
                    playListDao.updatePlayListModifyTimeByStorageId(id, newDateModified);
                }
                String newName = newItem.getName();
                if (!oldItem.getName().equals(newName)) {
                    playListDao.updatePlayListNameByStorageId(id, getUniquePlayListName(newName));
                }
            }

            //insert items
            for (Pair<AppPlayList, List<StoragePlayListItem>> itemToInsert: itemsToInsert){
                insertPlayListItems(itemToInsert.second, itemToInsert.first.getId());
            }
        });
    }

    public long insertPlayList(String name, Date dateAdded, Date dateModified, Function<Long> storagePlayListFetcher) {
        if (playListDao.isPlayListWithNameExists(name)) {
            throw new PlayListAlreadyExistsException();
        }
        Long storageId = storagePlayListFetcher.call();
        PlayListEntity entity = new PlayListEntity(storageId, name, dateAdded, dateModified);
        long id = playListDao.insertPlayListEntity(entity);
        if (id == -1) {
            throw new IllegalStateException("db not modified");
        }
        return id;
    }

    public long insertPlayList(StoragePlayList playList) {
        PlayListEntity entity = new PlayListEntity(
                playList.getStorageId(),
                playList.getName(),
                playList.getDateAdded(),
                playList.getDateModified());
        long id = playListDao.insertPlayListEntity(entity);
        if (id == -1) {
            throw new IllegalStateException("db not modified");
        }
        return id;
    }

    public List<AppPlayList> getAllAsStoragePlayLists() {
        return playListDao.getAllAsStoragePlayLists();
    }

    public long getPlayListItemsCount(long id) {
        return playListDao.getPlayListItemsCount(id);
    }

    public void deletePlayList(long id) {
        playListDao.deletePlayList(id);
    }

    public void updatePlayListName(long id, String name) {
        if (playListDao.isPlayListWithNameExists(name)) {
            throw new PlayListAlreadyExistsException();
        }
        playListDao.updatePlayListName(id, name);
    }

    public void updatePlayListModifyTime(long id, Date modifyTime) {
        playListDao.updatePlayListModifyTime(id, modifyTime);
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return playListDao.getPlayListsObservable();
    }

    public List<IdPair> getPlayListsIds() {
        return playListDao.getPlayListsIds();
    }

    public Observable<PlayList> getPlayListsObservable(long id) {
        return playListDao.getPlayListObservable(id)
                .takeWhile(items -> !items.isEmpty())
                .map(items -> items.get(0));
    }

    public Observable<List<PlayListItem>> getPlayListItemsObservable(long playListId,
                                                                     boolean useFileName) {
        String query = PlayListDao.getPlaylistItemsQuery(useFileName);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, new Object[] {playListId} );
        return playListDao.getPlayListItemsObservable(sqlQuery)
                .map(entities -> mapList(entities, this::toItem));
    }

    public List<StoragePlayListItem> getPlayListItemsAsStorageItems(long playlistId) {
        return playListDao.getPlayListItemsAsStorageItems(playlistId);
    }

    public int deletePlayListEntry(long id, long playListId) {
        return appDatabase.runInTransaction(() -> {
            int position = playListDao.selectPositionById(id);
            playListDao.deletePlayListEntry(id);
            playListDao.decreasePositionsAfter(position, playListId);
            return position;
        });
    }

    public void insertPlayListItems(List<StoragePlayListItem> items, long playListId) {
        insertPlayListItems(items, playListId, playListDao.selectMaxOrder(playListId));
    }

    public void addCompositions(List<Composition> compositions,
                                long playListId,
                                int position) {
        appDatabase.runInTransaction(() -> {
            playListDao.increasePositionsByCountAfter(compositions.size(), position, playListId);

            List<PlayListEntryEntity> entities = new ArrayList<>(compositions.size());
            int orderPosition = position;
            for (Composition item : compositions) {
                PlayListEntryEntity entryEntity = new PlayListEntryEntity(
                        null,
                        item.getId(),
                        playListId,
                        orderPosition++
                );
                entities.add(entryEntity);
            }
            playListDao.insertPlayListEntries(entities);
            playListDao.updatePlayListModifyTime(playListId, new Date());
        });
    }

    public void insertPlayListItems(List<StoragePlayListItem> items,
                                    long playListId,
                                    int position) {
        appDatabase.runInTransaction(() -> {
            playListDao.increasePositionsByCountAfter(items.size(), position, playListId);

            List<PlayListEntryEntity> entities = new ArrayList<>(items.size());
            int orderPosition = position;
            for (StoragePlayListItem item : items) {
                long compositionId = compositionsDao.selectIdByStorageId(item.getAudioId());
                if (compositionId == 0) {
                    continue;
                }

                PlayListEntryEntity entryEntity = new PlayListEntryEntity(
                        item.getItemId(),
                        compositionId,
                        playListId,
                        orderPosition++
                );
                entities.add(entryEntity);
            }
            playListDao.insertPlayListEntries(entities);
        });
    }

    public Long selectStorageId(long id) {
        return playListDao.selectStorageId(id);
    }

    public Long selectStorageItemId(long id) {
        return playListDao.selectStorageItemId(id);
    }

    public void moveItems(long playListId, int fromPos, int toPos) {
        playListDao.moveItems(playListId, fromPos, toPos);
    }

    private PlayListItem toItem(PlayListEntryDto entryDto) {
        return new PlayListItem(entryDto.getItemId(), entryDto.getComposition());
    }

    public void updateStorageId(long id, Long storageId) {
        playListDao.updateStorageId(id, storageId);
    }

    public boolean isPlayListExists(long playListId) {
        return playListDao.isPlayListExists(playListId);
    }

    private String getUniquePlayListName(String name) {
        return getUniquePlayListName(name, "");
    }

    private String getUniquePlayListName(String name, String salt) {
        String uniqueName = name;
        int i = 0;
        while (playListDao.isPlayListWithNameExists(uniqueName)) {
            i++;
            uniqueName = name + "("+ i + ")" + salt;
        }
        return uniqueName;
    }
}
