package com.github.anrimian.musicplayer.data.repositories.playlists;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

@Deprecated
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

}
