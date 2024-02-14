package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.FileReference;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface PlayListsRepository {

    Observable<List<PlayList>> getPlayListsObservable(String searchQuery);

    Observable<PlayList> getPlayListObservable(long playlistId);

    Observable<List<PlayListItem>> getCompositionsObservable(long playlistId, @Nullable String searchText);

    Single<List<Long>> getCompositionIdsInPlaylists(Iterable<PlayList> playlists);

    Single<List<Composition>> getCompositionsInPlaylists(Iterable<PlayList> playlists);

    Single<List<Composition>> getCompositionsByPlaylistsIds(Iterable<Long> playlistsIds);

    Single<PlayList> createPlayList(String name);

    Single<List<Composition>> addCompositionsToPlayList(List<Composition> compositions,
                                                        PlayList playList,
                                                        boolean checkForDuplicates,
                                                        boolean ignoreDuplicates);

    Completable deleteItemFromPlayList(PlayListItem playListItem, long playListId);

    Completable restoreDeletedPlaylistItem();

    Completable deletePlayList(long playListId);

    Completable moveItemInPlayList(long playListId, int from, int to);

    Completable updatePlayListName(long playListId, String name);

    Completable exportPlaylistsToFolder(List<PlayList> playlists, FileReference folder);

    Single<Long> importPlaylistFile(FileReference file, boolean overwriteExisting);

    void updatePlaylistCache(long playlistId);
}
