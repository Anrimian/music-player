package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface PlayListsRepository {

    Observable<List<PlayList>> getPlayListsObservable();

    Observable<PlayList> getPlayListObservable(long playlistId);

    Observable<List<PlayListItem>> getCompositionsObservable(long playlistId, @Nullable String searchText);

    Single<PlayList> createPlayList(String name);

    Completable addCompositionsToPlayList(List<Composition> compositions,
                                          PlayList playList,
                                          int position);

    Completable addCompositionsToPlayList(List<Composition> compositions,
                                          PlayList playList,
                                          boolean checkForDuplicates);

    Completable deleteItemFromPlayList(PlayListItem playListItem, long playListId);

    Completable restoreDeletedPlaylistItem();

    Completable deletePlayList(long playListId);

    Completable moveItemInPlayList(PlayList playList, int from, int to);

    Completable updatePlayListName(long playListId, String name);
}
