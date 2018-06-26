package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CompositionEvent;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static com.github.anrimian.simplemusicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static io.reactivex.subjects.BehaviorSubject.create;

/**
 * Created on 18.11.2017.
 */

public class PlayQueueRepositoryImpl implements PlayQueueRepository {

    private static final int NO_POSITION = 0;

    private final PlayQueueDataSource playQueueDataSource;
    private final UiStatePreferences uiStatePreferences;
    private final Scheduler scheduler;

    private final BehaviorSubject<CompositionEvent> currentCompositionSubject = create();

    private int position = NO_POSITION;

    private Disposable currentCompositionChangeDisposable;

    public PlayQueueRepositoryImpl(PlayQueueDataSource playQueueDataSource,
                                   UiStatePreferences uiStatePreferences,
                                   Scheduler scheduler) {
        this.playQueueDataSource = playQueueDataSource;
        this.uiStatePreferences = uiStatePreferences;
        this.scheduler = scheduler;
    }

    @Override
    public Completable setPlayQueue(List<Composition> compositions) {
        checkCompositionsList(compositions);
        return playQueueDataSource.setPlayQueue(compositions)
                .doOnSuccess(playQueue -> {
                    position = 0;
                    updateCurrentComposition(playQueue, position);
                    subscribeOnCurrentCompositionChange();
                })
                .toCompletable()
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<CompositionEvent> getCurrentCompositionObservable() {
        return withDefaultValue(currentCompositionSubject, getSavedComposition())
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<List<Composition>> getPlayQueueObservable() {
        return playQueueDataSource.getPlayQueueObservable()
                .subscribeOn(scheduler);
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        CompositionEvent compositionEvent = currentCompositionSubject.getValue();
        if (compositionEvent == null) {
            throw new IllegalStateException("change play mode without current composition");
        }
        Composition composition = compositionEvent.getComposition();
        if (composition == null) {
            throw new IllegalStateException("change play mode with composition event: null");
        }

        playQueueDataSource.setRandomPlayingEnabled(enabled, compositionEvent.getComposition())
                .doOnSuccess(position -> {
                    this.position = position;
                    uiStatePreferences.setCurrentCompositionPosition(position);
                })
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public Single<Integer> skipToNext() {
        return playQueueDataSource.getPlayQueue()
                .map(playQueue -> {
                    if (playQueue.isEmpty()) {
                        return 0;
                    }

                    if (position >= playQueue.size() - 1) {
                        position = 0;
                    } else {
                        position++;
                    }
                    updateCurrentComposition(playQueue, position);
                    return position;
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Single<Integer> skipToPrevious() {
        return playQueueDataSource.getPlayQueue()
                .map(playQueue -> {
                    if (playQueue.isEmpty()) {
                        return 0;
                    }

                    position--;
                    if (position < 0) {
                        position = playQueue.size() - 1;
                    }
                    updateCurrentComposition(playQueue, position);
                    return position;
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable skipToPosition(int position) {
        return playQueueDataSource.getPlayQueue()
                .doOnSuccess(playQueue -> {
                    checkCompositionsList(playQueue);

                    if (position < 0 || position >= playQueue.size()) {
                        throw new IndexOutOfBoundsException("unexpected position: " + position);
                    }

                    this.position = position;
                    updateCurrentComposition(playQueue, position);
                })
                .toCompletable()
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<Change<List<Composition>>> getPlayQueueChangeObservable() {
        return playQueueDataSource.getChangeObservable()
                .subscribeOn(scheduler);
    }

    private void subscribeOnCurrentCompositionChange() {
        if (currentCompositionChangeDisposable == null) {
            currentCompositionChangeDisposable = playQueueDataSource.getChangeObservable()
                    .subscribeOn(scheduler)
                    .subscribe(this::processChangeForCurrentComposition);
        }
    }

    private void processChangeForCurrentComposition(Change<List<Composition>> change) {
        Composition currentComposition = currentCompositionSubject.getValue().getComposition();
        for (Composition changedComposition: change.getData()) {
            if (changedComposition.equals(currentComposition)) {
                processCurrentCompositionChange(changedComposition, change.getChangeType());
                return;
            }
        }
    }

    private void processCurrentCompositionChange(Composition changedComposition,
                                                 ChangeType changeType) {
        switch (changeType) {
            case DELETED: {
                List<Composition> playQueue = playQueueDataSource.getPlayQueue().blockingGet();
                if (playQueue.isEmpty()) {
                    currentCompositionSubject.onNext(new CompositionEvent());
                } else {
                    if (position >= playQueue.size()) {
                        position = 0;
                    }
                    updateCurrentComposition(playQueue, position);
                }
                break;
            }
            case MODIFY: {
                currentCompositionSubject.onNext(new CompositionEvent(changedComposition, position));
                break;
            }
        }
    }

    private void updateCurrentComposition(List<Composition> currentPlayList, int position) {
        Composition composition = currentPlayList.get(position);
        if (composition == null) {
            System.out.println("wtf");//TODO fix and remove
        }
        uiStatePreferences.setCurrentCompositionId(composition.getId());
        uiStatePreferences.setCurrentCompositionPosition(position);

        CompositionEvent compositionEvent = new CompositionEvent(composition, position, 0);
        currentCompositionSubject.onNext(compositionEvent);
    }

    private Maybe<CompositionEvent> getSavedComposition() {
        return playQueueDataSource.getPlayQueue()
                .flatMapMaybe(this::findSavedComposition)
                .doOnSuccess(currentComposition -> subscribeOnCurrentCompositionChange())
                .subscribeOn(scheduler);
    }

    @Nullable
    private Maybe<CompositionEvent> findSavedComposition(List<Composition> compositions) {
        return Maybe.create(emitter -> {
            long id = uiStatePreferences.getCurrentCompositionId();
            int position = uiStatePreferences.getCurrentCompositionPosition();

            //optimized way
            if (position > 0 && position < compositions.size()) {
                Composition expectedComposition = compositions.get(position);
                if (expectedComposition.getId() == id) {
                    this.position = position;
                    emitter.onSuccess(new CompositionEvent(expectedComposition,
                            position,
                            uiStatePreferences.getTrackPosition()));
                    return;
                }
            }

            if (id == NO_COMPOSITION) {
                emitter.onComplete();
                return;
            }

            for (int i = 0; i< compositions.size(); i++) {
                Composition composition = compositions.get(i);
                if (composition.getId() == id) {
                    this.position = i;
                    emitter.onSuccess(new CompositionEvent(composition,
                            position,
                            uiStatePreferences.getTrackPosition()));
                    return;
                }
            }
            emitter.onComplete();
        });
    }

    private void checkCompositionsList(@Nullable List<Composition> compositions) {
        if (compositions == null || compositions.isEmpty()) {
            throw new IllegalStateException("empty play queue");
        }
    }
}
