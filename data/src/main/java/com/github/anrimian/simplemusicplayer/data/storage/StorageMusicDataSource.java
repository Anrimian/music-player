package com.github.anrimian.simplemusicplayer.data.storage;

import com.github.anrimian.simplemusicplayer.data.utils.Objects;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import static java.util.Collections.singletonList;


public class StorageMusicDataSource {

    private final StorageMusicProvider musicProvider;
    private final Scheduler scheduler;

    private final PublishSubject<Change<List<Composition>>> changeSubject = PublishSubject.create();

    private Map<Long, Composition> compositions;
    private Disposable changeDisposable;

    public StorageMusicDataSource(StorageMusicProvider musicProvider, Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.scheduler = scheduler;
    }

    public Single<Map<Long, Composition>> getCompositions() {
        return Single.fromCallable(this::getCompositionsMap)
                .subscribeOn(scheduler);
    }

    public Map<Long, Composition> getCompositionsMap() {
        if (compositions == null) {
            synchronized (StorageMusicDataSource.this) {
                if (compositions == null) {
                    compositions = musicProvider.getCompositions();
                    subscribeOnCompositionChanges();
                }
            }
        }
        return compositions;
    }

    public Observable<Change<List<Composition>>> getChangeObservable() {
        return changeSubject.subscribeOn(scheduler);
    }

    public Completable deleteComposition(Composition composition) {
        return getCompositions()
                .doOnSuccess(compositions -> {
                    musicProvider.deleteComposition(composition.getFilePath());
                    compositions.remove(composition.getId());
                    changeSubject.onNext(new Change<>(ChangeType.DELETED, singletonList(composition)));
                })
                .toCompletable()
                .subscribeOn(scheduler);
    }

    private void subscribeOnCompositionChanges() {
        if (changeDisposable != null) {
            throw new IllegalStateException("subscribe on composition changes twice");
        }
        changeDisposable = musicProvider.getChangeObservable()
                .flatMap(this::calculateChange)
//                .doOnNext(change -> Log.d("KEK", "change: " + change.getChangeType() + ", compositions: " + change.getData()))
                .subscribeOn(scheduler)
                .subscribe(changeSubject::onNext);
    }

    private Observable<Change<List<Composition>>> calculateChange(Map<Long, Composition> newCompositions) {
        return Observable.create(emitter -> {
            List<Composition> deletedCompositions = new ArrayList<>();
            List<Composition> addedCompositions = new ArrayList<>();
            List<Composition> changedCompositions = new ArrayList<>();

            Map<Long, Composition> existsCompositions = compositions;

            for (Composition existComposition: new HashMap<>(existsCompositions).values()) {
                Composition newComposition = newCompositions.get(existComposition.getId());
                if (newComposition == null) {
                    deletedCompositions.add(existComposition);
                    existsCompositions.remove(existComposition.getId());
                }
            }

            for (Composition newComposition: newCompositions.values()) {
                Composition existComposition = existsCompositions.get(newComposition.getId());
                if (existComposition == null) {
                    addedCompositions.add(newComposition);
                    existsCompositions.put(newComposition.getId(), newComposition);
                } else if (hasChanges(newComposition, existComposition)) {
                    changedCompositions.add(newComposition);
                    existsCompositions.put(newComposition.getId(), newComposition);
                }
            }

            if (!deletedCompositions.isEmpty()) {
                emitter.onNext(new Change<>(ChangeType.DELETED, deletedCompositions));
            }
            if (!addedCompositions.isEmpty()) {
                emitter.onNext(new Change<>(ChangeType.ADDED, addedCompositions));
            }
            if (!changedCompositions.isEmpty()) {
                emitter.onNext(new Change<>(ChangeType.MODIFY, changedCompositions));
            }
        });
    }

    private boolean hasChanges(@Nonnull Composition first, @Nonnull Composition second) {
        return !Objects.equals(first.getAlbum(), second.getAlbum())
                || !Objects.equals(first.getArtist(), second.getArtist())
                || !Objects.equals(first.getComposer(), second.getComposer())
                || !Objects.equals(first.getDateAdded(), second.getDateAdded())
                || !Objects.equals(first.getDateModified(), second.getDateModified())
                || !Objects.equals(first.getDisplayName(), second.getDisplayName())
                || first.getDuration() != second.getDuration()
                || !Objects.equals(first.getFilePath(), second.getFilePath())
                || first.getSize() != second.getSize()
                || !Objects.equals(first.getTitle(), second.getTitle())
                || !Objects.equals(first.getYear(), second.getYear());
    }
}