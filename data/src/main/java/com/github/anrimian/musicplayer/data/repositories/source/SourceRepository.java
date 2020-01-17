package com.github.anrimian.musicplayer.data.repositories.source;

import android.net.Uri;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.Single;

public class SourceRepository {

    private final CompositionsDaoWrapper compositionsDao;
    private final StorageMusicProvider storageMusicProvider;
    private final Scheduler scheduler;

    public SourceRepository(CompositionsDaoWrapper compositionsDao,
                            StorageMusicProvider storageMusicProvider,
                            Scheduler scheduler) {
        this.compositionsDao = compositionsDao;
        this.storageMusicProvider = storageMusicProvider;
        this.scheduler = scheduler;
    }

    public Single<Uri> getCompositionUri(long compositionId) {
        return Single.fromCallable(() -> compositionsDao.getStorageId(compositionId))
                .map(id -> storageMusicProvider.getCompositionUri(compositionId))
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(scheduler);
    }

    public InputStream getCompositionStream(long compositionId) throws FileNotFoundException {
        long storageId =  compositionsDao.getStorageId(compositionId);
        return storageMusicProvider.getCompositionStream(storageId);
    }

}
