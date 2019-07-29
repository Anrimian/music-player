package com.github.anrimian.musicplayer.data.repositories.playlists;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.musicplayer.domain.models.utils.PlayListItemHelper.getTotalDuration;

class PlayListDataSource {

    private final StoragePlayList rawPlayList;
    private final StoragePlayListsProvider storagePlayListsProvider;
    private final StorageMusicDataSource storageMusicDataSource;

    private BehaviorSubject<List<PlayListItem>> itemsSubject = BehaviorSubject.create();
    private BehaviorSubject<PlayList> playListSubject = BehaviorSubject.create();

    private Disposable disposable;

    PlayListDataSource(StoragePlayList rawPlayList,
                       StoragePlayListsProvider storagePlayListsProvider,
                       StorageMusicDataSource storageMusicDataSource) {
        this.rawPlayList = rawPlayList;
        this.storagePlayListsProvider = storagePlayListsProvider;
        this.storageMusicDataSource = storageMusicDataSource;
    }

    Observable<List<PlayListItem>> getPlayListItemsObservable() {
        return withDefaultValue(itemsSubject, this::loadPlayListItems);
    }

    Observable<PlayList> getPlayListObservable() {
        return withDefaultValue(playListSubject, this::loadPlayList);
    }

    Single<PlayList> getPlayList() {
        return Single.fromCallable(this::loadPlayList);
    }

    void updatePlayList(StoragePlayList newPlayList) {
        PlayList playList = playListSubject.getValue();
        if (playList == null) {
            return;
        }
        PlayList updatedPlayList = new PlayList(rawPlayList.getId(),
                newPlayList.getName(),
                newPlayList.getDateAdded(),
                newPlayList.getDateModified(),
                playList.getCompositionsCount(),
                playList.getTotalDuration());
        playListSubject.onNext(updatedPlayList);
    }

    void dispose() {
        if (disposable != null) {
            disposable.dispose();
        }
        itemsSubject.onComplete();
        playListSubject.onComplete();
    }


    private List<PlayListItem> loadPlayListItems() {
        List<PlayListItem> playListItems = itemsSubject.getValue();
        if (playListItems == null) {
            synchronized (this) {
                List<StoragePlayListItem> storageItems = storagePlayListsProvider.getPlayListItems(rawPlayList.getId());
                Map<Long, Composition> compositionMap = storageMusicDataSource.getCompositionsMap();
                playListItems = mergeItems(storageItems, compositionMap);
                subscribeOnPlayListItemsChanges(storageItems, compositionMap);
            }
        }
        return playListItems;
    }

    private PlayList loadPlayList() {
        PlayList playList = playListSubject.getValue();
        if (playList == null) {
            synchronized (this) {
                playList = createPlayList(rawPlayList, loadPlayListItems());
            }
        }
        return playList;
    }

    private PlayList createPlayList(StoragePlayList rawPlayList, List<PlayListItem> items) {
        return new PlayList(rawPlayList.getId(),
                rawPlayList.getName(),
                rawPlayList.getDateAdded(),
                rawPlayList.getDateModified(),
                items.size(),
                getTotalDuration(items));
    }

    private void subscribeOnPlayListItemsChanges(List<StoragePlayListItem> items, Map<Long, Composition> compositionMap) {
        disposable = Observable.combineLatest(
                Observable.just(items).mergeWith(storagePlayListsProvider.getPlayListChangeObservable(rawPlayList.getId())),
                Observable.just(compositionMap).mergeWith(storageMusicDataSource.getCompositionObservable()),//can be optimized
                this::mergeItems)
                .skip(1)
                .subscribe(this::onPlayListItemsChanged);
    }

    private List<PlayListItem> mergeItems(List<StoragePlayListItem> items, Map<Long, Composition> compositionMap) {
        List<PlayListItem> playListItems = new ArrayList<>(items.size());
        for (StoragePlayListItem item: items) {
            Composition composition = compositionMap.get(item.getCompositionId());
            if (composition == null) {
                continue;
            }
            playListItems.add(new PlayListItem(item.getItemId(), composition));
        }
        return playListItems;
    }

    private void onPlayListItemsChanged(List<PlayListItem> items) {
        itemsSubject.onNext(items);

        PlayList playList = playListSubject.getValue();
        if (playList == null) {
            return;
        }
        PlayList updatedPlayList = new PlayList(playList.getId(),
                playList.getName(),
                playList.getDateAdded(),
                playList.getDateModified(),
                items.size(),
                getTotalDuration(items));
        playListSubject.onNext(updatedPlayList);
    }
}
