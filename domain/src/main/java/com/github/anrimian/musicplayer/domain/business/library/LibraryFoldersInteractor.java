package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created on 24.10.2017.
 */

public class LibraryFoldersInteractor {

    private final LibraryRepository libraryRepository;
    private final EditorRepository editorRepository;
    private final MusicPlayerInteractor musicPlayerInteractor;
    private final PlayListsRepository playListsRepository;
    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;
    private final MediaScannerRepository mediaScannerRepository;

    private final BehaviorSubject<Boolean> moveModeSubject = BehaviorSubject.createDefault(false);
    private final LinkedHashSet<FileSource> filesToCopy = new LinkedHashSet<>();
    private final LinkedHashSet<FileSource> filesToMove = new LinkedHashSet<>();
    private String fromMovePath;

    public LibraryFoldersInteractor(LibraryRepository libraryRepository,
                                    EditorRepository editorRepository,
                                    MusicPlayerInteractor musicPlayerInteractor,
                                    PlayListsRepository playListsRepository,
                                    SettingsRepository settingsRepository,
                                    UiStateRepository uiStateRepository,
                                    MediaScannerRepository mediaScannerRepository) {
        this.libraryRepository = libraryRepository;
        this.editorRepository = editorRepository;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.playListsRepository = playListsRepository;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
        this.mediaScannerRepository = mediaScannerRepository;
    }

    public Single<Folder> getCompositionsInPath(@Nullable String path, @Nullable String searchText) {
        return libraryRepository.getCompositionsInPath(path, searchText);
    }

    public void playAllMusicInPath(@Nullable String path) {
        libraryRepository.getAllCompositionsInPath(path)
                .doOnSuccess(musicPlayerInteractor::startPlaying)
                .subscribe();
    }

    public Single<List<Composition>> getAllCompositionsInPath(@Nullable String path) {
        return libraryRepository.getAllCompositionsInPath(path);
    }

    public Single<List<Composition>> getAllCompositionsInFileSources(List<FileSource> fileSources) {
        return libraryRepository.getAllCompositionsInFolders(fileSources);
    }

    public void play(List<FileSource> fileSources) {
        libraryRepository.getAllCompositionsInFolders(fileSources)
                .doOnSuccess(musicPlayerInteractor::startPlaying)
                .subscribe();
    }

    public void play(String path, Composition composition) {
        libraryRepository.getAllCompositionsInPath(path)
                .doOnSuccess(compositions -> {
                    int firstPosition = compositions.indexOf(composition);
                    musicPlayerInteractor.startPlaying(compositions, firstPosition);
                })
                .subscribe();
    }

    public void addCompositionsToPlayNext(List<FileSource> fileSources) {
        libraryRepository.getAllCompositionsInFolders(fileSources)
                .flatMapCompletable(musicPlayerInteractor::addCompositionsToPlayNext)
                .subscribe();
    }

    public void addCompositionsToEnd(List<FileSource> fileSources) {
        libraryRepository.getAllCompositionsInFolders(fileSources)
                .flatMapCompletable(musicPlayerInteractor::addCompositionsToEnd)
                .subscribe();
    }

    public Single<List<Composition>> deleteCompositions(List<FileSource> fileSources) {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
                .flatMap(compositions -> libraryRepository.deleteCompositions(compositions)
                        .toSingleDefault(compositions));
    }

    public Single<List<Composition>> deleteFolder(FolderFileSource folder) {
        return libraryRepository.getAllCompositionsInPath(folder.getPath())
                .flatMap(compositions -> libraryRepository.deleteCompositions(compositions)
                        .toSingleDefault(compositions));
    }

    public Single<List<Composition>> addCompositionsToPlayList(String path, PlayList playList) {
        return libraryRepository.getAllCompositionsInPath(path)
                .flatMap(compositions -> playListsRepository.addCompositionsToPlayList(compositions, playList)
                        .toSingleDefault(compositions));
    }

    public Single<List<Composition>> addCompositionsToPlayList(List<FileSource> fileSources, PlayList playList) {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
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
        return libraryRepository.getAvailablePathsForPath(currentPath);
    }

    public Completable renameFolder(String folderPath, String newName) {
        return editorRepository.changeFolderName(folderPath, newName)
                .flatMapCompletable(newPath ->
                        libraryRepository.changeFolderName(folderPath, newPath)
                                .flatMapCompletable(editorRepository::changeCompositionsFilePath)
                );
    }

    public void addFilesToMove(String fromMovePath, Collection<FileSource> fileSources) {
        filesToMove.clear();
        filesToMove.addAll(fileSources);
        this.fromMovePath = fromMovePath;
        moveModeSubject.onNext(true);
    }

    public void addFilesToCopy(String fromMovePath, Collection<FileSource> fileSources) {
        filesToCopy.clear();
        filesToCopy.addAll(fileSources);
        this.fromMovePath = fromMovePath;
        moveModeSubject.onNext(true);
    }

    public void stopMoveMode() {
        filesToCopy.clear();
        filesToMove.clear();
        fromMovePath = null;
        moveModeSubject.onNext(false);
    }

    public Completable copyFilesTo(String path) {
        Completable completable;
        if (!filesToMove.isEmpty()) {
            completable = Observable.fromIterable(filesToMove)
                    .flatMapCompletable(fileSource -> moveFiles(fileSource, path));
        } else if (!filesToCopy.isEmpty()) {
            completable = Completable.error(new Exception("not implemented"));
        } else {
            throw new IllegalStateException("unexpected state");
        }
        return completable.doOnComplete(this::stopMoveMode);
    }

    public Completable moveFilesToNewFolder(String path, String folderName) {
        //TODO root(null) path issue
        String newFolderPath = path + "/" + folderName;
        Completable completable = editorRepository.createDirectory(newFolderPath);
        if (!filesToMove.isEmpty()) {
            completable = completable.andThen(Observable.fromIterable(filesToMove)
                    .flatMapCompletable(fileSource -> moveFiles(fileSource, newFolderPath))
            );
        } else if (!filesToCopy.isEmpty()) {
            completable = Completable.error(new Exception("not implemented"));
        } else {
            throw new IllegalStateException("unexpected state");
        }
        return completable.doOnComplete(this::stopMoveMode);
    }

    public BehaviorSubject<Boolean> getMoveModeObservable() {
        return moveModeSubject;
    }

    public LinkedHashSet<FileSource> getFilesToMove() {
        return filesToMove;
    }

    public Completable addFolderToIgnore(IgnoredFolder folder) {
        return libraryRepository.addFolderToIgnore(folder)
                .andThen(mediaScannerRepository.runStorageScanner());
    }

    public Observable<List<IgnoredFolder>> getIgnoredFoldersObservable() {
        return libraryRepository.getIgnoredFoldersObservable();
    }

    public Completable deleteIgnoredFolder(IgnoredFolder folder) {
        return libraryRepository.deleteIgnoredFolder(folder)
                .andThen(mediaScannerRepository.runStorageScanner());
    }

    private Completable moveFiles(FileSource fileSource, String path) {
        return editorRepository.moveFile(fileSource.getPath(), fromMovePath, path)
                .flatMap(newPath -> libraryRepository.moveFileTo(path, newPath, fileSource))
                .flatMapCompletable(editorRepository::changeCompositionsFilePath);
    }
}
