package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel;
import com.github.anrimian.musicplayer.domain.models.folders.FileReference;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist;
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface PlaylistsRepository {

    Observable<List<Playlist>> getPlaylistsObservable(String searchQuery);

    Observable<Playlist> getPlaylistObservable(long playlistId);

    Observable<List<PlaylistEntry>> getCompositionsObservable(long playlistId, @Nullable String searchText);

    Single<List<Long>> getCompositionIdsInPlaylists(Iterable<Playlist> playlists);

    Single<List<Composition>> getCompositionsInPlaylists(Iterable<Playlist> playlists);

    Single<List<Composition>> getCompositionsByPlaylistsIds(Iterable<Long> playlistsIds);

    Single<Playlist> createPlaylist(String name);

    Single<List<CompositionModel>> addCompositionsToPlaylist(List<CompositionModel> compositions,
                                                             Playlist playList,
                                                             boolean checkForDuplicates,
                                                             boolean ignoreDuplicates);

    Completable deleteItemFromPlaylist(PlaylistEntry playListEntry, long playListId);

    Completable restoreDeletedPlaylistItem();

    Completable deletePlaylist(long playListId);

    Completable moveItemInPlaylist(long playListId, int from, int to);

    Completable updatePlaylistName(long playListId, String name);

    Completable exportPlaylistsToFolder(List<Playlist> playlists, FileReference folder);

    Single<Long> importPlaylistFile(FileReference file, boolean overwriteExisting);

    void updatePlaylistCache(long playlistId);

    Completable sortPlaylistEntries(long playlistId, Order order, boolean useFileName);

    Completable undoSortPlaylistEntries();
}
