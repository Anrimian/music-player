package com.github.anrimian.musicplayer.data.storage.providers.music;

import com.github.anrimian.musicplayer.data.storage.files.FileManager;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.exceptions.StorageTimeoutException;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;
import com.github.anrimian.musicplayer.domain.utils.changes.ChangeType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.musicplayer.domain.Constants.TIMEOUTS.STORAGE_LOADING_TIMEOUT_SECONDS;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.areSourcesTheSame;
import static java.util.Collections.singletonList;


public class StorageMusicDataSource {

    private final StorageMusicProvider musicProvider;
    private final FileManager fileManager;
    private final Scheduler scheduler;

    private final PublishSubject<Change<List<Composition>>> changeSubject = PublishSubject.create();
    private final BehaviorSubject<Map<Long, Composition>> compositionSubject = BehaviorSubject.create();

    private Map<Long, Composition> compositions;
    private Disposable changeDisposable;

    public StorageMusicDataSource(StorageMusicProvider musicProvider,
                                  FileManager fileManager,
                                  Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.fileManager = fileManager;
        this.scheduler = scheduler;
    }

    public Single<Map<Long, Composition>> getCompositions() {
        return Single.fromCallable(this::getCompositionsMap)
//                .delay(3, TimeUnit.SECONDS)//test timeout error
                .timeout(STORAGE_LOADING_TIMEOUT_SECONDS, TimeUnit.SECONDS, Single.error(new StorageTimeoutException()))
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

    public Composition getCompositionById(long id) {
        if (compositions == null) {
            return musicProvider.getComposition(id);
        }
        return compositions.get(id);
    }

    public Observable<Change<List<Composition>>> getChangeObservable() {
        return changeSubject;
    }

    public Observable<Map<Long, Composition>> getCompositionObservable() {
        return withDefaultValue(compositionSubject, getCompositions());
    }

    public Completable deleteCompositions(List<Composition> compositionsToDelete) {
        return getCompositions()
                .doOnSuccess(compositions -> {
                    for (Composition composition: compositionsToDelete) {
                        deleteCompositionInternal(compositions, composition);
                    }
                    compositionSubject.onNext(compositions);
                    changeSubject.onNext(new Change<>(ChangeType.DELETED, compositionsToDelete));
                })
                .ignoreElement()
                .subscribeOn(scheduler);
    }

    public Completable deleteComposition(Composition composition) {
        return getCompositions()
                .doOnSuccess(compositions -> {
                    deleteCompositionInternal(compositions, composition);
                    compositionSubject.onNext(compositions);
                    changeSubject.onNext(new Change<>(ChangeType.DELETED, singletonList(composition)));
                })
                .ignoreElement()
                .subscribeOn(scheduler);
    }

    public Completable updateCompositionAuthor(Composition composition, String author) {
        return Completable.fromAction(() -> musicProvider.updateCompositionAuthor(composition, author))
                .subscribeOn(scheduler);
    }

    private void deleteCompositionInternal(Map<Long, Composition> compositions,
                                           Composition composition) {
        String filePath = composition.getFilePath();
        File parentDirectory = new File(filePath).getParentFile();

        musicProvider.deleteComposition(filePath);
        fileManager.deleteFile(filePath);
        compositions.remove(composition.getId());

        fileManager.deleteEmptyDirectory(parentDirectory);
    }

    private void subscribeOnCompositionChanges() {
        if (changeDisposable != null) {
            throw new IllegalStateException("subscribe on composition changes twice");
        }
        changeDisposable = musicProvider.getChangeObservable()
                .flatMap(this::calculateChange)
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
                } else if (!areSourcesTheSame(newComposition, existComposition)) {
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

            if (!deletedCompositions.isEmpty()
                    || !addedCompositions.isEmpty()
                    || !changedCompositions.isEmpty()) {
                compositionSubject.onNext(existsCompositions);
            }
        });
    }
}
