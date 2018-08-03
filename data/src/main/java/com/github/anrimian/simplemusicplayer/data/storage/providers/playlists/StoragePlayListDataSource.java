package com.github.anrimian.simplemusicplayer.data.storage.providers.playlists;

import com.github.anrimian.simplemusicplayer.data.models.StoragePlayList;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.utils.CompositionHelper.getTotalDuration;

public class StoragePlayListDataSource {

    private final StoragePlayListsProvider storagePlayListsProvider;

    private PublishSubject<List<PlayList>> playListsSubject = PublishSubject.create();
    private List<PlayList> playLists;

    //TODO need clear
    private Map<Long, PlayListChangeSubscription> playListSubscriptions = new HashMap<>();

    public StoragePlayListDataSource(StoragePlayListsProvider storagePlayListsProvider) {
        this.storagePlayListsProvider = storagePlayListsProvider;
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return storagePlayListsProvider.getChangeObservable()
                .flatMapSingle(this::toPlayLists)
                .startWith(getStartPlayLists().toObservable())
                .doOnNext(playLists -> this.playLists = playLists)
                .mergeWith(playListsSubject);
    }

    public Observable<List<Composition>> getCompositionsObservable(long playlistId) {
        return Observable.never();
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

        long playListId = storagePlayList.getId();

        PlayList playList = new PlayList(storagePlayList.getId(),
                storagePlayList.getName(),
                storagePlayList.getDateAdded(),
                storagePlayList.getDateModified(),
                compositions.size(),
                getTotalDuration(compositions));

        PlayListChangeSubscription subscription = playListSubscriptions.get(playListId);
        if (subscription == null) {
            subscription = new PlayListChangeSubscription(playList);
            playListSubscriptions.put(playListId, subscription);
        } else {
            updatePlayList(playList);
            subscription.setPlayList(playList);
        }

        return playList;
    }

    private void updatePlayList(PlayList newPlayList) {
        for (int i = 0; i < playLists.size(); i++) {
            PlayList playList = playLists.get(i);
            if (newPlayList.equals(playList)) {
                playLists.set(i, newPlayList);
                return;
            }
        }
    }

    private class PlayListChangeSubscription {
        private final Disposable disposable;
        private PlayList playList;

        public PlayListChangeSubscription(PlayList playList) {
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

        public void setPlayList(PlayList playList) {
            this.playList = playList;
        }
    }
}
