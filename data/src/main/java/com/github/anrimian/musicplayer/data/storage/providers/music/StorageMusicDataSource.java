package com.github.anrimian.musicplayer.data.storage.providers.music;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.files.FileManager;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.exceptions.StorageTimeoutException;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;
import com.github.anrimian.musicplayer.domain.utils.changes.ModifiedData;
import com.github.anrimian.musicplayer.domain.utils.changes.map.MapChangeProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

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
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapToMap;
import static java.util.Collections.singletonList;


public class StorageMusicDataSource {

    private final StorageMusicProvider musicProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final FileManager fileManager;
    private final Scheduler scheduler;

    private final PublishSubject<Change<Composition>> changeSubject = PublishSubject.create();
    private final BehaviorSubject<Map<Long, Composition>> compositionSubject = BehaviorSubject.create();

    private Map<Long, Composition> compositions;
    private Disposable changeDisposable;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private Disposable changeDisposable2;

    public StorageMusicDataSource(StorageMusicProvider musicProvider,
                                  CompositionsDaoWrapper compositionsDao,
                                  FileManager fileManager,
                                  Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.compositionsDao = compositionsDao;
        this.fileManager = fileManager;
        this.scheduler = scheduler;

        changeDisposable2 = musicProvider.getCompositionsObservable()
                .startWith(musicProvider.getCompositions())
                .subscribeOn(scheduler)
                .subscribe(this::onNewCompositionsFromMediaStorageReceived);
    }

    @Deprecated
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

    public Observable<Change<Composition>> getChangeObservable() {
        return changeSubject;
    }

    @Deprecated
    public Observable<Map<Long, Composition>> getCompositionObservable() {
        return withDefaultValue(compositionSubject, getCompositions());
    }

    @Deprecated
    public Observable<Map<Long, Composition>> getCompositionObservable2() {
        return compositionsDao.getAllObservable()
                .map(compositions -> mapToMap(compositions, new HashMap<>(), Composition::getId));
    }

    public Observable<Composition> getCompositionObservable(long id) {
        return compositionsDao.getCompoisitionObservable(id);
    }

    public Observable<List<Composition>> getCompositionObservable2(Order order,
                                                                   @Nullable String query) {
        return compositionsDao.getAllObservable(order, query);
    }

    public Completable deleteCompositions(List<Composition> compositionsToDelete) {
        return Completable.fromAction(() -> {
            for (Composition composition: compositionsToDelete) {
                deleteCompositionFile(composition);
            }
            compositionsDao.deleteAll(mapList(compositionsToDelete, Composition::getId));
            musicProvider.deleteCompositions(compositionsToDelete);
            changeSubject.onNext(new Change.DeleteChange<>(compositionsToDelete));
        });
    }

    public Completable deleteComposition(Composition composition) {
        return Completable.fromAction(() -> {
            deleteCompositionFile(composition);
            musicProvider.deleteComposition(composition.getId());
            compositionsDao.delete(composition.getId());
            changeSubject.onNext(new Change.DeleteChange<>(singletonList(composition)));
        });
    }

    public Completable updateCompositionAuthor(Composition composition, String author) {
        return Completable.fromAction(() -> {
            compositionsDao.updateArtist(composition.getId(), author);
            musicProvider.updateCompositionAuthor(composition, author);
        });
    }

    public Completable updateCompositionTitle(Composition composition, String title) {
        return Completable.fromAction(() -> {
            compositionsDao.updateTitle(composition.getId(), title);
            musicProvider.updateCompositionTitle(composition, title);
        });
    }

    public Completable updateCompositionFilePath(Composition composition, String filePath) {
        return Completable.fromAction(() -> {
            compositionsDao.updateFilePath(composition.getId(), filePath);
            musicProvider.updateCompositionFilePath(composition, filePath);
        });
    }

    public Completable updateCompositionsFilePath(List<Composition> compositions) {
        return Completable.fromAction(() -> musicProvider.updateCompositionsFilePath(compositions))
                .subscribeOn(scheduler);
    }

    private void onNewCompositionsFromMediaStorageReceived(Map<Long, Composition> newCompositions) {
        Map<Long, Composition> currentCompositions = compositionsDao.getAllMap();

        List<Composition> addedCompositions = new ArrayList<>();
        List<Composition> deletedCompositions = new ArrayList<>();
        List<Composition> changedCompositions = new ArrayList<>();
        boolean hasChanges = MapChangeProcessor.processChanges2(currentCompositions,
                newCompositions,
                CompositionHelper::hasChanges,
                deletedCompositions::add,
                addedCompositions::add,
                changedCompositions::add);

        if (hasChanges) {
            compositionsDao.applyChanges(addedCompositions, deletedCompositions, changedCompositions);
        }
    }

    private void deleteCompositionFile(Composition composition) {
        String filePath = composition.getFilePath();
        File parentDirectory = new File(filePath).getParentFile();

        fileManager.deleteFile(filePath);
        fileManager.deleteEmptyDirectory(parentDirectory);
    }

    private void subscribeOnCompositionChanges() {
        if (changeDisposable != null) {
            throw new IllegalStateException("subscribe on composition changes twice");
        }
        changeDisposable = musicProvider.getCompositionsObservable()
                .flatMap(this::calculateChange)
                .subscribeOn(scheduler)
                .subscribe(changeSubject::onNext);
    }

    private Observable<Change<Composition>> calculateChange(Map<Long, Composition> newCompositions) {
        return Observable.create(emitter -> {
            List<Composition> deletedCompositions = new ArrayList<>();
            List<Composition> addedCompositions = new ArrayList<>();
            List<ModifiedData<Composition>> changedCompositions = new ArrayList<>();

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
                    changedCompositions.add(new ModifiedData<>(existComposition, newComposition));
                    existsCompositions.put(newComposition.getId(), newComposition);
                }
            }

            if (!deletedCompositions.isEmpty()) {
                emitter.onNext(new Change.DeleteChange<>(deletedCompositions));
            }
            if (!addedCompositions.isEmpty()) {
                emitter.onNext(new Change.AddChange<>(addedCompositions));
            }
            if (!changedCompositions.isEmpty()) {
                emitter.onNext(new Change.ModifyChange<>(changedCompositions));
            }

            if (!deletedCompositions.isEmpty()
                    || !addedCompositions.isEmpty()
                    || !changedCompositions.isEmpty()) {
                compositionSubject.onNext(existsCompositions);
            }
        });
    }
}
