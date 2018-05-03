package com.github.anrimian.simplemusicplayer.data.repositories.music;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeableList;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

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
        return storageMusicDataSource.getCompositions()
                .doOnSuccess(compositionChangeableList -> compositionChangeableList.getChangeObservable()
                        .subscribe())
                .map(ChangeableList::getList)
                .subscribeOn(scheduler);
    }

    @Override
    public Completable onErrorWithComposition(Throwable throwable, Composition composition) {
        return Completable.complete()
                .subscribeOn(scheduler);
    }
}
