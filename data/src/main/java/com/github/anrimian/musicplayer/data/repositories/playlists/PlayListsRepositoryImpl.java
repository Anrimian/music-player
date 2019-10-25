package com.github.anrimian.musicplayer.data.repositories.playlists;

import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.playlist.RawPlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;

import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

public class PlayListsRepositoryImpl implements PlayListsRepository {

    private final StoragePlayListsProvider storagePlayListsProvider;
    private final PlayListsDaoWrapper playListsDao;
    private final Scheduler scheduler;

    public PlayListsRepositoryImpl(StoragePlayListsProvider storagePlayListsProvider,
                                   PlayListsDaoWrapper playListsDao,
                                   Scheduler scheduler) {
        this.storagePlayListsProvider = storagePlayListsProvider;
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
        return Single.fromCallable(() -> {
            Date currentDate = new Date();
            long id = playListsDao.insertPlayList(name, currentDate, currentDate);

            //don't modify storage playlists now
//            Long storageId = storagePlayListsProvider.createPlayList(name,
//                    currentDate,
//                    currentDate);
//            playListsDao.updateStorageId(id, storageId);
            return new PlayList(id,
                    null,
                    name,
                    currentDate,
                    currentDate,
                    0,
                    0);
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToPlayList(List<Composition> compositions,
                                                 PlayList playList,
                                                 int position) {
        return Completable.fromAction(() -> {
                    //don't modify storage playlists now


//                    Long playListId = playList.getStorageId();
//                    if (playListId != null) {
//                        storagePlayListsProvider.addCompositionsToPlayList(compositions,
//                                playListId,
//                                position);
//                    }
                    playListsDao.insertPlayListItems(
                            mapList(compositions, composition -> new RawPlayListItem(
                                    null,//composition.getStorageId(),//get inserted id in storage
                                    composition.getId()
                            )),
                            playList.getId(),
                            position);
                }
        ).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToPlayList(List<Composition> compositions, PlayList playList) {
        return addCompositionsToPlayList(compositions, playList, playList.getCompositionsCount())
                .subscribeOn(scheduler);
    }

    @Override
    public Completable deleteItemFromPlayList(PlayListItem playListItem, long playListId) {
        return Completable.fromAction(() -> {
            //don't modify storage playlists now

//            Long storageItemId = playListItem.getStorageItemId();
//            if (storageItemId != null) {
//                Long storagePlayListId = playListsDao.selectStorageId(playListId);
//                if (storagePlayListId != null) {
//                    storagePlayListsProvider.deleteItemFromPlayList(
//                            playListItem.getStorageItemId(),
//                            storagePlayListId
//                    );
//                }
//            }
            playListsDao.deletePlayListEntry(playListItem.getItemId(), playListId);
        }).subscribeOn(scheduler);
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
    public Completable moveItemInPlayList(PlayList playList, int from, int to) {
        return Completable.fromAction(() -> {

                    //don't modify storage playlists now

//            storagePlayListsProvider.moveItemInPlayList(
//                            playListId,
//                            from,
//                            to);
                    playListsDao.moveItems(playList.getId(), from, to);
                }
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
        }).subscribeOn(scheduler);
    }
}
