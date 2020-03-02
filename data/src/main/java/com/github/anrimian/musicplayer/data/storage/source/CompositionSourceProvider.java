package com.github.anrimian.musicplayer.data.storage.source;

import android.net.Uri;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.Single;

public class CompositionSourceProvider {

    private final CompositionsDaoWrapper compositionsDao;
    private final StorageMusicProvider storageMusicProvider;
    private final Scheduler scheduler;

    public CompositionSourceProvider(CompositionsDaoWrapper compositionsDao,
                                     StorageMusicProvider storageMusicProvider,
                                     Scheduler scheduler) {
        this.compositionsDao = compositionsDao;
        this.storageMusicProvider = storageMusicProvider;
        this.scheduler = scheduler;
    }

    public Single<Uri> getCompositionUri(long compositionId) {
        return Single.fromCallable(() -> compositionsDao.getStorageId(compositionId))
                .map(storageMusicProvider::getCompositionUri)
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(scheduler);
    }

    public Single<FileDescriptor> getCompositionFileDescriptor(long compositionId) {
        return Single.fromCallable(() -> compositionsDao.getStorageId(compositionId))
                .map(storageMusicProvider::getFileDescriptor)
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(scheduler);
    }

    public InputStream getCompositionStream(long compositionId) throws FileNotFoundException {
        long storageId =  compositionsDao.getStorageId(compositionId);
        return storageMusicProvider.getCompositionStream(storageId);
    }

//    public Uri getCompositionUri(long compositionId) {
//        long storageId =  compositionsDao.getStorageId(compositionId);
//        return storageMusicProvider.getCompositionUri(storageId);
//    }

}
