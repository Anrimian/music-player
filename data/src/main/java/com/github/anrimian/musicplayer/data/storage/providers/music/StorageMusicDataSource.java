package com.github.anrimian.musicplayer.data.storage.providers.music;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.files.FileManager;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;

import java.io.File;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapListNotNull;

public class StorageMusicDataSource {

    private final StorageMusicProvider musicProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final GenresDaoWrapper genreDao;
    private final FileManager fileManager;
    private final Scheduler scheduler;

    public StorageMusicDataSource(StorageMusicProvider musicProvider,
                                  CompositionsDaoWrapper compositionsDao,
                                  GenresDaoWrapper genreDao,
                                  FileManager fileManager,
                                  Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.compositionsDao = compositionsDao;
        this.genreDao = genreDao;
        this.fileManager = fileManager;
        this.scheduler = scheduler;
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

    public Completable updateCompositionAuthor(FullComposition composition, String authorName) {
        return Completable.fromAction(() -> {
            compositionsDao.updateArtist(composition.getId(), authorName);
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                musicProvider.updateCompositionAuthor(storageId, authorName);
            }
        });
    }

    public Completable updateCompositionAlbumArtist(FullComposition composition, String authorName) {
        return Completable.fromAction(() ->
                compositionsDao.updateAlbumArtist(composition.getId(), authorName)
        );
    }

    public Completable updateCompositionGenre(FullComposition composition, String genre) {
        return Completable.fromAction(() -> {
            genreDao.updateCompositionGenre(composition.getId(), genre);
//            Long storageId = composition.getStorageId();
//            if (storageId != null) {
//                storageGenresProvider.updateCompositionGenre(storageId, genre);
//            }
        });
    }

    public Completable updateCompositionAlbum(FullComposition composition, String albumName) {
        return Completable.fromAction(() -> {
            compositionsDao.updateAlbum(composition.getId(), albumName);
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                musicProvider.updateCompositionAlbum(storageId, albumName);
            }
        });
    }

    public Completable updateCompositionTitle(FullComposition composition, String title) {
        return Completable.fromAction(() -> {
            compositionsDao.updateTitle(composition.getId(), title);
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                musicProvider.updateCompositionTitle(storageId, title);
            }
        });
    }

    public Completable updateCompositionFilePath(FullComposition composition, String filePath) {
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
