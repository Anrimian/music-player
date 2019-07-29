package com.github.anrimian.musicplayer.data.repositories.music.edit;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.repositories.CompositionEditorRepository;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.io.File;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

public class CompositionEditorRepositoryImpl implements CompositionEditorRepository {

    private final CompositionSourceEditor sourceEditor = new CompositionSourceEditor();

    private final StorageMusicDataSource storageMusicDataSource;
    private final Scheduler scheduler;

    public CompositionEditorRepositoryImpl(StorageMusicDataSource storageMusicDataSource,
                                           Scheduler scheduler) {
        this.storageMusicDataSource = storageMusicDataSource;
        this.scheduler = scheduler;
    }

    @Override
    public Completable editCompositionAuthor(Composition composition, String newAuthor) {
        return sourceEditor.setCompositionAuthor(composition.getFilePath(), newAuthor)
                .andThen(storageMusicDataSource.updateCompositionAuthor(composition, newAuthor))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable editCompositionTitle(Composition composition, String title) {
        return sourceEditor.setCompositionTitle(composition.getFilePath(), title)
                .andThen(storageMusicDataSource.updateCompositionTitle(composition, title))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable editCompositionFileName(Composition composition, String fileName) {
        return Single.fromCallable(() -> FileUtils.getChangedFilePath(composition.getFilePath(), fileName))
                .flatMap(newPath -> renameFile(composition.getFilePath(), newPath))
                .flatMapCompletable(newPath -> storageMusicDataSource.updateCompositionFilePath(composition, newPath))
                .subscribeOn(scheduler);
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
