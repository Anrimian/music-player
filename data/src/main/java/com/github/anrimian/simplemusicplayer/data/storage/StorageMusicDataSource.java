package com.github.anrimian.simplemusicplayer.data.storage;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeableList;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;


public class StorageMusicDataSource {

    private final StorageMusicProvider musicProvider;
    private final Scheduler scheduler;

    @Nullable
    private ChangeableList<Composition> compositions;

    public StorageMusicDataSource(StorageMusicProvider musicProvider, Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.scheduler = scheduler;
    }

    public Single<ChangeableList<Composition>> getCompositions() {
        return Single.fromCallable(this::getList)
                .observeOn(scheduler);
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
                .share()
                .observeOn(scheduler);
        return new ChangeableList<>(compositions, changeObservable);
    }

    private Observable<Change<Composition>> calculateChange(Object o) {
        return Observable.create(emitter -> {
            List<Composition> existsCompositions = compositions.getList();
            List<Composition> newCompositions = musicProvider.getCompositions();

            List<Composition> deletedCompositions = new ArrayList<>();
            for (int existsIndex = 0; existsIndex < existsCompositions.size(); existsIndex++) {
                Composition existsComposition = existsCompositions.get(existsIndex);

                boolean deleted = true;
                for (Composition newComposition: newCompositions) {
                    if (existsComposition.equals(newComposition)) {
                        deleted = false;
                    }
                }
                if (deleted) {
                    deletedCompositions.add(existsComposition);
                }
            }
            if (!deletedCompositions.isEmpty()) {
                emitter.onNext(new Change<>(ChangeType.DELETED, deletedCompositions));
            }
        });
    }
}
