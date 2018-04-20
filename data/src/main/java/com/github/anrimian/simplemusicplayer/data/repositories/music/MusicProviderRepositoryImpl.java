package com.github.anrimian.simplemusicplayer.data.repositories.music;

import android.content.Context;
import android.database.Cursor;

import com.github.anrimian.simplemusicplayer.data.repositories.music.exceptions.MusicNotFoundException;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.data.utils.IOUtils;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import static android.provider.MediaStore.Audio;
import static android.provider.MediaStore.Images;

/**
 * Created on 24.10.2017.
 */

public class MusicProviderRepositoryImpl implements MusicProviderRepository {

    private StorageMusicDataSource storageMusicDataSource;
    private Scheduler scheduler;

    public MusicProviderRepositoryImpl(StorageMusicDataSource storageMusicDataSource,
                                       Scheduler scheduler) {
        this.storageMusicDataSource = storageMusicDataSource;
        this.scheduler = scheduler;
    }

    @Override
    public Single<List<Composition>> getAllCompositions() {
        return storageMusicDataSource.getAllCompositions()
                .subscribeOn(scheduler);
    }

    @Override
    public Completable setCompositionCorrupted(Composition composition) {
        return Completable.complete()
                .subscribeOn(scheduler);
    }
}
