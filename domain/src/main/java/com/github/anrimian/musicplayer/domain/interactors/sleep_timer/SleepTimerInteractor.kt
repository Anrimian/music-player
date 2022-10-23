package com.github.anrimian.musicplayer.domain.interactors.sleep_timer

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

const val NO_TIMER = -1L

class SleepTimerInteractor(
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    private val settingsRepository: SettingsRepository
) {

    private val timerCountDownSubject = BehaviorSubject.createDefault(NO_TIMER)
    private val sleepTimerStateSubject = BehaviorSubject.createDefault(SleepTimerState.DISABLED)

    private var timerDisposable: Disposable? = null
    private var remainingMillis: Long = 0

    fun start() {
        startSleepTimer(settingsRepository.sleepTimerTime)
        sleepTimerStateSubject.onNext(SleepTimerState.ENABLED)
    }

    fun stop() {
        timerDisposable?.dispose()
        remainingMillis = 0L
        timerCountDownSubject.onNext(NO_TIMER)
        sleepTimerStateSubject.onNext(SleepTimerState.DISABLED)
    }

    fun pause() {
        timerDisposable?.dispose()
        sleepTimerStateSubject.onNext(SleepTimerState.PAUSED)
    }

    fun resume() {
        startSleepTimer(remainingMillis)
        sleepTimerStateSubject.onNext(SleepTimerState.ENABLED)
    }

    fun getSleepTimerCountDownObservable(): Observable<Long> = timerCountDownSubject

    fun getSleepTimerStateObservable(): Observable<SleepTimerState> = sleepTimerStateSubject

    fun setPlayLastSong(playLastSong: Boolean) {
        settingsRepository.isSleepTimerPlayLastSong = playLastSong
    }

    fun setSleepTimerTime(millis: Long) {
        if (sleepTimerStateSubject.value == SleepTimerState.ENABLED) {
            return
        }
        settingsRepository.sleepTimerTime = millis
    }

    fun getSleepTimerTime() = settingsRepository.sleepTimerTime

    private fun startSleepTimer(timeMillis: Long) {
        remainingMillis = timeMillis
        timerDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .map {
                    remainingMillis -= 1000L
                    return@map remainingMillis
                }
                .doOnSubscribe { timerCountDownSubject.onNext(remainingMillis) }
                .doOnNext(timerCountDownSubject::onNext)
                .takeUntil { millis: Long -> millis < 0 }
                .doOnComplete(this::onTimerFinished)
                .subscribe()
    }

    private fun onTimerFinished() {
        libraryPlayerInteractor.pause()
        timerCountDownSubject.onNext(NO_TIMER)
        sleepTimerStateSubject.onNext(SleepTimerState.DISABLED)
    }

}