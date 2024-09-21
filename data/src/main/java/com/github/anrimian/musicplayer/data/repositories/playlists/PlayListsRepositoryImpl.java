package com.github.anrimian.musicplayer.data.repositories.playlists;


import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.models.exceptions.NoCompositionsToInsertException;
import com.github.anrimian.musicplayer.data.models.exceptions.NoPlaylistItemsException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListAlreadyExistsException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlaylistNotCompletelyImportedException;
import com.github.anrimian.musicplayer.data.models.exceptions.TooManyPlayListItemsException;
import com.github.anrimian.musicplayer.data.models.folders.UriFileReference;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.PlaylistFilesStorage;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.M3UEditor;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListFile;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.data.utils.file.ContentProviderUtils;
import com.github.anrimian.musicplayer.domain.Constants;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.FileReference;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import kotlin.io.ByteStreamsKt;
import kotlin.io.ConstantsKt;

public class PlayListsRepositoryImpl implements PlayListsRepository {

    private final Context context;
    private final SettingsRepository settingsRepository;
    private final StoragePlayListsProvider storagePlayListsProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final PlayListsDaoWrapper playListsDao;
    private final PlaylistFilesStorage playlistFilesStorage;
    private final Scheduler dbScheduler;
    private final Scheduler slowBgScheduler;

    @Nullable
    private PlayListItem deletedItem;
    private long deletedItemPlayListId;
    private int deletedItemPosition;

    public PlayListsRepositoryImpl(Context context,
                                   SettingsRepository settingsRepository,
                                   StoragePlayListsProvider storagePlayListsProvider,
                                   CompositionsDaoWrapper compositionsDao,
                                   PlayListsDaoWrapper playListsDao,
                                   PlaylistFilesStorage playlistFilesStorage,
                                   Scheduler dbScheduler,
                                   Scheduler slowBgScheduler) {
        this.context = context;
        this.settingsRepository = settingsRepository;
        this.storagePlayListsProvider = storagePlayListsProvider;
        this.compositionsDao = compositionsDao;
        this.playListsDao = playListsDao;
        this.playlistFilesStorage = playlistFilesStorage;
        this.dbScheduler = dbScheduler;
        this.slowBgScheduler = slowBgScheduler;
    }

    @Override
    public Observable<List<PlayList>> getPlayListsObservable(String searchQuery) {
        return playListsDao.getPlayListsObservable(searchQuery);
    }

    @Override
    public Observable<PlayList> getPlayListObservable(long playlistId) {
        return playListsDao.getPlayListsObservable(playlistId);
    }

    @Override
    public Observable<List<PlayListItem>> getCompositionsObservable(long playlistId,
                                                                    @Nullable String searchText) {
        return settingsRepository.getDisplayFileNameObservable()
                .switchMap(useFileName ->
                        playListsDao.getPlayListItemsObservable(playlistId, useFileName, searchText));
    }

    @Override
    public Single<List<Long>> getCompositionIdsInPlaylists(Iterable<PlayList> playlists) {
        return Observable.fromIterable(playlists)
                .map(playlist -> playListsDao.getCompositionIdsInPlaylist(playlist.getId()))
                .<List<Long>>collect(ArrayList::new, List::addAll)
                .subscribeOn(dbScheduler);
    }

    @Override
    public Single<List<Composition>> getCompositionsInPlaylists(Iterable<PlayList> playlists) {
        return Observable.fromIterable(playlists)
                .map(playList -> playListsDao.getCompositionsInPlaylist(
                        playList.getId(),
                        settingsRepository.isDisplayFileNameEnabled())
                )
                .<List<Composition>>collect(ArrayList::new, List::addAll)
                .subscribeOn(dbScheduler);
    }

    @Override
    public Single<List<Composition>> getCompositionsByPlaylistsIds(Iterable<Long> playlistsIds) {
        return Observable.fromIterable(playlistsIds)
                .map(playlistId -> playListsDao.getCompositionsInPlaylist(
                        playlistId,
                        settingsRepository.isDisplayFileNameEnabled())
                )
                .<List<Composition>>collect(ArrayList::new, List::addAll)
                .subscribeOn(dbScheduler);
    }

