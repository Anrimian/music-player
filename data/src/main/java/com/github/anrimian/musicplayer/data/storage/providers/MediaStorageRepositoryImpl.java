package com.github.anrimian.musicplayer.data.storage.providers;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.playlist.RawPlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.repositories.MediaStorageRepository;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.changes.map.MapChangeProcessor;
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;

public class MediaStorageRepositoryImpl implements MediaStorageRepository {

    private final StorageMusicProvider musicProvider;
    private final StoragePlayListsProvider playListsProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final PlayListsDaoWrapper playListsDao;
    private final Scheduler scheduler;

    private Disposable compositionsDisposable;
    private Disposable playListsDisposable;
    private HashMap<Long, Disposable> playListEntriesDisposable = new HashMap<>();

    public MediaStorageRepositoryImpl(StorageMusicProvider musicProvider,
                                      StoragePlayListsProvider playListsProvider,
                                      CompositionsDaoWrapper compositionsDao,
                                      PlayListsDaoWrapper playListsDao,
                                      Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.playListsProvider = playListsProvider;
        this.compositionsDao = compositionsDao;
        this.playListsDao = playListsDao;
        this.scheduler = scheduler;
    }

    @Override
    public void initialize() {
        compositionsDisposable = musicProvider.getCompositionsObservable()
                .startWith(musicProvider.getCompositions())
                .subscribeOn(scheduler)
                .subscribe(this::onNewCompositionsFromMediaStorageReceived);
    }

    @Override
    public void rescanStorage() {
        // not implemented
//        onNewCompositionsFromMediaStorageReceived(musicProvider.getCompositions());
    }

    private void onNewCompositionsFromMediaStorageReceived(Map<Long, StorageComposition> newCompositions) {
        Map<Long, StorageComposition> currentCompositions = compositionsDao.selectAllAsStorageCompositions();

        List<StorageComposition> addedCompositions = new ArrayList<>();
        List<StorageComposition> deletedCompositions = new ArrayList<>();
        List<StorageComposition> changedCompositions = new ArrayList<>();
        boolean hasChanges = MapChangeProcessor.processChanges2(currentCompositions,
                newCompositions,
                this::hasActualChanges,
                deletedCompositions::add,
                addedCompositions::add,
                changedCompositions::add);

        if (hasChanges) {
            compositionsDao.applyChanges(addedCompositions, deletedCompositions, changedCompositions);
        }
        if (playListsDisposable == null) {
            playListsDisposable = playListsProvider.getPlayListsObservable()
                    .startWith(playListsProvider.getPlayLists())
                    .subscribeOn(scheduler)
                    .subscribe(this::onStoragePlayListReceived);
        }

    }

    private void onStoragePlayListReceived(Map<Long, StoragePlayList> newPlayLists) {
        List<StoragePlayList> currentPlayLists = playListsDao.getAllAsStoragePlayLists();
        Map<Long, StoragePlayList> currentPlayListsMap = ListUtils.mapToMap(currentPlayLists,
                new HashMap<>(),
                StoragePlayList::getId);

        List<StoragePlayList> addedPlayLists = new ArrayList<>();
        List<StoragePlayList> changedPlayLists = new ArrayList<>();
        boolean hasChanges = MapChangeProcessor.processChanges2(currentPlayListsMap,
                newPlayLists,
                this::hasActualChanges,
                playList -> {},
                addedPlayLists::add,
                changedPlayLists::add);

        if (hasChanges) {
            playListsDao.applyChanges(addedPlayLists, changedPlayLists);
        }

        List<IdPair> allPlayLists = playListsDao.getPlayListsIds();
        for (IdPair playListids: allPlayLists) {
            Long storageId = playListids.getStorageId();
            if (storageId == null) {
                continue;
            }
            long dbId = playListids.getDbId();
            if (!playListEntriesDisposable.containsKey(dbId)) {
                Disposable disposable = playListsProvider.getPlayListEntriesObservable(storageId)
                        .startWith(playListsProvider.getPlayListItems(storageId))
                        .subscribeOn(scheduler)
                        .subscribe(entries -> onPlayListEntriesReceived(dbId, storageId, entries));
                playListEntriesDisposable.put(dbId, disposable);
            }
        }
    }

    private void onPlayListEntriesReceived(long playListId,
                                           long storagePlayListId,
                                           List<StoragePlayListItem> newItems) {
        List<StoragePlayListItem> currentItems = playListsDao.getPlayListItemsAsStorageItems(playListId);
        Map<Long, StoragePlayListItem> currentItemsMap = ListUtils.mapToMap(currentItems,
                new HashMap<>(),
                StoragePlayListItem::getItemId);

        Map<Long, StoragePlayListItem> newItemsMap = ListUtils.mapToMap(newItems,
                new HashMap<>(),
                StoragePlayListItem::getItemId);

        List<RawPlayListItem> addedItems = new ArrayList<>();
        boolean hasChanges = MapChangeProcessor.processChanges2(currentItemsMap,
                newItemsMap,
                (o1, o2) -> true,
                item -> {},
                item -> addedItems.add(rawPlayListItem(item.getItemId(), item.getCompositionId())),
                item -> {});

        if (hasChanges) {
            playListsDao.insertPlayListItems(addedItems, playListId, storagePlayListId);
        }
    }

    private RawPlayListItem rawPlayListItem(long itemId, long storageCompositionId) {
        return new RawPlayListItem(
                itemId,
                compositionsDao.selectIdByStorageId(storageCompositionId)
        );
    }

    private boolean hasActualChanges(StoragePlayList first, StoragePlayList second) {
        return (!Objects.equals(first.getName(), second.getName())
                || !Objects.equals(first.getDateAdded(), second.getDateAdded())
                || !Objects.equals(first.getDateModified(), second.getDateModified()))
                && DateUtils.isAfter(first.getDateModified(), second.getDateModified());
    }

    private boolean hasActualChanges(StorageComposition first, StorageComposition second) {
        return !(Objects.equals(first.getAlbum(), second.getAlbum())
                && Objects.equals(first.getArtist(), second.getArtist())
                && Objects.equals(first.getDateAdded(), second.getDateAdded())
                && Objects.equals(first.getDateModified(), second.getDateModified())
                && first.getDuration() == second.getDuration()
                && Objects.equals(first.getFilePath(), second.getFilePath())
                && first.getSize() == second.getSize()
                && Objects.equals(first.getTitle(), second.getTitle()))
                && DateUtils.isAfter(first.getDateModified(), second.getDateModified());
    }
}