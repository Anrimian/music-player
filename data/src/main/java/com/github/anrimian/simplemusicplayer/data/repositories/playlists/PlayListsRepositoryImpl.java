package com.github.anrimian.simplemusicplayer.data.repositories.playlists;

import com.github.anrimian.simplemusicplayer.data.storage.providers.playlists.StoragePlayListDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class PlayListsRepositoryImpl implements PlayListsRepository {

    private final StoragePlayListDataSource storagePlayListDataSource;
    private final Scheduler scheduler;

    public PlayListsRepositoryImpl(StoragePlayListDataSource storagePlayListDataSource,
                                   Scheduler scheduler) {
        this.storagePlayListDataSource = storagePlayListDataSource;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<List<PlayList>> getPlayListsObservable() {
        return storagePlayListDataSource.getPlayListsObservable()
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<List<Composition>> getCompositionsObservable(long playlistId) {
        return storagePlayListDataSource.getCompositionsObservable(playlistId)
                .subscribeOn(scheduler);
    }

    @Override
    public Completable createPlayList(String name) {
        return storagePlayListDataSource.createPlayList(name)
                .subscribeOn(scheduler);
    }
}
