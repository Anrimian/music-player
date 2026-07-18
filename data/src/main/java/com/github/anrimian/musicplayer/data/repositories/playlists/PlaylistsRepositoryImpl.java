package com.github.anrimian.musicplayer.data.repositories.playlists;


import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.playlist.PlaylistsDaoWrapper;
import com.github.anrimian.musicplayer.data.models.exceptions.NoCompositionsToInsertException;
import com.github.anrimian.musicplayer.data.models.exceptions.NoPlaylistItemsException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlaylistAlreadyExistsException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlaylistNotCompletelyImportedException;
import com.github.anrimian.musicplayer.data.models.exceptions.TooManyPlayListItemsException;
import com.github.anrimian.musicplayer.data.models.folders.UriFileReference;
import com.github.anrimian.musicplayer.data.repositories.playlists.models.PlaylistEntryPosition;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.PlaylistFilesStorage;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.M3UEditor;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListFile;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlaylist;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylistsProvider;
import com.github.anrimian.musicplayer.data.utils.file.ContentProviderUtils;
import com.github.anrimian.musicplayer.domain.Constants;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel;
import com.github.anrimian.musicplayer.domain.models.folders.FileReference;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist;
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry;
import com.github.anrimian.musicplayer.domain.repositories.PlaylistsRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import kotlin.io.ByteStreamsKt;
import kotlin.io.ConstantsKt;

public class PlaylistsRepositoryImpl implements PlaylistsRepository {

    private final Context context;
    private final SettingsRepository settingsRepository;
    private final StoragePlaylistsProvider storagePlaylistsProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final PlaylistsDaoWrapper playlistsDao;
    private final PlaylistFilesStorage playlistFilesStorage;
    private final Scheduler ioScheduler;
    private final Scheduler dbScheduler;
    private final Scheduler slowBgScheduler;

    @Nullable
    private PlaylistEntry deletedItem;
    private long deletedItemPlayListId;
    private int deletedItemPosition;

    @Nullable
    private List<PlaylistEntryPosition> previousSortPositions;
    private long previousSortPlaylistId;

    public PlaylistsRepositoryImpl(Context context,
                                   SettingsRepository settingsRepository,
                                   StoragePlaylistsProvider storagePlayListsProvider,
                                   CompositionsDaoWrapper compositionsDao,
                                   PlaylistsDaoWrapper playlistsDao,
                                   PlaylistFilesStorage playlistFilesStorage,
                                   Scheduler ioScheduler,
                                   Scheduler dbScheduler,
                                   Scheduler slowBgScheduler) {
        this.context = context;
        this.settingsRepository = settingsRepository;
        this.storagePlaylistsProvider = storagePlayListsProvider;
        this.compositionsDao = compositionsDao;
        this.playlistsDao = playlistsDao;
        this.playlistFilesStorage = playlistFilesStorage;
        this.ioScheduler = ioScheduler;
        this.dbScheduler = dbScheduler;
        this.slowBgScheduler = slowBgScheduler;
    }

    @Override
    public Observable<List<Playlist>> getPlaylistsObservable(String searchQuery) {
        return playlistsDao.getPlayListsObservable(searchQuery);
    }

    @Override
    public Observable<Playlist> getPlaylistObservable(long playlistId) {
        return playlistsDao.getPlayListsObservable(playlistId);
    }

    @Override
    public Observable<List<PlaylistEntry>> getCompositionsObservable(long playlistId,
                                                                     @Nullable String searchText) {
        return settingsRepository.getDisplayFileNameObservable()
                .switchMap(useFileName ->
                        playlistsDao.getPlayListItemsObservable(playlistId, useFileName, searchText));
    }

    @Override
    public Single<List<Long>> getCompositionIdsInPlaylists(Iterable<Playlist> playlists) {
        return Observable.fromIterable(playlists)
                .map(playlist -> playlistsDao.getCompositionIdsInPlaylist(playlist.getId()))
                .<List<Long>>collect(ArrayList::new, List::addAll)
                .subscribeOn(dbScheduler);
    }

    @Override
    public Single<List<Composition>> getCompositionsInPlaylists(Iterable<Playlist> playlists) {
        return Observable.fromIterable(playlists)
                .map(playList -> playlistsDao.getCompositionsInPlaylist(
                        playList.getId(),
                        settingsRepository.isDisplayFileNameEnabled())
                )
                .<List<Composition>>collect(ArrayList::new, List::addAll)
                .subscribeOn(dbScheduler);
    }

    @Override
    public Single<List<Composition>> getCompositionsByPlaylistsIds(Iterable<Long> playlistsIds) {
        return Observable.fromIterable(playlistsIds)
                .map(playlistId -> playlistsDao.getCompositionsInPlaylist(
                        playlistId,
                        settingsRepository.isDisplayFileNameEnabled())
                )
                .<List<Composition>>collect(ArrayList::new, List::addAll)
                .subscribeOn(dbScheduler);
    }

