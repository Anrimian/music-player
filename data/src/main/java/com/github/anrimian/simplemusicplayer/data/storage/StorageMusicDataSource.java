package com.github.anrimian.simplemusicplayer.data.storage;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeableList;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Single;


public class StorageMusicDataSource {

    private final StorageMusicProvider musicProvider;

    @Nullable
    private ChangeableList<Composition> compositions;

    public StorageMusicDataSource(StorageMusicProvider musicProvider) {
        this.musicProvider = musicProvider;
    }

    public Single<ChangeableList<Composition>> getCompositions() {
        return Single.fromCallable(this::getList);
    }

    private ChangeableList<Composition> getList() {
        if (compositions == null) {
            synchronized (StorageMusicDataSource.this) {
                if (compositions == null) {
                    compositions = createList();
                }
            }
        }
        return compositions;
    }

    private ChangeableList<Composition> createList() {
        List<Composition> compositions = musicProvider.getCompositions();
        Observable<Change<Composition>> changeObservable = musicProvider.getChangeObservable()
                .flatMap(this::calculateChange)
                .share();
        return new ChangeableList<>(compositions, changeObservable);
    }

    private Observable<Change<Composition>> calculateChange(Object o) {
        return Observable.create(emitter -> {

        });
    }
}
