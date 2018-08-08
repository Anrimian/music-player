package com.github.anrimian.simplemusicplayer.data.storage.providers.playlists;

import com.github.anrimian.simplemusicplayer.data.models.StoragePlayList;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.utils.CompositionHelper.getTotalDuration;

public class StoragePlayListDataSource {

    private final StoragePlayListsProvider storagePlayListsProvider;

    private final PublishSubject<List<PlayList>> playListsSubject = PublishSubject.create();

    private List<PlayList> playLists;
    private List<PlayListChangeSubscription> playListSubscriptions = new ArrayList<>();

    public StoragePlayListDataSource(StoragePlayListsProvider storagePlayListsProvider) {
        this.storagePlayListsProvider = storagePlayListsProvider;
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return storagePlayListsProvider.getChangeObservable()
                .flatMapSingle(this::toPlayLists)
                .startWith(getStartPlayLists().toObservable())
                .doOnNext(this::subscribeOnPlayListsContentChanging)
                .mergeWith(playListsSubject);
    }

    public Observable<List<Composition>> getCompositionsObservable(long playlistId) {
        return storagePlayListsProvider.getPlayListChangeObservable(playlistId)
                .startWith(storagePlayListsProvider.getCompositions(playlistId));
    }

    public Completable createPlayList(String name) {
        return Completable.fromAction(() -> storagePlayListsProvider.createPlayList(name));
    }

    public Completable addCompositionInPlayList(Composition composition, PlayList playList) {
        return Completable.fromAction(() -> storagePlayListsProvider.addCompositionInPlayList(
                composition.getId(),
                playList.getId(),
                playList.getCompositionsCount())
        );
    }

    private Single<List<PlayList>> getStartPlayLists() {
        if (playLists == null) {
            return toPlayLists(storagePlayListsProvider.getPlayLists());
        } else {
            return Single.just(playLists);
        }
    }

    private Single<List<PlayList>> toPlayLists(List<StoragePlayList> playLists) {
        return Single.fromCallable(() -> playLists)
                .flatMapObservable(Observable::fromIterable)
                .map(this::toPlayList)
                .collect(ArrayList::new, List::add);
    }

    private PlayList toPlayList(StoragePlayList storagePlayList) {
        List<Composition> compositions = storagePlayListsProvider.getCompositions(
                storagePlayList.getId());

        return new PlayList(storagePlayList.getId(),
                storagePlayList.getName(),
                storagePlayList.getDateAdded(),
                storagePlayList.getDateModified(),
                compositions.size(),
                getTotalDuration(compositions));
    }

    private void subscribeOnPlayListsContentChanging(List<PlayList> playLists) {
        for (PlayListChangeSubscription subscription: playListSubscriptions) {
            subscription.clear();
        }
        playListSubscriptions.clear();

        this.playLists = playLists;
        for (PlayList playList: playLists) {
            playListSubscriptions.add(new PlayListChangeSubscription(playList));
        }
    }

    private class PlayListChangeSubscription {
        private final Disposable disposable;
        private PlayList playList;

        PlayListChangeSubscription(PlayList playList) {
            this.playList = playList;

            disposable = storagePlayListsProvider.getPlayListChangeObservable(playList.getId())
                    .subscribe(this::onPlayListCompositionsChanged);
        }

        private void clear() {
            disposable.dispose();
        }

        private void onPlayListCompositionsChanged(List<Composition> compositions) {
            for (int i = 0; i < playLists.size(); i++) {
                PlayList playList = playLists.get(i);
                if (this.playList.equals(playList)) {
                    this.playList = new PlayList(playList.getId(),
                            playList.getName(),
                            playList.getDateAdded(),
                            playList.getDateModified(),
                            compositions.size(),
                            getTotalDuration(compositions));
                    playLists.set(i, this.playList);
                    playListsSubject.onNext(playLists);
                    return;
                }
            }
        }
    }
}
