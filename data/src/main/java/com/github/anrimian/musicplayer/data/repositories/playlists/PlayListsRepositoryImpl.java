package com.github.anrimian.musicplayer.data.repositories.playlists;


import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;

//TODO consider write playlists in audio file tags
public class PlayListsRepositoryImpl implements PlayListsRepository {

    private final StoragePlayListsProvider storagePlayListsProvider;
    private final PlayListsDaoWrapper playListsDao;
    private final Scheduler dbScheduler;
    private final Scheduler slowBgScheduler;

    @Nullable
    private PlayListItem deletedItem;
    private long deletedItemPlayListId;
    private int deletedItemPosition;

    public PlayListsRepositoryImpl(StoragePlayListsProvider storagePlayListsProvider,
                                   PlayListsDaoWrapper playListsDao,
                                   Scheduler dbScheduler,
                                   Scheduler slowBgScheduler) {
        this.storagePlayListsProvider = storagePlayListsProvider;
        this.playListsDao = playListsDao;
        this.dbScheduler = dbScheduler;
        this.slowBgScheduler = slowBgScheduler;
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
        return Single.fromCallable(() -> {
            Date currentDate = new Date();
            long id = playListsDao.insertPlayList(
                    name,
                    currentDate,
                    currentDate,
                    () -> storagePlayListsProvider.createPlayList(name, currentDate, currentDate)
            );
            return new PlayList(id, name, currentDate, currentDate, 0, 0);
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable addCompositionsToPlayList(List<Composition> compositions,
                                                 PlayList playList,
                                                 int position) {
        return addCompositionsToPlayList(compositions, playList.getId(), position);
    }

    @Override
    public Completable addCompositionsToPlayList(List<Composition> compositions,
                                                 PlayList playList,
                                                 boolean checkForDuplicates) {
        return addCompositionsToPlayList(compositions, playList, playList.getCompositionsCount())
                .subscribeOn(dbScheduler);
    }

    @Override
    public Completable deleteItemFromPlayList(PlayListItem playListItem, long playListId) {
        return Completable.fromAction(() -> {
            int position = playListsDao.deletePlayListEntry(playListItem.getItemId(), playListId);
            deleteItemFromStoragePlayList(playListItem, playListId);
            deletedItem = playListItem;
            deletedItemPlayListId = playListId;
            deletedItemPosition = position;
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable restoreDeletedPlaylistItem() {
        if (deletedItem == null) {
            return Completable.complete();
        }
        return addCompositionsToPlayList(
                asList(deletedItem.getComposition()),
                deletedItemPlayListId,
                deletedItemPosition);
    }

    @Override
    public Completable deletePlayList(long playListId) {
        return Completable.fromAction(() -> {
            Long storageId = playListsDao.selectStorageId(playListId);
            playListsDao.deletePlayList(playListId);
            if (storageId != null) {
                storagePlayListsProvider.deletePlayList(storageId);
            }
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable moveItemInPlayList(PlayList playList, int from, int to) {
        return Completable.fromAction(() -> {
            playListsDao.moveItems(playList.getId(), from, to);
            moveItemInStoragePlayList(playList, from, to);
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable updatePlayListName(long playListId, String name) {
        return Completable.fromAction(() -> {
            playListsDao.updatePlayListName(playListId, name);
            Long storageId = playListsDao.selectStorageId(playListId);
            if (storageId != null) {
                storagePlayListsProvider.updatePlayListName(storageId, name);
            }
        }).subscribeOn(dbScheduler);
    }

    //media store playlist methods are quite slow, run on separate thread
    private Completable addCompositionsToPlayList(List<Composition> compositions,
                                                  long playListId,
                                                  int position) {
        return Completable.fromAction(() ->
                playListsDao.addCompositions(compositions, playListId, position)
        ).subscribeOn(dbScheduler)
                .doOnComplete(() -> addCompositionsToStoragePlaylist(compositions, playListId, position));
    }

    private void addCompositionsToStoragePlaylist(List<Composition> compositions,
                                                  long playListId,
                                                  int position) {
        Completable.fromAction(() -> {
            Long storageId = playListsDao.selectStorageId(playListId);
            if (storageId != null) {
                storagePlayListsProvider.addCompositionsToPlayList(compositions,
                        storageId,
                        position);
            }
        }).onErrorComplete()
                .subscribeOn(slowBgScheduler)
                .subscribe();
    }

    private void moveItemInStoragePlayList(PlayList playList, int from, int to) {
        Completable.fromAction(() -> {
            Long storageId = playListsDao.selectStorageId(playList.getId());
            storagePlayListsProvider.moveItemInPlayList(storageId, from, to);
        }).onErrorComplete()
                .subscribeOn(slowBgScheduler)
                .subscribe();
    }

    private void deleteItemFromStoragePlayList(PlayListItem playListItem, long playListId) {
        Completable.fromAction(() -> {
            Long storagePlayListId = playListsDao.selectStorageId(playListId);
            Long storageItemId = playListsDao.selectStorageItemId(playListItem.getItemId());
            if (storageItemId != null && storagePlayListId != null) {
                storagePlayListsProvider.deleteItemFromPlayList(storageItemId, storagePlayListId);
            }
        }).onErrorComplete()
                .subscribeOn(slowBgScheduler)
                .subscribe();
    }
}
