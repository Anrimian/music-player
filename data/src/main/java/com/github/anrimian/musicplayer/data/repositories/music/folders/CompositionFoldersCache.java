package com.github.anrimian.musicplayer.data.repositories.music.folders;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;
import com.github.anrimian.musicplayer.domain.utils.changes.ModifiedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.areSourcesTheSame;

/**
 * 'temp' solution for composition file tree
 */
public class CompositionFoldersCache {

    private final CompositionsDaoWrapper compositionsDao;

    private final PublishSubject<Change<Composition>> changeSubject = PublishSubject.create();
    private final BehaviorSubject<Map<Long, Composition>> compositionSubject = BehaviorSubject.create();

    private Map<Long, Composition> compositions;
    private Disposable changeDisposable;

    public CompositionFoldersCache(CompositionsDaoWrapper compositionsDao) {
        this.compositionsDao = compositionsDao;
    }

    public Map<Long, Composition> getCompositionsMap() {
        if (compositions == null) {
            synchronized (this) {
                if (compositions == null) {
                    compositions = compositionsDao.getAllMap();
                    subscribeOnCompositionChanges();
                }
            }
        }
        return compositions;
    }

    public Observable<Change<Composition>> getChangeObservable() {
        return changeSubject;
    }

    private void subscribeOnCompositionChanges() {
        if (changeDisposable != null) {
            throw new IllegalStateException("subscribe on composition changes twice");
        }
        changeDisposable = compositionsDao.getAllObservable()
                .map(list -> ListUtils.mapToMap(list, new HashMap<>(), Composition::getId))
                .flatMap(this::calculateChange)
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
