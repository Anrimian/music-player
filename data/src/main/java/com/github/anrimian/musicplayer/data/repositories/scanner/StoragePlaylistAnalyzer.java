package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;
import androidx.core.util.Pair;

import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
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
        List<StoragePlayList> currentPlayLists = playListsDao.getAllAsStoragePlayLists();
        LongSparseArray<StoragePlayList> currentPlayListsMap = AndroidCollectionUtils.mapToSparseArray(currentPlayLists,
                StoragePlayList::getId);

        List<Pair<StoragePlayList, List<StoragePlayListItem>>> addedPlayLists = new ArrayList<>();
        List<StoragePlayList> changedPlayLists = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentPlayListsMap,
                newPlayLists,
                this::hasActualChanges,
                playList -> {},
                item -> addedPlayLists.add(new Pair<>(item, playListsProvider.getPlayListItems(item.getId()))),
                changedPlayLists::add);

        if (hasChanges) {
            playListsDao.applyChanges(addedPlayLists, changedPlayLists);
        }
    }

    private boolean hasActualChanges(StoragePlayList first, StoragePlayList second) {
        return (!Objects.equals(first.getName(), second.getName())
                || !Objects.equals(first.getDateAdded(), second.getDateAdded())
                || !Objects.equals(first.getDateModified(), second.getDateModified()))
                && DateUtils.isAfter(first.getDateModified(), second.getDateModified());
    }
}