    @Override
    public Single<PlayList> createPlayList(String name) {
        return Single.fromCallable(() -> {
            Date currentDate = new Date();
            long id = playListsDao.insertPlayList(
                    name,
                    currentDate,
                    currentDate,
                    () -> {
                        playlistFilesStorage.insertPlaylist(new PlayListFile(name, currentDate, currentDate, Collections.emptyList()));
                        return storagePlayListsProvider.createPlayList(name, currentDate, currentDate);
                    }
            );
            return new PlayList(id, name, currentDate, currentDate, 0, 0);
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Single<List<Composition>> addCompositionsToPlayList(List<Composition> compositions,
                                                               PlayList playList,
                                                               boolean checkForDuplicates,
                                                               boolean ignoreDuplicates) {
        int position = settingsRepository.isPlaylistInsertStartEnabled()? 0 : playList.getCompositionsCount();
        return addCompositionsToPlayList(
                compositions,
                playList.getId(),
                position,
                checkForDuplicates,
                ignoreDuplicates
        ).subscribeOn(dbScheduler);
    }

    @Override
    public Completable deleteItemFromPlayList(PlayListItem playListItem, long playListId) {
        return Completable.fromAction(() -> {
            int position = playListsDao.deletePlayListEntry(playListItem.getItemId(), playListId);
            updatePlaylistCache(playListId);
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
                asList(deletedItem),
                deletedItemPlayListId,
                deletedItemPosition,
                false,
                false)
                .ignoreElement();
    }

    @Override
    public Completable deletePlayList(long playListId) {
        return Completable.fromAction(() -> {
            Long storageId = playListsDao.selectStorageId(playListId);
            String name = playListsDao.selectPlayListName(playListId);
            playListsDao.deletePlayList(playListId);
            playlistFilesStorage.deletePlayList(name);
            if (storageId != null) {
                storagePlayListsProvider.deletePlayList(storageId);
            }
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable moveItemInPlayList(long playlistId, int from, int to) {
        return Completable.fromAction(() -> {
            playListsDao.moveItems(playlistId, from, to);
            updatePlaylistCache(playlistId);
            moveItemInStoragePlayList(playlistId, from, to);
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable updatePlayListName(long playListId, String name) {
        return Completable.fromAction(() -> {
            String oldName = playListsDao.selectPlayListName(playListId);
            playListsDao.updatePlayListName(playListId, name);
            playlistFilesStorage.renamePlaylist(oldName, name);
            Long storageId = playListsDao.selectStorageId(playListId);
            if (storageId != null) {
                storagePlayListsProvider.updatePlayListName(storageId, name);
            }
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Completable exportPlaylistsToFolder(List<PlayList> playlists, FileReference folder) {
        return Completable.fromAction(() -> {
            DocumentFile folderFile = DocumentFile.fromTreeUri(context,
                    ((UriFileReference) folder).getUri());
            if (folderFile == null) {
                throw new IllegalStateException("can't get folder reference");
            }
            for (PlayList playList: playlists) {
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
            long playlistId = playListsDao.findPlaylist(playlistName);
            if (playlistId != 0 && !overwriteExisting) {
                throw new PlayListAlreadyExistsException();
            }
            try(InputStream stream = context.getContentResolver().openInputStream(uri)) {
                PlayListFile playListFile = new M3UEditor().read(playlistName, stream);
                List<PlayListEntry> entries = playListFile.getEntries();
                if (entries.isEmpty()) {
                    throw new NoPlaylistItemsException();
                }
                List<Long> compositionIds = compositionsDao.getCompositionIds(entries, new HashMap<>());
                if (playlistId == 0) {
                    playlistId = playListsDao.insertPlayList(playListFile.getName(),
                            playListFile.getCreateDate(),
                            playListFile.getModifyDate(),
                            compositionIds
                    );
                } else {
                    playListsDao.setPlayListEntries(playlistId, compositionIds);
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
        AppPlayList playList = playListsDao.getPlayList(playlistId);
        List<PlayListEntry> entries = playListsDao.getPlayListItemsAsFileEntries(playlistId);
        PlayListFile playlistFile = new PlayListFile(playList.getName(),
                playList.getDateAdded(),
                playList.getDateModified(),
                entries);
        playlistFilesStorage.insertPlaylist(playlistFile);
    }

    private Single<List<Composition>> addCompositionsToPlayList(List<Composition> compositions,
                                                                long playListId,
                                                                int position,
                                                                boolean checkForDuplicates,
                                                                boolean ignoreDuplicates) {
        return Single.fromCallable(() ->{
                    if (compositions.isEmpty()) {
                        throw new NoCompositionsToInsertException();
                    }
                    if (playListsDao.getPlaylistSize(playListId) + compositions.size() > Constants.PLAY_LIST_MAX_ITEMS_COUNT) {
                        throw new TooManyPlayListItemsException();
                    }
                    List<Composition> addedCompositions = playListsDao.addCompositions(compositions,
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

    private void moveItemInStoragePlayList(long playlistId, int from, int to) {
        Completable.fromAction(() -> {
                    Long storageId = playListsDao.selectStorageId(playlistId);
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
