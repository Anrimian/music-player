package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;
import androidx.core.util.Pair;

import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class StoragePlaylistAnalyzer {

    private final PlayListsDaoWrapper playListsDao;
    private final StoragePlayListsProvider playListsProvider;

    public StoragePlaylistAnalyzer(PlayListsDaoWrapper playListsDao,
                                   StoragePlayListsProvider playListsProvider) {
        this.playListsDao = playListsDao;
        this.playListsProvider = playListsProvider;
    }

    public synchronized void applyPlayListData(LongSparseArray<StoragePlayList> newPlayLists) {
        List<AppPlayList> currentPlayLists = playListsDao.getAllAsStoragePlayLists();
        LongSparseArray<AppPlayList> currentPlayListsMap = AndroidCollectionUtils.mapToSparseArray(currentPlayLists,
                AppPlayList::getStorageId);

        List<Pair<StoragePlayList, List<StoragePlayListItem>>> addedPlayLists = new ArrayList<>();
        List<Change<AppPlayList, StoragePlayList>> changedPlayLists = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processDiffChanges(currentPlayListsMap,
                newPlayLists,
                this::hasActualChanges,
                playList -> {},
                item -> addedPlayLists.add(new Pair<>(item, playListsProvider.getPlayListItems(item.getStorageId()))),
                (old, item) -> changedPlayLists.add(new Change<>(old, item)));

        //add items to empty existing playlist
        List<Pair<AppPlayList, List<StoragePlayListItem>>> itemsToInsert = new ArrayList<>();
        for (AppPlayList currentPlayList: currentPlayLists) {
            if (playListsDao.getPlayListItemsCount(currentPlayList.getId()) == 0) {
                List<StoragePlayListItem> items = playListsProvider.getPlayListItems(currentPlayList.getStorageId());
                if (!items.isEmpty()) {
                    itemsToInsert.add(new Pair<>(currentPlayList, items));
                }
            }
        }

        if (hasChanges) {
            playListsDao.applyChanges(addedPlayLists, changedPlayLists, itemsToInsert);
        }
    }

    private boolean hasActualChanges(AppPlayList first, StoragePlayList second) {
        return (!Objects.equals(first.getName(), second.getName()))
                && DateUtils.isAfter(first.getDateModified(), second.getDateModified());
    }
}
