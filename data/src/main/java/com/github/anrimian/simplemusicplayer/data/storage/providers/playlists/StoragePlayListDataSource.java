package com.github.anrimian.simplemusicplayer.data.storage.providers.playlists;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;

import java.util.List;

import io.reactivex.Observable;

public class StoragePlayListDataSource {

    private final StoragePlayListsProvider storagePlayListsProvider;

    public StoragePlayListDataSource(StoragePlayListsProvider storagePlayListsProvider) {
        this.storagePlayListsProvider = storagePlayListsProvider;
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return Observable.never();
    }

    public Observable<List<Composition>> getCompositionsObservable(int playlistId) {
        return Observable.never();
    }
}
