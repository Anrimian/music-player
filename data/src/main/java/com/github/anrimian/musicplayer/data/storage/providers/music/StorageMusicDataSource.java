package com.github.anrimian.musicplayer.data.storage.providers.music;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.files.FileManager;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.io.File;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapListNotNull;

public class StorageMusicDataSource {

    private final StorageMusicProvider musicProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final FileManager fileManager;
    private final Scheduler scheduler;

    public StorageMusicDataSource(StorageMusicProvider musicProvider,
                                  CompositionsDaoWrapper compositionsDao,
                                  FileManager fileManager,
                                  Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.compositionsDao = compositionsDao;
        this.fileManager = fileManager;
        this.scheduler = scheduler;
    }

    public Observable<Composition> getCompositionObservable(long id) {
        return compositionsDao.getCompositionObservable(id);
    }

    public Completable deleteCompositions(List<Composition> compositionsToDelete) {
        return Completable.fromAction(() -> {
            for (Composition composition: compositionsToDelete) {
                deleteCompositionFile(composition);
            }
            compositionsDao.deleteAll(mapList(compositionsToDelete, Composition::getId));
            musicProvider.deleteCompositions(mapListNotNull(
                    compositionsToDelete,
                    Composition::getStorageId)
            );
        });
    }

    public Completable deleteComposition(Composition composition) {
        return Completable.fromAction(() -> {
            deleteCompositionFile(composition);
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                musicProvider.deleteComposition(storageId);
            }
            compositionsDao.delete(composition.getId());
        });
    }

    public Completable updateCompositionAuthor(Composition composition, String authorName) {
        return Completable.fromAction(() -> {
            compositionsDao.updateArtist(composition.getId(), authorName);
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                musicProvider.updateCompositionAuthor(storageId, authorName);
            }
        });
    }

    public Completable updateCompositionAlbum(Composition composition, String albumName) {
        return Completable.fromAction(() -> {
            compositionsDao.updateAlbum(composition.getId(), albumName);
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                musicProvider.updateCompositionAlbum(storageId, albumName);
            }
        });
    }

    public Completable updateCompositionTitle(Composition composition, String title) {
        return Completable.fromAction(() -> {
            compositionsDao.updateTitle(composition.getId(), title);
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                musicProvider.updateCompositionTitle(storageId, title);
            }
        });
    }

    public Completable updateCompositionFilePath(Composition composition, String filePath) {
        return Completable.fromAction(() -> {
            compositionsDao.updateFilePath(composition.getId(), filePath);
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                musicProvider.updateCompositionFilePath(storageId, filePath);
            }
        });
    }

    public Completable updateCompositionsFilePath(List<Composition> compositions) {
        return Completable.fromAction(() -> musicProvider.updateCompositionsFilePath(compositions))
                .subscribeOn(scheduler);
    }

    private void deleteCompositionFile(Composition composition) {
        String filePath = composition.getFilePath();
        File parentDirectory = new File(filePath).getParentFile();

        fileManager.deleteFile(filePath);
        if (parentDirectory != null) {
            fileManager.deleteEmptyDirectory(parentDirectory);
        }
    }
}
