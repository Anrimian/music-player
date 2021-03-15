package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

//fade out when timer is finishing(?)
//handle 'play last song' option
//states: enable/disable/paused/disable_wait_for_finish
//TODO we need to save remaining millis into preferences?
//TODO dialog design
//TODO time pickers design
//TODO test time pickers
//TODO etSeconds - action doesn't clear focus or even called
//TODO check android 5 and tablets
//TODO split screen: dialog buttons(timer and speed)
public class SleepTimerInteractor {

    public static final long NO_TIMER = -1;

    private final LibraryPlayerInteractor libraryPlayerInteractor;
    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;

    private final BehaviorSubject<Long> timerCountDownSubject = BehaviorSubject.createDefault(NO_TIMER);
    private final BehaviorSubject<SleepTimerState> sleepTimerStateSubject = BehaviorSubject.createDefault(SleepTimerState.DISABLED);

    private Disposable timerDisposable;

    private long remainingMillis;

    public SleepTimerInteractor(LibraryPlayerInteractor libraryPlayerInteractor,
                                SettingsRepository settingsRepository,
                                UiStateRepository uiStateRepository) {
        this.libraryPlayerInteractor = libraryPlayerInteractor;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
    }

    public void start() {
        startSleepTimer(settingsRepository.getSleepTimerTime());
        sleepTimerStateSubject.onNext(SleepTimerState.ENABLED);
    }

    public void stop() {
        pause();
        uiStateRepository.setSleepTimerRemainingMillis(0L);
        timerCountDownSubject.onNext(NO_TIMER);
        sleepTimerStateSubject.onNext(SleepTimerState.DISABLED);
    }

    public void pause() {
        if (timerDisposable != null) {
            timerDisposable.dispose();
        }
        sleepTimerStateSubject.onNext(SleepTimerState.PAUSED);
    }

    public void resume() {
        startSleepTimer(uiStateRepository.getSleepTimerRemainingMillis());
        sleepTimerStateSubject.onNext(SleepTimerState.ENABLED);
    }

    public Observable<Long> getSleepTimerCountDownObservable() {
        return timerCountDownSubject;
    }

    public Observable<SleepTimerState> getSleepTimerStateObservable() {
        return sleepTimerStateSubject;
    }

    public void setPlayLastSong(boolean playLastSong) {
        settingsRepository.setSleepTimerPlayLastSong(playLastSong);
    }

    public void setSleepTimerTime(long millis) {
        if (sleepTimerStateSubject.getValue() == SleepTimerState.ENABLED) {
            return;
        }
        settingsRepository.setSleepTimerTime(millis);
    }

    public long getSleepTimerTime() {
        return settingsRepository.getSleepTimerTime();
    }

    private void startSleepTimer(long timeMillis) {
        remainingMillis = timeMillis;
        timerDisposable = Observable.interval( 1, TimeUnit.SECONDS)
                .map(seconds -> remainingMillis -= 1000)
                .doOnSubscribe(d -> timerCountDownSubject.onNext(remainingMillis))
                .doOnNext(timerCountDownSubject::onNext)
                .takeUntil(millis -> millis < 0)
                .doOnDispose(() -> uiStateRepository.setSleepTimerRemainingMillis(remainingMillis))
                .doOnComplete(this::onTimerFinished)
                .subscribe();
    }

    private void onTimerFinished() {
        libraryPlayerInteractor.pause();
        timerCountDownSubject.onNext(NO_TIMER);
        sleepTimerStateSubject.onNext(SleepTimerState.DISABLED);
    }

    public enum SleepTimerState {
        ENABLED,
        DISABLED,
        PAUSED
    }
}
