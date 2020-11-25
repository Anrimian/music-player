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
        //set enabled state
        //start timer if present
    }

    public void stop() {
        //set disabled state
        //cancel timer
    }

    public void onCompositionPlayFinished() {
        //if enabled
        //decrease counter
        //stop if counter is 0 OR flag to stop is enabled
    }

    public void setFinishTime() {
        //save finish time
    }

    public void setCompositionToFinishCount() {
        //save count
    }

    public void setPlayLastSong(boolean playLastSong) {
        settingsRepository.setSleepTimerPlayLastSong(playLastSong);
    }

    public void setSleepTimerTime(long millis) {
        settingsRepository.setSleepTimerTime(millis);
    }

    private void onTimerFinished() {
        //if finish play last song is enabled -> set flag to stop
        //else stop
    }

    private void startPromoteScreenCountDownTimer() {
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            return;
        }
        long startTime = uiStateRepository.getSleepTimerStartTime();
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
            uiStateRepository.setSleepTimerStartTimer(startTime);
        }

        long remainingSeconds = ((startTime - System.currentTimeMillis()) + settingsRepository.getSleepTimerTime()) / 1000L;
        if (remainingSeconds <= 0) {
            return;
        }

        timerDisposable = Observable.interval( 1, TimeUnit.SECONDS)
                .map(seconds -> remainingSeconds - seconds)
                .doOnNext(timerCountDownSubject::onNext)
                .takeUntil(seconds -> seconds <= 0)
                .doOnComplete(() -> {
                    //stop music
                    //disable timer
                })
                .subscribe();
    }
}
