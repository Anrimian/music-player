package com.github.anrimian.musicplayer.data.repositories.playlists;

import com.github.anrimian.musicplayer.data.models.StoragePlayList;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListNotFoundException;
import com.github.anrimian.musicplayer.data.repositories.playlists.comparators.PlayListModifyDateComparator;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.changes.MapChangeProcessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.getTotalDuration;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapToList;

public class PlayListsRepositoryImpl implements PlayListsRepository {

    private final StoragePlayListsProvider storagePlayListsProvider;
    private final Scheduler scheduler;

    private final BehaviorSubject<Map<Long, PlayListFullModel>> playListsSubject = BehaviorSubject.create();

    private Disposable changeDisposable;
    private Map<Long, PlayListFullModel> playListMap;

    public PlayListsRepositoryImpl(StoragePlayListsProvider storagePlayListsProvider,
                                   Scheduler scheduler) {
        this.storagePlayListsProvider = storagePlayListsProvider;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<List<PlayList>> getPlayListsObservable() {
        return withDefaultValue(playListsSubject, this::getPlayListsMap)
                .map(this::toSortedPlayLists)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<PlayList> getPlayListObservable(long playlistId) {
        PlayListFullModel playListFullModel = getPlayListsMap().get(playlistId);
        Observable<PlayList> observable;
        if (playListFullModel == null) {
            observable = Observable.error(new PlayListNotFoundException());
        } else {
            observable = playListFullModel.getPlayListObservable();
        }
        return observable.subscribeOn(scheduler);
    }

    @Override
    public Observable<List<Composition>> getCompositionsObservable(long playlistId) {
        PlayListFullModel playListFullModel = getPlayListsMap().get(playlistId);
        Observable<List<Composition>> observable;
        if (playListFullModel == null) {
            observable = Observable.error(new PlayListNotFoundException());
        } else {
            observable = playListFullModel.getCompositionsObservable();
        }
        return observable.subscribeOn(scheduler);
    }

    @Override
    public Single<PlayList> createPlayList(String name) {
        return Single.fromCallable(() -> storagePlayListsProvider.createPlayList(name))
                .map(playList -> toPlayList(playList, Collections.emptyList()))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToPlayList(List<Composition> compositions, PlayList playList) {
        return Completable.fromAction(() -> storagePlayListsProvider.addCompositionsToPlayList(
                compositions,
                playList.getId(),
                playList.getCompositionsCount())
        ).subscribeOn(scheduler);
    }

    private List<PlayList> toSortedPlayLists(Map<Long, PlayListFullModel> playListMap) {
        List<PlayList> list = mapToList(playListMap, PlayListFullModel::getPlayList);
        Collections.sort(list, new PlayListModifyDateComparator());
        return list;
    }

    private Map<Long, PlayListFullModel> getPlayListsMap() {
        if (playListMap == null) {
            synchronized (this) {
                if (playListMap == null) {
                    playListMap = createPlayListMap(storagePlayListsProvider.getPlayLists());
                    subscribeOnPlayListChanges();
                }
            }
        }
        return playListMap;
    }

    private void subscribeOnPlayListChanges() {
        changeDisposable = storagePlayListsProvider.getChangeObservable()
                .subscribeOn(scheduler)
                .subscribe(this::onPlayListsChanged);
    }

    private void onPlayListsChanged(List<StoragePlayList> playLists) {
        Map<Long, PlayListFullModel> newPlayListMap = new HashMap<>();
        for (StoragePlayList storagePlayList: playLists) {
            long id = storagePlayList.getId();
            PlayList playList = toPlayList(storagePlayList, Collections.emptyList());
            newPlayListMap.put(id, new PlayListFullModel(playList));
        }
        boolean updated = MapChangeProcessor.processChanges(playListMap,
                newPlayListMap,
                this::hasDirectChanges,
                deletedEntry -> playListMap.remove(deletedEntry.getKey()).dispose(),
                this::onNewPlayListReceived,
                modifiedEntry -> playListMap.get(modifiedEntry.getKey())
                        .updatePlayList(modifiedEntry.getValue().getPlayList()));

        if (updated) {
            playListsSubject.onNext(playListMap);
        }
    }

    private void onNewPlayListReceived(Map.Entry<Long, PlayListFullModel> entry) {
        PlayList oldPlayList = entry.getValue().getPlayList();
        List<Composition> compositions = storagePlayListsProvider.getCompositions(oldPlayList.getId());
        PlayList playList = new PlayList(oldPlayList.getId(),
                oldPlayList.getName(),
                oldPlayList.getDateAdded(),
                oldPlayList.getDateModified(),
                compositions.size(),
                getTotalDuration(compositions));
        playListMap.put(entry.getKey(), new PlayListFullModel(playList, compositions));
    }

    private boolean hasDirectChanges(@Nonnull PlayListFullModel firstModel,
                                     @Nonnull PlayListFullModel secondModel) {
        PlayList first = firstModel.getPlayList();
        PlayList second = secondModel.getPlayList();
        return !Objects.equals(first.getName(), second.getName())
                || !Objects.equals(first.getDateAdded(), second.getDateAdded())
                || !Objects.equals(first.getDateModified(), second.getDateModified());
    }

    private Map<Long, PlayListFullModel> createPlayListMap(List<StoragePlayList> storagePlayLists) {
        Map<Long, PlayListFullModel> playListMap = new HashMap<>();
        for (StoragePlayList storagePlayList: storagePlayLists) {
            long id = storagePlayList.getId();
            List<Composition> compositions = storagePlayListsProvider.getCompositions(id);
            PlayList playList = toPlayList(storagePlayList, compositions);
            playListMap.put(id, new PlayListFullModel(playList, compositions));
        }
        return playListMap;
    }

    private PlayList toPlayList(StoragePlayList storagePlayList, List<Composition> compositions) {
        return new PlayList(storagePlayList.getId(),
                storagePlayList.getName(),
                storagePlayList.getDateAdded(),
                storagePlayList.getDateModified(),
                compositions.size(),
                getTotalDuration(compositions));
    }

    private class PlayListFullModel {

        private Disposable disposable;

        private final BehaviorSubject<List<Composition>> compositionsSubject;
        private final BehaviorSubject<PlayList> playListSubject;

        private PlayList playList;

        PlayListFullModel(PlayList playList, List<Composition> compositions) {
            this.playList = playList;
            compositionsSubject = BehaviorSubject.createDefault(compositions);
            playListSubject = BehaviorSubject.createDefault(playList);

            disposable = storagePlayListsProvider.getPlayListChangeObservable(playList.getId())
                    .subscribe(this::onPlayListCompositionsChanged);
        }

        PlayListFullModel(PlayList playList) {
            this.playList = playList;
            compositionsSubject = BehaviorSubject.create();
            playListSubject = BehaviorSubject.create();
        }

        Observable<List<Composition>> getCompositionsObservable() {
            return compositionsSubject;
        }

        Observable<PlayList> getPlayListObservable() {
            return playListSubject;
        }

        PlayList getPlayList() {
            return playList;
        }

        void updatePlayList(PlayList newPlayList) {
            playList = new PlayList(playList.getId(),
                    newPlayList.getName(),
                    newPlayList.getDateAdded(),
                    newPlayList.getDateModified(),
                    playList.getCompositionsCount(),
                    playList.getTotalDuration());
            playListSubject.onNext(playList);
        }

        void dispose() {
            disposable.dispose();
            compositionsSubject.onComplete();
            playListSubject.onComplete();
        }

        private void onPlayListCompositionsChanged(List<Composition> compositions) {
            playList = new PlayList(playList.getId(),
                    playList.getName(),
                    playList.getDateAdded(),
                    playList.getDateModified(),
                    compositions.size(),
                    getTotalDuration(compositions));

            playListSubject.onNext(playList);
            playListsSubject.onNext(playListMap);
            compositionsSubject.onNext(compositions);
        }
    }
}
