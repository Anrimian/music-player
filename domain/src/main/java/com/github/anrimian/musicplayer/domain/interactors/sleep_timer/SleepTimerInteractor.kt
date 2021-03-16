package com.github.anrimian.musicplayer.domain.interactors.sleep_timer;

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

//TODO check android 5 and tablets
public class SleepTimerInteractor {

    public static final long NO_TIMER = -1;

    private final LibraryPlayerInteractor libraryPlayerInteractor;
    private final SettingsRepository settingsRepository;

    private final BehaviorSubject<Long> timerCountDownSubject = BehaviorSubject.createDefault(NO_TIMER);
    private final BehaviorSubject<SleepTimerState> sleepTimerStateSubject = BehaviorSubject.createDefault(SleepTimerState.DISABLED);

    private Disposable timerDisposable;

    private long remainingMillis;

    public SleepTimerInteractor(LibraryPlayerInteractor libraryPlayerInteractor,
                                SettingsRepository settingsRepository) {
        this.libraryPlayerInteractor = libraryPlayerInteractor;
        this.settingsRepository = settingsRepository;
    }

    public void start() {
        startSleepTimer(settingsRepository.getSleepTimerTime());
        sleepTimerStateSubject.onNext(SleepTimerState.ENABLED);
    }

    public void stop() {
        pause();
        remainingMillis = 0L;
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
        startSleepTimer(remainingMillis);
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
                .doOnComplete(this::onTimerFinished)
                .subscribe();
    }

    private void onTimerFinished() {
        libraryPlayerInteractor.pause();
        timerCountDownSubject.onNext(NO_TIMER);
        sleepTimerStateSubject.onNext(SleepTimerState.DISABLED);
    }

}
