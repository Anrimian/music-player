package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public class LibraryFilesInteractor {

    private final MusicProviderRepository musicProviderRepository;
    private final EditorRepository editorRepository;
    private final MusicPlayerInteractor musicPlayerInteractor;
    private final PlayListsRepository playListsRepository;
    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;

    public LibraryFilesInteractor(MusicProviderRepository musicProviderRepository,
                                  EditorRepository editorRepository,
                                  MusicPlayerInteractor musicPlayerInteractor,
                                  PlayListsRepository playListsRepository,
                                  SettingsRepository settingsRepository,
                                  UiStateRepository uiStateRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.editorRepository = editorRepository;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.playListsRepository = playListsRepository;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
    }

    public Single<Folder> getCompositionsInPath(@Nullable String path, @Nullable String searchText) {
        return musicProviderRepository.getCompositionsInPath(path, searchText);
    }

    public void playAllMusicInPath(@Nullable String path) {
        musicProviderRepository.getAllCompositionsInPath(path)
                .doOnSuccess(musicPlayerInteractor::startPlaying)
                .subscribe();
    }

    public Single<List<Composition>> getAllCompositionsInPath(@Nullable String path) {
        return musicProviderRepository.getAllCompositionsInPath(path);
    }

    public Single<List<Composition>> getAllCompositionsInFileSources(List<FileSource> fileSources) {
        return musicProviderRepository.getAllCompositionsInFolders(fileSources);
    }

    public void play(List<FileSource> fileSources) {
        musicProviderRepository.getAllCompositionsInFolders(fileSources)
                .doOnSuccess(musicPlayerInteractor::startPlaying)
                .subscribe();
    }

    public void play(String path, Composition composition) {
        musicProviderRepository.getAllCompositionsInPath(path)
                .doOnSuccess(compositions -> {
                    int firstPosition = compositions.indexOf(composition);
                    musicPlayerInteractor.startPlaying(compositions, firstPosition);
                })
                .subscribe();
    }

    public void addCompositionsToPlayNext(List<FileSource> fileSources) {
        musicProviderRepository.getAllCompositionsInFolders(fileSources)
                .flatMapCompletable(musicPlayerInteractor::addCompositionsToPlayNext)
                .subscribe();
    }

    public void addCompositionsToEnd(List<FileSource> fileSources) {
        musicProviderRepository.getAllCompositionsInFolders(fileSources)
                .flatMapCompletable(musicPlayerInteractor::addCompositionsToEnd)
                .subscribe();
    }

    public Single<List<Composition>> deleteCompositions(List<FileSource> fileSources) {
        return musicProviderRepository.getAllCompositionsInFolders(fileSources)
                .flatMap(compositions -> musicProviderRepository.deleteCompositions(compositions)
                        .toSingleDefault(compositions));
    }

    public Single<List<Composition>> deleteFolder(FolderFileSource folder) {
        return musicProviderRepository.getAllCompositionsInPath(folder.getFullPath())
                .flatMap(compositions -> musicProviderRepository.deleteCompositions(compositions)
                        .toSingleDefault(compositions));
    }

    public Single<List<Composition>> addCompositionsToPlayList(String path, PlayList playList) {
        return musicProviderRepository.getAllCompositionsInPath(path)
                .flatMap(compositions -> playListsRepository.addCompositionsToPlayList(compositions, playList)
                        .toSingleDefault(compositions));
    }

    public Single<List<Composition>> addCompositionsToPlayList(List<FileSource> fileSources, PlayList playList) {
        return musicProviderRepository.getAllCompositionsInFolders(fileSources)
                .flatMap(compositions -> playListsRepository.addCompositionsToPlayList(compositions, playList)
                        .toSingleDefault(compositions));
    }

    public void setFolderOrder(Order order) {
        settingsRepository.setFolderOrder(order);
    }

    public Order getFolderOrder() {
        return settingsRepository.getFolderOrder();
    }

    public void saveCurrentPath(@Nullable String path) {
        uiStateRepository.setSelectedFolderScreen(path);
    }

    public Single<List<String>> getCurrentFolderScreens() {
        String currentPath = uiStateRepository.getSelectedFolderScreen();
        return musicProviderRepository.getAvailablePathsForPath(currentPath);
    }

    public Completable renameFolder(String folderPath, String newName) {
        return editorRepository.changeFolderName(folderPath, newName)
                .flatMapCompletable(newPath ->
                        musicProviderRepository.changeFolderName(folderPath, newPath)
                                .flatMapCompletable(editorRepository::changeCompositionsFilePath)
                );
    }

}
