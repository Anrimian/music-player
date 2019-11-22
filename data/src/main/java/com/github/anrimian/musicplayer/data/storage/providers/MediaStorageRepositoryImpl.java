package com.github.anrimian.musicplayer.data.storage.providers;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.repositories.MediaStorageRepository;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
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
    private LongSparseArray<Disposable> playListEntriesDisposable = new LongSparseArray<>();

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
    public synchronized void rescanStorage() {
        Completable.fromAction(() -> {
            applyCompositionsData(musicProvider.getCompositions());
            applyPlayListData(playListsProvider.getPlayLists());

            List<IdPair> allPlayLists = playListsDao.getPlayListsIds();
            for (IdPair playListIds: allPlayLists) {
                long storageId = playListIds.getStorageId();
                long dbId = playListIds.getDbId();
                applyPlayListItemsData(dbId, playListsProvider.getPlayListItems(storageId));
            }
        }).subscribeOn(scheduler).subscribe();
    }

    private void onNewCompositionsFromMediaStorageReceived(LongSparseArray<StorageComposition> newCompositions) {
        applyCompositionsData(newCompositions);

        if (playListsDisposable == null) {
            playListsDisposable = playListsProvider.getPlayListsObservable()
                    .startWith(playListsProvider.getPlayLists())
                    .subscribeOn(scheduler)
                    .subscribe(this::onStoragePlayListReceived);
        }
    }

    private void onStoragePlayListReceived(LongSparseArray<StoragePlayList> newPlayLists) {
        applyPlayListData(newPlayLists);

        List<IdPair> allPlayLists = playListsDao.getPlayListsIds();
        for (IdPair playListids: allPlayLists) {
            long storageId = playListids.getStorageId();
            long dbId = playListids.getDbId();
            if (!playListEntriesDisposable.containsKey(dbId)) {
                Disposable disposable = playListsProvider.getPlayListEntriesObservable(storageId)
                        .startWith(playListsProvider.getPlayListItems(storageId))
                        .subscribeOn(scheduler)
                        .subscribe(entries -> applyPlayListItemsData(dbId, entries));
                playListEntriesDisposable.put(dbId, disposable);
            }
        }
    }

    private synchronized void applyPlayListItemsData(long playListId,
                                                     List<StoragePlayListItem> newItems) {
        List<StoragePlayListItem> currentItems = playListsDao.getPlayListItemsAsStorageItems(playListId);
        LongSparseArray<StoragePlayListItem> currentItemsMap = AndroidCollectionUtils.mapToSparseArray(
                currentItems,
                StoragePlayListItem::getItemId);

        LongSparseArray<StoragePlayListItem> newItemsMap = AndroidCollectionUtils.mapToSparseArray(
                newItems,
                StoragePlayListItem::getItemId);

        List<StoragePlayListItem> addedItems = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentItemsMap,
                newItemsMap,
                (o1, o2) -> false,
                item -> {},
                addedItems::add,
                item -> {});

        if (hasChanges) {
            playListsDao.insertPlayListItems(addedItems, playListId);
        }
    }

    private synchronized void applyPlayListData(LongSparseArray<StoragePlayList> newPlayLists) {
        List<StoragePlayList> currentPlayLists = playListsDao.getAllAsStoragePlayLists();
        LongSparseArray<StoragePlayList> currentPlayListsMap = AndroidCollectionUtils.mapToSparseArray(currentPlayLists,
                StoragePlayList::getId);

        List<StoragePlayList> addedPlayLists = new ArrayList<>();
        List<StoragePlayList> changedPlayLists = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentPlayListsMap,
                newPlayLists,
                this::hasActualChanges,
                playList -> {},
                addedPlayLists::add,
                changedPlayLists::add);

        if (hasChanges) {
            playListsDao.applyChanges(addedPlayLists, changedPlayLists);
        }
    }

    private synchronized void applyCompositionsData(LongSparseArray<StorageComposition> newCompositions) {
        LongSparseArray<StorageComposition> currentCompositions = compositionsDao.selectAllAsStorageCompositions();

        List<StorageComposition> addedCompositions = new ArrayList<>();
        List<StorageComposition> deletedCompositions = new ArrayList<>();
        List<StorageComposition> changedCompositions = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentCompositions,
                newCompositions,
                this::hasActualChanges,
                deletedCompositions::add,
                addedCompositions::add,
                changedCompositions::add);

        if (hasChanges) {
            compositionsDao.applyChanges(addedCompositions, deletedCompositions, changedCompositions);
        }
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