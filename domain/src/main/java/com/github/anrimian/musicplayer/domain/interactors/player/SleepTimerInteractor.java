package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

//fade out when timer is finishing(?)
//states: enable/disable/disable_wait_for_finish
public class SleepTimerInteractor {

//    private final LibraryPlayerInteractor libraryPlayerInteractor;

    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;

    private final BehaviorSubject<Long> timerCountDownSubject = BehaviorSubject.create();

    private Disposable timerDisposable;

    public SleepTimerInteractor(SettingsRepository settingsRepository,
                                UiStateRepository uiStateRepository) {
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
    }

    public void start() {
        startSleepTimer(settingsRepository.getSleepTimerTime());
    }

    public void stop() {
        pause();
        uiStateRepository.setSleepTimerRemainingMillis(0L);
    }

    public void pause() {
        if (timerDisposable != null) {
            timerDisposable.dispose();
        }
    }

    public void resume() {
        startSleepTimer(uiStateRepository.getSleepTimerRemainingMillis());
    }

    public void setPlayLastSong(boolean playLastSong) {
        settingsRepository.setSleepTimerPlayLastSong(playLastSong);
    }

    public void setSleepTimerTime(long millis) {
        settingsRepository.setSleepTimerTime(millis);
    }

    private void startSleepTimer(long timeMillis) {
        long remainingSeconds = timeMillis / 1000L;
        timerDisposable = Observable.interval( 1, TimeUnit.SECONDS)
                .map(seconds -> remainingSeconds - seconds)
                .doOnNext(timerCountDownSubject::onNext)
                .takeUntil(seconds -> seconds <= 0)
                .doOnDispose(() -> uiStateRepository.setSleepTimerRemainingMillis(remainingSeconds))
                .doOnComplete(this::onTimerFinished)
                .subscribe();
    }

    private void onTimerFinished() {
        //stop music
        //disable timer
    }
}
