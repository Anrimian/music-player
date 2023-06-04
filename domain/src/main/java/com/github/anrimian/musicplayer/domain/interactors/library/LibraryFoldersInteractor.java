package com.github.anrimian.musicplayer.domain.interactors.library;

import com.github.anrimian.filesync.SyncInteractor;
import com.github.anrimian.musicplayer.domain.Constants;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition;
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.sync.FileKey;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelperKt;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * Created on 24.10.2017.
 */

public class LibraryFoldersInteractor {

    private final LibraryRepository libraryRepository;
    private final EditorRepository editorRepository;
    private final LibraryPlayerInteractor musicPlayerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final SyncInteractor<FileKey, ?, Long> syncInteractor;
    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;
    private final MediaScannerRepository mediaScannerRepository;

    public LibraryFoldersInteractor(LibraryRepository libraryRepository,
                                    EditorRepository editorRepository,
                                    LibraryPlayerInteractor musicPlayerInteractor,
                                    PlayListsInteractor playListsInteractor,
                                    SyncInteractor<FileKey, ?, Long> syncInteractor,
                                    SettingsRepository settingsRepository,
                                    UiStateRepository uiStateRepository,
                                    MediaScannerRepository mediaScannerRepository) {
        this.libraryRepository = libraryRepository;
        this.editorRepository = editorRepository;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.playListsInteractor = playListsInteractor;
        this.syncInteractor = syncInteractor;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
        this.mediaScannerRepository = mediaScannerRepository;
    }

    public Observable<List<FileSource>> getFoldersInFolder(@Nullable Long folderId,
                                                           @Nullable String searchQuery) {
        return libraryRepository.getFoldersInFolder(folderId, searchQuery);
    }

    public Observable<FolderFileSource> getFolderObservable(long folderId) {
        return libraryRepository.getFolderObservable(folderId);
    }

    public void playAllMusicInFolder(@Nullable Long folderId) {
        libraryRepository.getAllCompositionsInFolder(folderId)
                .doOnSuccess(musicPlayerInteractor::startPlayingCompositions)
                .subscribe();
    }

    public Single<List<Composition>> getAllCompositionsInFolder(@Nullable Long folderId) {
        return libraryRepository.getAllCompositionsInFolder(folderId);
    }

    public Single<List<Composition>> getAllCompositionsInFileSources(List<FileSource> fileSources) {
        return libraryRepository.getAllCompositionsInFolders(fileSources);
    }

    public void play(List<FileSource> fileSources, int position) {
        Composition composition = null;
        if (position != Constants.NO_POSITION) {
            FileSource source = fileSources.get(position);
            if (source instanceof CompositionFileSource) {
                composition = ((CompositionFileSource) source).getComposition();
            }
        }
        play(fileSources, composition);
    }

    public void play(Long folderId, long compositionId) {
        libraryRepository.getAllCompositionsInFolder(folderId)
                .doOnSuccess(compositions -> {
                    int firstPosition = ListUtils.findPosition(
                            compositions,
                            composition -> composition.getId() == compositionId);
                    musicPlayerInteractor.startPlayingCompositions(compositions, firstPosition);
                })
                .subscribe();
    }

    public Single<List<Composition>> addCompositionsToPlayNext(List<FileSource> fileSources) {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
                .flatMap(musicPlayerInteractor::addCompositionsToPlayNext);
    }

    public Single<List<Composition>> addCompositionsToEnd(List<FileSource> fileSources) {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
                .flatMap(musicPlayerInteractor::addCompositionsToEnd);
    }

    public Single<List<DeletedComposition>> deleteFiles(List<FileSource> fileSources) {
        return libraryRepository.deleteFolders(fileSources)
                .doOnSuccess(this::onCompositionsDeleted);
    }

    public Single<List<DeletedComposition>> deleteFolder(FolderFileSource folder) {
        return libraryRepository.deleteFolder(folder)
                .doOnSuccess(this::onCompositionsDeleted);
    }

    public Single<List<Composition>> addCompositionsToPlayList(Long folderId, PlayList playList) {
        return libraryRepository.getAllCompositionsInFolder(folderId)
                .flatMap(compositions -> playListsInteractor.addCompositionsToPlayList(compositions, playList)
                        .toSingleDefault(compositions));
    }

    public Single<List<Composition>> addCompositionsToPlayList(List<FileSource> fileSources, PlayList playList) {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
                .flatMap(compositions -> playListsInteractor.addCompositionsToPlayList(compositions, playList)
                        .toSingleDefault(compositions));
    }

    public void setFolderOrder(Order order) {
        settingsRepository.setFolderOrder(order);
    }

    public Order getFolderOrder() {
        return settingsRepository.getFolderOrder();
    }

    public void saveCurrentFolder(@Nullable Long folderId) {
        uiStateRepository.setSelectedFolderScreen(folderId);
    }

    public Single<List<Long>> getCurrentFolderScreens() {
        Long currentFolder = uiStateRepository.getSelectedFolderScreen();
        return libraryRepository.getAllParentFolders(currentFolder);
    }

    public Single<List<Long>> getParentFolders(Long compositionId) {
        return libraryRepository.getAllParentFoldersForComposition(compositionId);
    }

    public Completable renameFolder(long folderId, String newName) {
        return editorRepository.changeFolderName(folderId, newName);
    }

    public Completable addFolderToIgnore(IgnoredFolder folder) {
        return libraryRepository.addFolderToIgnore(folder)
                .andThen(mediaScannerRepository.runStorageScanner());
    }

    public Single<IgnoredFolder> addFolderToIgnore(FolderFileSource folder) {
        return libraryRepository.addFolderToIgnore(folder)
                .flatMap(ignoredFolder -> mediaScannerRepository.runStorageScanner()
                        .toSingleDefault(ignoredFolder)
                );
    }

    public Observable<List<IgnoredFolder>> getIgnoredFoldersObservable() {
        return libraryRepository.getIgnoredFoldersObservable();
    }

    public Completable deleteIgnoredFolder(IgnoredFolder folder) {
        return libraryRepository.deleteIgnoredFolder(folder)
                .andThen(mediaScannerRepository.runStorageScanner());
    }

    private void play(List<FileSource> fileSources, @Nullable Composition composition) {
        libraryRepository.getAllCompositionsInFolders(fileSources)
                .doOnSuccess(compositions -> {
                    //in folders we can have duplicates in list in search mode,
                    // so compare by references too
                    int firstPosition = ListUtils.findPosition(compositions,
                            c -> c.equals(composition) && c == composition);
                    musicPlayerInteractor.startPlayingCompositions(compositions, firstPosition);
                })
                .subscribe();
    }

    private void onCompositionsDeleted(List<DeletedComposition> compositions) {
        syncInteractor.onLocalFilesDeleted(CompositionHelperKt.toFileKeys(compositions));
    }

}
