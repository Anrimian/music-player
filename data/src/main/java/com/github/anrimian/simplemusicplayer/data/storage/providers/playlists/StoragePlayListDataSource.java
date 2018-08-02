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

import static com.github.anrimian.simplemusicplayer.domain.models.utils.CompositionHelper.getTotalDuration;

public class StoragePlayListDataSource {

    private final StoragePlayListsProvider storagePlayListsProvider;

    private Map<Long, List<Composition>> playListMap = new HashMap<>();

    public StoragePlayListDataSource(StoragePlayListsProvider storagePlayListsProvider) {
        this.storagePlayListsProvider = storagePlayListsProvider;
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return storagePlayListsProvider.getChangeObservable()
                .flatMapSingle(this::getPlayLists)
                .startWith(getPlayLists(storagePlayListsProvider.getPlayLists()).toObservable());
    }

    public Observable<List<Composition>> getCompositionsObservable(long playlistId) {
        return Observable.never();
    }

    private Single<List<PlayList>> getPlayLists(List<StoragePlayList> playLists) {
        return Single.fromCallable(() -> playLists)
                .flatMapObservable(Observable::fromIterable)
                .map(this::getPlayList)
                .collect(ArrayList::new, List::add);
    }

    private PlayList getPlayList(StoragePlayList storagePlayList) {
        List<Composition> compositions = storagePlayListsProvider.getCompositions(
                storagePlayList.getId());

        return new PlayList(storagePlayList.getId(),
                storagePlayList.getName(),
                storagePlayList.getDateAdded(),
                storagePlayList.getDateModified(),
                compositions.size(),
                getTotalDuration(compositions));
    }
}
