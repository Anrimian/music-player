package com.github.anrimian.simplemusicplayer.data.repositories.playlist;

import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static io.reactivex.subjects.BehaviorSubject.create;

/**
 * Created on 18.11.2017.
 */

public class PlayQueueRepositoryImpl implements PlayQueueRepository {

    private static final int NO_POSITION = -1;

    private final PlayQueueDataSource playQueueDataSource;
    private final UiStatePreferences uiStatePreferences;
    private final Scheduler dbScheduler;

    private final BehaviorSubject<List<Composition>> currentPlayListSubject = create();
    private final BehaviorSubject<Composition> currentCompositionSubject = create();

    private int position = NO_POSITION;

    public PlayQueueRepositoryImpl(PlayQueueDataSource playQueueDataSource,
                                   UiStatePreferences uiStatePreferences,
                                   Scheduler dbScheduler) {
        this.playQueueDataSource = playQueueDataSource;
        this.uiStatePreferences = uiStatePreferences;
        this.dbScheduler = dbScheduler;
    }

    @Override
    public Completable setPlayQueue(List<Composition> compositions) {
        return Completable.fromRunnable(() -> {
            checkCompositionsList(compositions);
            playQueueDataSource.setPlayQueue(compositions);
            List<Composition> playQueue = playQueueDataSource.getPlayQueue();
            currentPlayListSubject.onNext(playQueue);

            position = 0;
            updateCurrentComposition(playQueue, position);
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Observable<Composition> getCurrentCompositionObservable() {
        return getSavedComposition()
                .mergeWith(currentCompositionSubject)
                .subscribeOn(dbScheduler);
    }

    @Override
    public Observable<List<Composition>> getPlayQueueObservable() {
        return getSavedPlayQueue()
                .mergeWith(currentPlayListSubject)
                .subscribeOn(dbScheduler);
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        Composition currentComposition = currentCompositionSubject.getValue();
        if (currentComposition == null) {
            throw new IllegalStateException("change play mode without current composition");
        }

        position = playQueueDataSource.setRandomPlayingEnabled(enabled, currentComposition);
        List<Composition> playQueue = playQueueDataSource.getPlayQueue();
        currentPlayListSubject.onNext(playQueue);

        uiStatePreferences.setCurrentCompositionPosition(position);
    }

    @Override
    public void skipToNext() {
        List<Composition> currentPlayList = currentPlayListSubject.getValue();
        checkCompositionsList(currentPlayList);

        if (position >= currentPlayList.size() - 1) {
            position = 0;
        } else {
            position++;
        }
        updateCurrentComposition(currentPlayList, position);
    }

    @Override
    public void skipToPrevious() {
        List<Composition> currentPlayList = currentPlayListSubject.getValue();
        checkCompositionsList(currentPlayList);

        position--;
        if (position < 0) {
            position = currentPlayList.size() - 1;
        }
        updateCurrentComposition(currentPlayList, position);
    }

    @Override
    public void skipToPosition(int position) {
        List<Composition> currentPlayList = currentPlayListSubject.getValue();
        checkCompositionsList(currentPlayList);

        if (position < 0 || position >= currentPlayList.size()) {
            throw new IndexOutOfBoundsException("unexpected position: " + position);
        }

        this.position = position;
        updateCurrentComposition(currentPlayList, position);
    }

    private void updateCurrentComposition(List<Composition> currentPlayList, int position) {
        Composition currentComposition = currentPlayList.get(position);
        uiStatePreferences.setCurrentCompositionId(currentComposition.getId());
        uiStatePreferences.setCurrentCompositionPosition(position);
        currentCompositionSubject.onNext(currentComposition);
    }

    private Observable<Composition> getSavedComposition() {
        return Observable.create(emitter -> {
            if (currentCompositionSubject.getValue() == null) {
                List<Composition> playQueue = playQueueDataSource.getPlayQueue();
                Composition currentComposition = findCurrentComposition(playQueue);
                if (currentComposition != null) {
                    emitter.onNext(currentComposition);
                }
            }
        });
    }

    private Observable<List<Composition>> getSavedPlayQueue() {
        return Observable.create(emitter -> {
            if (currentPlayListSubject.getValue() == null) {
                List<Composition> playQueue = playQueueDataSource.getPlayQueue();
                emitter.onNext(playQueue);
            }
        });
    }

    @Nullable
    private Composition findCurrentComposition(List<Composition> compositions) {
        long id = uiStatePreferences.getCurrentCompositionId();
        int position = uiStatePreferences.getCurrentCompositionPosition();

        //optimized way
        if (position > 0 && position < compositions.size()) {
            Composition expectedComposition = compositions.get(position);
            if (expectedComposition.getId() == id) {
                return expectedComposition;
            }
        }

        if (id == NO_COMPOSITION) {
            return null;
        }

        for (Composition composition: compositions) {
            if (composition.getId() == id) {
                return composition;
            }
        }
        return null;
    }

    private void checkCompositionsList(@Nullable List<Composition> compositions) {
        if (compositions == null || compositions.isEmpty()) {
            throw new IllegalStateException("empty play queue");
        }
    }
}
