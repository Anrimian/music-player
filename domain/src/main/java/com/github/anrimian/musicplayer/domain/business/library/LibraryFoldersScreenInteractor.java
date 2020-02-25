package com.github.anrimian.musicplayer.domain.business.library;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
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

    @Nullable
    private Long moveFromFolderId;

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

    public void playAllMusicInFolder(@Nullable Long folderId) {
        foldersInteractor.playAllMusicInFolder(folderId);
    }

    public Single<List<Composition>> getAllCompositionsInFolder(@Nullable Long folderId) {
        return foldersInteractor.getAllCompositionsInFolder(folderId);
    }

    public Single<List<Composition>> getAllCompositionsInFileSources(List<FileSource2> fileSources) {
        return foldersInteractor.getAllCompositionsInFileSources(fileSources);
    }

    public void play(List<FileSource2> fileSources) {
        foldersInteractor.play(fileSources);
    }

    public void play(Long folderId, Composition composition) {
        foldersInteractor.play(folderId, composition);
    }

    public void addCompositionsToPlayNext(List<FileSource2> fileSources) {
        foldersInteractor.addCompositionsToPlayNext(fileSources);
    }

    public void addCompositionsToEnd(List<FileSource2> fileSources) {
        foldersInteractor.addCompositionsToEnd(fileSources);
    }

    public Single<List<Composition>> deleteFiles(List<FileSource2> fileSources) {
        return foldersInteractor.deleteCompositions(fileSources);
    }

    public Single<List<Composition>> deleteFolder(FolderFileSource2 folder) {
        return foldersInteractor.deleteFolder(folder);
    }

    public Single<List<Composition>> addCompositionsToPlayList(FolderFileSource2 folder, PlayList playList) {
        return foldersInteractor.addCompositionsToPlayList(folder, playList);
    }

    public Single<List<Composition>> addCompositionsToPlayList(List<FileSource2> fileSources, PlayList playList) {
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

    public Completable renameFolder(long folderId, String newName) {
        return foldersInteractor.renameFolder(folderId, newName);
    }

    public void addFilesToMove(@Nullable Long folderId, Collection<FileSource2> fileSources) {
        filesToMove.clear();
        filesToMove.addAll(fileSources);
        this.moveFromFolderId = folderId;
        moveModeSubject.onNext(true);
    }

    public void addFilesToCopy(@Nullable Long folderId, Collection<FileSource2> fileSources) {
        filesToCopy.clear();
        filesToCopy.addAll(fileSources);
        this.moveFromFolderId = folderId;
        moveModeSubject.onNext(true);
    }

    public void stopMoveMode() {
        filesToCopy.clear();
        filesToMove.clear();
        moveFromFolderId = null;
        moveModeSubject.onNext(false);
    }

    public Completable copyFilesTo(@Nullable Long folderId) {
        Completable completable;
        if (!filesToMove.isEmpty()) {
            completable = editorRepository.moveFiles(filesToMove, moveFromFolderId, folderId);
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
