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

@Deprecated
public class StorageMusicDataSource {

    private final StorageMusicProvider musicProvider;
    private final CompositionsDaoWrapper compositionsDao;

    public StorageMusicDataSource(StorageMusicProvider musicProvider,
                                  CompositionsDaoWrapper compositionsDao) {
        this.musicProvider = musicProvider;
        this.compositionsDao = compositionsDao;
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
}
