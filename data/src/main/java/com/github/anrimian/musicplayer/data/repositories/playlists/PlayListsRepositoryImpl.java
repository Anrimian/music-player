package com.github.anrimian.musicplayer.data.repositories.playlists;

import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

public class PlayListsRepositoryImpl implements PlayListsRepository {

    private final StoragePlayListsProvider storagePlayListsProvider;
    @Deprecated
    private final StorageMusicDataSource storageMusicDataSource;
    private final PlayListsDaoWrapper playListsDao;
    private final Scheduler scheduler;

    public PlayListsRepositoryImpl(StoragePlayListsProvider storagePlayListsProvider,
                                   StorageMusicDataSource storageMusicDataSource,
                                   PlayListsDaoWrapper playListsDao,
                                   Scheduler scheduler) {
        this.storagePlayListsProvider = storagePlayListsProvider;
        this.storageMusicDataSource = storageMusicDataSource;
        this.playListsDao = playListsDao;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<List<PlayList>> getPlayListsObservable() {
        return playListsDao.getPlayListsObservable();
    }

    @Override
    public Observable<PlayList> getPlayListObservable(long playlistId) {
        return playListsDao.getPlayListsObservable(playlistId);
    }

    @Override
    public Observable<List<PlayListItem>> getCompositionsObservable(long playlistId) {
        return playListsDao.getPlayListItemsObservable(playlistId);
    }

    @Override
    public Single<PlayList> createPlayList(String name) {
        return Single.fromCallable(() -> storagePlayListsProvider.createPlayList(name))
                .map(playListsDao::insertPlayList)
                .subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToPlayList(List<Composition> compositions,
                                                 PlayList playList,
                                                 int position) {
        return Completable.fromAction(() -> storagePlayListsProvider.addCompositionsToPlayList(
                compositions,
                playList.getId(),
                position)
        ).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToPlayList(List<Composition> compositions, PlayList playList) {
        return Completable.fromAction(() -> {
                    storagePlayListsProvider.addCompositionsToPlayList(
                            compositions,
                            playList.getId(),
                            playList.getCompositionsCount());

//                    playListsDao.insertPlayListItems();

                }
        ).subscribeOn(scheduler);
    }

    @Override
    public Completable deleteItemFromPlayList(long itemId, long playListId) {
        return Completable.fromAction(() ->
                storagePlayListsProvider.deleteItemFromPlayList(itemId, playListId)
        ).subscribeOn(scheduler);
    }

    @Override
    public Completable deletePlayList(long playListId) {
        return Completable.fromAction(() -> {
            Long storageId = playListsDao.selectStorageId(playListId);
            playListsDao.deletePlayList(playListId);
            if (storageId != null) {
                storagePlayListsProvider.deletePlayList(storageId);
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable moveItemInPlayList(long playListId, int from, int to) {
        return Completable.fromAction(() -> storagePlayListsProvider.moveItemInPlayList(
                playListId,
                from,
                to)
        ).subscribeOn(scheduler);
    }

    @Override
    public Completable updatePlayListName(long playListId, String name) {
        return Completable.fromAction(() -> {
                    playListsDao.updatePlayListName(playListId, name);
                    Long storageId = playListsDao.selectStorageId(playListId);
                    if (storageId != null) {
                        storagePlayListsProvider.updatePlayListName(storageId, name);
                    }
                }
        ).subscribeOn(scheduler);
    }
}
