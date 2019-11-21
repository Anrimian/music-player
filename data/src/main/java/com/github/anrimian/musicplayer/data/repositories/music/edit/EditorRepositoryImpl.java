package com.github.anrimian.musicplayer.data.repositories.music.edit;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

public class EditorRepositoryImpl implements EditorRepository {

    private final CompositionSourceEditor sourceEditor = new CompositionSourceEditor();

    private final StorageMusicDataSource storageMusicDataSource;
    private final Scheduler scheduler;

    public EditorRepositoryImpl(StorageMusicDataSource storageMusicDataSource,
                                Scheduler scheduler) {
        this.storageMusicDataSource = storageMusicDataSource;
        this.scheduler = scheduler;
    }

    @Override
    public Completable changeCompositionAuthor(Composition composition, String newAuthor) {
        return sourceEditor.setCompositionAuthor(composition.getFilePath(), newAuthor)
                .andThen(storageMusicDataSource.updateCompositionAuthor(composition, newAuthor))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAlbum(Composition composition, String newAlbum) {
        return sourceEditor.setCompositionAlbum(composition.getFilePath(), newAlbum)
                .andThen(storageMusicDataSource.updateCompositionAlbum(composition, newAlbum))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionTitle(Composition composition, String title) {
        return sourceEditor.setCompositionTitle(composition.getFilePath(), title)
                .andThen(storageMusicDataSource.updateCompositionTitle(composition, title))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionFileName(Composition composition, String fileName) {
        return Single.fromCallable(() -> FileUtils.getChangedFilePath(composition.getFilePath(), fileName))
                .flatMap(newPath -> renameFile(composition.getFilePath(), newPath))
                .flatMapCompletable(newPath -> storageMusicDataSource.updateCompositionFilePath(composition, newPath))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionsFilePath(List<Composition> compositions) {
        return storageMusicDataSource.updateCompositionsFilePath(compositions)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String> changeFolderName(String filePath, String folderName) {
        return Single.fromCallable(() -> FileUtils.getChangedFilePath(filePath, folderName))
                .flatMap(newPath -> renameFile(filePath, newPath))
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String> moveFile(String filePath, String oldPath, String newPath) {
        if (oldPath.equals(newPath)) {
            return Single.error(new MoveInTheSameFolderException("move in the same folder"));
        }
        return Single.fromCallable(() -> FileUtils.getChangedFilePath(filePath, oldPath, newPath))
                .flatMap(path -> renameFile(filePath, path))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable createFile(String path) {
        return Completable.fromAction(() -> {
            File file = new File(path);
            if (file.exists()) {
                throw new FileExistsException();
            }
            if (!file.mkdir()) {
                throw new Exception("file not created");
            }
        }).subscribeOn(scheduler);
    }

    private Single<String> renameFile(String oldPath, String newPath) {
        return Single.create(emitter -> {

            File oldFile = new File(oldPath);
            File newFile = new File(newPath);
            if (oldFile.renameTo(newFile)) {
                emitter.onSuccess(newPath);
            } else {
                emitter.onError(new Exception("file not renamed"));
            }
        });
    }



}
