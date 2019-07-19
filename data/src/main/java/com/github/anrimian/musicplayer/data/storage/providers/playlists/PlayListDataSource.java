package com.github.anrimian.musicplayer.data.storage.providers.playlists;

import com.github.anrimian.musicplayer.data.models.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
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

public class PlayListDataSource {

    private Disposable disposable;

    private final StoragePlayList rawPlayList;
    private final StoragePlayListsProvider storagePlayListsProvider;
    private final StorageMusicDataSource storageMusicDataSource;

    private BehaviorSubject<List<PlayListItem>> itemsSubject = BehaviorSubject.create();
    private BehaviorSubject<PlayList> playListSubject = BehaviorSubject.create();
//    private BehaviorSubject<PlayList> playListSubject = BehaviorSubject.create();

//    private final Observable<List<StoragePlayListItem>> playListItemObservable;
//    private final Observable<List<StoragePlayListItem>> playListItemObservable;

    public PlayListDataSource(StoragePlayList rawPlayList,
                       StoragePlayListsProvider storagePlayListsProvider,
                       StorageMusicDataSource storageMusicDataSource) {
        this.rawPlayList = rawPlayList;
//        this.rawPlayList = rawPlayList;
//        itemsSubject = BehaviorSubject.createDefault(items);
//        playListSubject = BehaviorSubject.createDefault(rawPlayList);
//
//        disposable = Observable.combineLatest(Observable.just(storageItems).mergeWith(storagePlayListsProvider.getPlayListChangeObservable(rawPlayList.getId())),
//                storageMusicDataSource.getCompositionObservable(),
//                this::createPlayList)
//                .subscribe(this::onPlayListItemsChanged);
        this.storagePlayListsProvider = storagePlayListsProvider;
        this.storageMusicDataSource = storageMusicDataSource;
    }

//    PlayListDataSource(PlayList rawPlayList) {
//        this.rawPlayList = rawPlayList;
//        itemsSubject = BehaviorSubject.create();
//        playListSubject = BehaviorSubject.create();
//    }

    public Observable<List<PlayListItem>> getPlayListItemsObservable() {
        return withDefaultValue(itemsSubject, this::loadPlayListItems);
    }

    public Observable<PlayList> getPlayListObservable() {
        return withDefaultValue(playListSubject, this::loadPlayList);
    }

//    PlayList getRawPlayList() {
//        return rawPlayList;
//    }

    public void updatePlayList(StoragePlayList newPlayList) {
        PlayList playList = playListSubject.getValue();
        PlayList updatedPlayList = new PlayList(rawPlayList.getId(),
                newPlayList.getName(),
                newPlayList.getDateAdded(),
                newPlayList.getDateModified(),
                playList.getCompositionsCount(),
                playList.getTotalDuration());
        playListSubject.onNext(updatedPlayList);
    }

    public void dispose() {
        if (disposable != null) {
            disposable.dispose();
        }
        itemsSubject.onComplete();
        playListSubject.onComplete();
    }

    public Single<PlayList> getPlayList() {
        return Single.fromCallable(this::loadPlayList);
    }

    private List<PlayListItem> loadPlayListItems() {
        List<PlayListItem> playListItems = itemsSubject.getValue();
        if (playListItems == null) {
            synchronized (this) {
                List<StoragePlayListItem> storageItems = storagePlayListsProvider.getPlayListItems(rawPlayList.getId());
                Map<Long, Composition> compositionMap = storageMusicDataSource.getCompositionsMap();
                playListItems = createPlayList(storageItems, compositionMap);
                subscribeOnPlayListItemsChanges(storageItems, compositionMap);
            }
        }
        return playListItems;
    }

    private PlayList loadPlayList() {
        PlayList playList = playListSubject.getValue();
//        List<PlayListItem> playListItems = loadPlayListItems();
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
        disposable = Observable.combineLatest(storagePlayListsProvider.getPlayListChangeObservable(rawPlayList.getId()).defaultIfEmpty(items),
                storageMusicDataSource.getCompositionObservable().defaultIfEmpty(compositionMap),//error with no value//TODO optimize
                this::createPlayList)
                .subscribe(this::onPlayListItemsChanged);
    }

    private void onPlayListItemsChanged(List<PlayListItem> items) {
        PlayList playList = playListSubject.getValue();
        PlayList updatedPlayList = new PlayList(playList.getId(),
                playList.getName(),
                playList.getDateAdded(),
                playList.getDateModified(),
                items.size(),
                getTotalDuration(items));
        playListSubject.onNext(updatedPlayList);
        itemsSubject.onNext(items);
    }

//    private List<PlayListItem> getPlayListItems() {
//        return createPlayList(storagePlayListsProvider.getPlayListItems(rawPlayList.getId()),
//                storageMusicDataSource.getCompositionsMap()
//        );
//    }

    private List<PlayListItem> createPlayList(List<StoragePlayListItem> items, Map<Long, Composition> compositionMap) {
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
}
