package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.models.exceptions.CompositionNotFoundException;
import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CurrentComposition;
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
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static com.github.anrimian.simplemusicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.DELETED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.MODIFY;
import static io.reactivex.subjects.BehaviorSubject.create;

/**
 * Created on 18.11.2017.
 */

public class PlayQueueRepositoryImpl implements PlayQueueRepository {

    private static final int NO_POSITION = 0;

    private final PlayQueueDataSource playQueueDataSource;
    private final UiStatePreferences uiStatePreferences;
    private final Scheduler dbScheduler;

    private final BehaviorSubject<CurrentComposition> currentCompositionSubject = create();

    private final PublishSubject<Change<Composition>> currentCompositionChangeSubject
            = PublishSubject.create();

    private int position = NO_POSITION;

    private Disposable currentCompositionChangeDisposable;

    public PlayQueueRepositoryImpl(PlayQueueDataSource playQueueDataSource,
                                   UiStatePreferences uiStatePreferences,
                                   Scheduler dbScheduler) {
        this.playQueueDataSource = playQueueDataSource;
        this.uiStatePreferences = uiStatePreferences;
        this.dbScheduler = dbScheduler;
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
                .subscribeOn(dbScheduler);
    }

    @Override
    public Observable<CurrentComposition> getCurrentCompositionObservable() {
        return withDefaultValue(currentCompositionSubject, getSavedComposition())
                .subscribeOn(dbScheduler);
    }

    @Override
    public Single<CurrentComposition> getCurrentComposition() {
        return getCurrentCompositionObservable()
                .lastOrError()
                .onErrorResumeNext(Single.error(new CompositionNotFoundException()));
    }

    @Override
    public Observable<List<Composition>> getPlayQueueObservable() {
        return playQueueDataSource.getPlayQueueObservable();
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        CurrentComposition currentComposition = currentCompositionSubject.getValue();
        if (currentComposition == null) {
            throw new IllegalStateException("change play mode without current composition");
        }

        playQueueDataSource.setRandomPlayingEnabled(enabled, currentComposition.getComposition())
                .doOnSuccess(position -> {
                    this.position = position;
                    uiStatePreferences.setCurrentCompositionPosition(position);
                })
                .subscribe();
    }

    @Override
    public Single<Integer> skipToNext() {
        return playQueueDataSource.getPlayQueue()
                .map(currentPlayList -> {
                    checkCompositionsList(currentPlayList);

                    if (position >= currentPlayList.size() - 1) {
                        position = 0;
                    } else {
                        position++;
                    }
                    updateCurrentComposition(currentPlayList, position);
                    return position;
                });
    }

    @Override
    public Single<Integer> skipToPrevious() {
        return playQueueDataSource.getPlayQueue()
                .map(currentPlayList -> {
                    checkCompositionsList(currentPlayList);

                    position--;
                    if (position < 0) {
                        position = currentPlayList.size() - 1;
                    }
                    updateCurrentComposition(currentPlayList, position);
                    return position;
                });
    }

    @Override
    public Completable skipToPosition(int position) {
        return playQueueDataSource.getPlayQueue()
                .doOnSuccess(currentPlayList -> {
                    checkCompositionsList(currentPlayList);

                    if (position < 0 || position >= currentPlayList.size()) {
                        throw new IndexOutOfBoundsException("unexpected position: " + position);
                    }

                    this.position = position;
                    updateCurrentComposition(currentPlayList, position);
                })
                .toCompletable();
    }

    @Override
    public Observable<Change<List<Composition>>> getPlayQueueChangeObservable() {
        return playQueueDataSource.getChangeObservable();
    }

    @Override
    public Observable<Change<Composition>> getCurrentCompositionChangeObservable() {
        return currentCompositionChangeSubject;
    }

    private void subscribeOnCurrentCompositionChange() {
        if (currentCompositionChangeDisposable == null) {
            currentCompositionChangeDisposable = playQueueDataSource.getChangeObservable()
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
                    currentCompositionChangeSubject.onNext(new Change<>(DELETED, changedComposition));
                } else {
                    if (position >= playQueue.size()) {
                        position = 0;
                    }
                    updateCurrentComposition(playQueue, position);
                }
                break;
            }
            case MODIFY: {
                currentCompositionChangeSubject.onNext(new Change<>(MODIFY, changedComposition));
                break;
            }
        }
    }

    private void updateCurrentComposition(List<Composition> currentPlayList, int position) {
        Composition composition = currentPlayList.get(position);
        uiStatePreferences.setCurrentCompositionId(composition.getId());
        uiStatePreferences.setCurrentCompositionPosition(position);

        CurrentComposition currentComposition = new CurrentComposition(composition, position, 0);
        currentCompositionSubject.onNext(currentComposition);
    }

    private Maybe<CurrentComposition> getSavedComposition() {
        return playQueueDataSource.getPlayQueue()
                .flatMapMaybe(this::findSavedComposition)
                .doOnSuccess(currentComposition -> subscribeOnCurrentCompositionChange());
    }

    @Nullable
    private Maybe<CurrentComposition> findSavedComposition(List<Composition> compositions) {
        return Maybe.create(emitter -> {
            long id = uiStatePreferences.getCurrentCompositionId();
            int position = uiStatePreferences.getCurrentCompositionPosition();

            //optimized way
            if (position > 0 && position < compositions.size()) {
                Composition expectedComposition = compositions.get(position);
                if (expectedComposition.getId() == id) {
                    this.position = position;
                    emitter.onSuccess(new CurrentComposition(expectedComposition,
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
                    emitter.onSuccess(new CurrentComposition(composition,
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