    @Override
    public Single<Playlist> createPlaylist(String name) {
        return Single.fromCallable(() -> {
            long currentTime = System.currentTimeMillis();
            long id = playlistsDao.insertPlaylist(
                    name,
                    currentTime,
                    currentTime,
                    () -> {
                        playlistFilesStorage.insertPlaylist(new PlayListFile(name, currentTime, currentTime, Collections.emptyList()));
                        return storagePlaylistsProvider.createPlayList(name, currentTime, currentTime);
                    }
            );
            return new Playlist(id, name, currentTime, currentTime, 0, 0);
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Single<List<CompositionModel>> addCompositionsToPlaylist(List<CompositionModel> compositions,
                                                                    Playlist playList,
                                                                    boolean checkForDuplicates,
                                                                    boolean ignoreDuplicates) {
        return Single.fromCallable(() ->
            settingsRepository.isPlaylistInsertStartEnabled() ? 0 : playlistsDao.getNextOrderPosition(playList.getId())
        )
                .flatMap(pos ->
                        addCompositionsToPlayList(
                                compositions,
                                playList.getId(),
                                pos,
                                checkForDuplicates,
                                ignoreDuplicates
                        )
                )
                .subscribeOn(dbScheduler);
    }

    @Override
    public Completable deleteItemFromPlaylist(PlaylistEntry playListEntry, long playListId) {
        return Completable.fromAction(() -> {
            int position = playlistsDao.deletePlayListEntry(playListEntry.getEntryId(), playListId);
            updatePlaylistCache(playListId);
            deleteItemFromStoragePlayList(playListEntry, playListId);
            deletedItem = playListEntry;
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
                asList(deletedItem),
                deletedItemPlayListId,
                deletedItemPosition,
                false,
                false)
                .ignoreElement();
    }

    @Override
    public Completable deletePlaylist(long playListId) {
        return Completable.fromAction(() -> {
            Long storageId = playlistsDao.selectStorageId(playListId);
            String name = playlistsDao.selectPlayListName(playListId);
            if (name == null) {
                return;
            }
            playlistFilesStorage.deletePlayList(name);
            playlistsDao.deletePlayList(playListId);
            playlistFilesStorage.deletePlayList(name);
            if (storageId != null) {
                storagePlaylistsProvider.deletePlayList(storageId);
            }
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable moveItemInPlaylist(long playlistId, int from, int to) {
        return Completable.fromAction(() -> {
            synchronized(this) {
                playlistsDao.moveItems(playlistId, from, to);
                updatePlaylistCache(playlistId);
                moveItemInStoragePlayList(playlistId, from, to);
            }
        }).subscribeOn(ioScheduler);
    }

    @Override
    public Completable updatePlaylistName(long playListId, String name) {
        return Completable.fromAction(() -> {
            String oldName = playlistsDao.selectPlayListName(playListId);
            playlistsDao.updatePlayListName(playListId, name);
            playlistFilesStorage.renamePlaylist(oldName, name);
            Long storageId = playlistsDao.selectStorageId(playListId);
            if (storageId != null) {
                storagePlaylistsProvider.updatePlayListName(storageId, name);
            }
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable exportPlaylistsToFolder(List<Playlist> playlists, FileReference folder) {
        return Completable.fromAction(() -> {
            DocumentFile folderFile = DocumentFile.fromTreeUri(context,
                    ((UriFileReference) folder).getUri());
            if (folderFile == null) {
                throw new IllegalStateException("can't get folder reference");
            }
            for (Playlist playList: playlists) {
                DocumentFile file = folderFile.createFile("audio/m3u",
                        playList.getName() + ".m3u");
                if (file == null) {
                    throw new IllegalStateException("can't get file reference");
                }
                File playlistFile = playlistFilesStorage.getPlaylistFile(playList.getName());
                try(OutputStream stream = context.getContentResolver().openOutputStream(file.getUri());
                    InputStream inputStream = new FileInputStream(playlistFile)) {
                    ByteStreamsKt.copyTo(inputStream, stream, ConstantsKt.DEFAULT_BUFFER_SIZE);
                }
            }
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Single<Long> importPlaylistFile(FileReference file, boolean overwriteExisting) {
        return Single.fromCallable(() -> {
            Uri uri = ((UriFileReference) file).getUri();
            String fileName = ContentProviderUtils.getFileName(context, uri);
            String playlistName = FileUtils.formatFileName(fileName);
            long playlistId = playlistsDao.findPlaylist(playlistName);
            if (playlistId != 0 && !overwriteExisting) {
                throw new PlaylistAlreadyExistsException();
            }
            try(InputStream stream = context.getContentResolver().openInputStream(uri)) {
                PlayListFile playListFile = new M3UEditor().read(playlistName, stream);
                List<PlayListEntry> entries = playListFile.getEntries();
                if (entries.isEmpty()) {
                    throw new NoPlaylistItemsException();
                }
                List<Long> compositionIds = compositionsDao.getCompositionIds(entries, new HashMap<>());
                if (playlistId == 0) {
                    playlistId = playlistsDao.insertPlaylist(playListFile.getName(),
                            playListFile.getCreateDate(),
                            playListFile.getModifyDate(),
                            compositionIds
                    );
                } else {
                    playlistsDao.setPlayListEntries(playlistId, compositionIds);
                }
                playlistFilesStorage.insertPlaylist(playListFile);

                int notFoundFilesCount = entries.size() - compositionIds.size();
                if (notFoundFilesCount > 0) {
                    throw new PlaylistNotCompletelyImportedException(playlistId, notFoundFilesCount);
                }
                return playlistId;
            }
        }).subscribeOn(dbScheduler);
    }

    @Override
    public void updatePlaylistCache(long playlistId) {
        AppPlaylist playList = playlistsDao.getPlayList(playlistId);
        List<PlayListEntry> entries = playlistsDao.getPlayListItemsAsFileEntries(playlistId);
        PlayListFile playlistFile = new PlayListFile(playList.getName(),
                playList.getAddedTime(),
                playList.getModifiedTime(),
                entries);
        playlistFilesStorage.insertPlaylist(playlistFile);
    }

    @Override
    public Completable sortPlaylistEntries(long playlistId, Order order, boolean useFileName) {
        return Completable.fromAction(() -> {
            synchronized (this) {
                previousSortPositions = playlistsDao.getEntryPositions(playlistId);
                previousSortPlaylistId = playlistId;

                playlistsDao.sortEntries(playlistId, order, useFileName);

                updatePlaylistCache(playlistId);
            }
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable undoSortPlaylistEntries() {
        return Completable.fromAction(() -> {
            synchronized (this) {
                if (previousSortPositions == null) {
                    return;
                }
                for (PlaylistEntryPosition pos : previousSortPositions) {
                    playlistsDao.updateEntryPosition(pos.getItemId(), pos.getOrderPosition());
                }
                updatePlaylistCache(previousSortPlaylistId);
                previousSortPositions = null;
            }
        }).subscribeOn(dbScheduler);
    }

    private Single<List<CompositionModel>> addCompositionsToPlayList(List<CompositionModel> compositions,
                                                                     long playListId,
                                                                     int position,
                                                                     boolean checkForDuplicates,
                                                                     boolean ignoreDuplicates) {
        return Single.fromCallable(() ->{
                    if (compositions.isEmpty()) {
                        throw new NoCompositionsToInsertException();
                    }
                    if (playlistsDao.getPlaylistSize(playListId) + compositions.size() > Constants.PLAY_LIST_MAX_ITEMS_COUNT) {
                        throw new TooManyPlayListItemsException();
                    }
                    List<CompositionModel> addedCompositions = playlistsDao.addCompositions(compositions,
                            playListId,
                            position,
                            checkForDuplicates,
                            ignoreDuplicates);
                    updatePlaylistCache(playListId);
                    return addedCompositions;
                }).subscribeOn(dbScheduler)
                .doOnSuccess(c -> addCompositionsToStoragePlaylist(c, playListId, position));
    }

    //media store playlist methods are quite slow, run on separate thread
    private void addCompositionsToStoragePlaylist(List<CompositionModel> compositions,
                                                  long playListId,
                                                  int position) {
        Completable.fromAction(() -> {
                    Long storageId = playlistsDao.selectStorageId(playListId);
                    if (storageId != null) {
                        storagePlaylistsProvider.addCompositionsToPlayList(compositions,
                                storageId,
                                position);
                    }
                }).onErrorComplete()
                .subscribeOn(slowBgScheduler)
                .subscribe();
    }

    private void moveItemInStoragePlayList(long playlistId, int from, int to) {
        Completable.fromAction(() -> {
                    Long storageId = playlistsDao.selectStorageId(playlistId);
                    storagePlaylistsProvider.moveItemInPlayList(storageId, from, to);
                }).onErrorComplete()
                .subscribeOn(slowBgScheduler)
                .subscribe();
    }

    private void deleteItemFromStoragePlayList(PlaylistEntry playListEntry, long playListId) {
        Completable.fromAction(() -> {
                    Long storagePlayListId = playlistsDao.selectStorageId(playListId);
                    Long storageItemId = playlistsDao.selectStorageItemId(playListEntry.getEntryId());
                    if (storageItemId != null && storagePlayListId != null) {
                        storagePlaylistsProvider.deleteItemFromPlayList(storageItemId, storagePlayListId);
                    }
                }).onErrorComplete()
                .subscribeOn(slowBgScheduler)
                .subscribe();
    }

}
