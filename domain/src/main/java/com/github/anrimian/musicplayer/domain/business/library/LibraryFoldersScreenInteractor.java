package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;

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

public class LibraryFoldersScreenInteractor {

    private final LibraryFoldersInteractor foldersInteractor;
    private final LibraryRepository libraryRepository;
    private final EditorRepository editorRepository;
    private final MediaScannerRepository mediaScannerRepository;


    private final BehaviorSubject<Boolean> moveModeSubject = BehaviorSubject.createDefault(false);
    private final LinkedHashSet<FileSource2> filesToCopy = new LinkedHashSet<>();
    private final LinkedHashSet<FileSource2> filesToMove = new LinkedHashSet<>();
    private String fromMovePath;

    public LibraryFoldersScreenInteractor(LibraryFoldersInteractor foldersInteractor,
                                          LibraryRepository libraryRepository,
                                          EditorRepository editorRepository,
                                          MediaScannerRepository mediaScannerRepository) {
        this.foldersInteractor = foldersInteractor;
        this.libraryRepository = libraryRepository;
        this.editorRepository = editorRepository;
        this.mediaScannerRepository = mediaScannerRepository;
    }

    public Single<Folder> getCompositionsInPath(@Nullable String path, @Nullable String searchText) {
        return foldersInteractor.getCompositionsInPath(path, searchText);
    }

    public Observable<List<FileSource2>> getFoldersInFolder(@Nullable Long folderId,
                                                            @Nullable String searchQuery) {
        return foldersInteractor.getFoldersInFolder(folderId, searchQuery);
    }

    public Observable<FolderFileSource2> getFolderObservable(long folderId) {
        return foldersInteractor.getFolderObservable(folderId);
    }

    public void playAllMusicInPath(@Nullable String path) {
        foldersInteractor.playAllMusicInPath(path);
    }

    public Single<List<Composition>> getAllCompositionsInPath(@Nullable String path) {
        return foldersInteractor.getAllCompositionsInPath(path);
    }

    public Single<List<Composition>> getAllCompositionsInFileSources(List<FileSource> fileSources) {
        return foldersInteractor.getAllCompositionsInFileSources(fileSources);
    }

    public void play(List<FileSource> fileSources) {
        foldersInteractor.play(fileSources);
    }

    public void play(String path, Composition composition) {
        foldersInteractor.play(path, composition);
    }

    public void addCompositionsToPlayNext(List<FileSource> fileSources) {
        foldersInteractor.addCompositionsToPlayNext(fileSources);
    }

    public void addCompositionsToEnd(List<FileSource> fileSources) {
        foldersInteractor.addCompositionsToEnd(fileSources);
    }

    public Single<List<Composition>> deleteCompositions(List<FileSource> fileSources) {
        return foldersInteractor.deleteCompositions(fileSources);
    }

    public Single<List<Composition>> deleteFolder(FolderFileSource folder) {
        return foldersInteractor.deleteFolder(folder);
    }

    public Single<List<Composition>> addCompositionsToPlayList(String path, PlayList playList) {
        return foldersInteractor.addCompositionsToPlayList(path, playList);
    }

    public Single<List<Composition>> addCompositionsToPlayList(List<FileSource> fileSources, PlayList playList) {
        return foldersInteractor.addCompositionsToPlayList(fileSources, playList);
    }

    public void setFolderOrder(Order order) {
        foldersInteractor.setFolderOrder(order);
    }

    public Order getFolderOrder() {
        return foldersInteractor.getFolderOrder();
    }

    public void saveCurrentPath(@Nullable String path) {
        foldersInteractor.saveCurrentPath(path);
    }

    public Single<List<String>> getCurrentFolderScreens() {
        return foldersInteractor.getCurrentFolderScreens();
    }

    public Completable renameFolder(String folderPath, String newName) {
        return foldersInteractor.renameFolder(folderPath, newName);
    }

    public void addFilesToMove(String fromMovePath, Collection<FileSource2> fileSources) {
        filesToMove.clear();
        filesToMove.addAll(fileSources);
        this.fromMovePath = fromMovePath;
        moveModeSubject.onNext(true);
    }

    public void addFilesToCopy(String fromMovePath, Collection<FileSource2> fileSources) {
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

    public LinkedHashSet<FileSource2> getFilesToMove() {
        return filesToMove;
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

    private Completable moveFiles(FileSource2 fileSource, String path) {
        return Completable.complete();
//        return editorRepository.moveFile(fileSource.getPath(), fromMovePath, path)
//                .flatMap(newPath -> libraryRepository.moveFileTo(path, newPath, fileSource))
//                .flatMapCompletable(editorRepository::changeCompositionsFilePath);
    }
}
