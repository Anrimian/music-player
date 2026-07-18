package com.github.anrimian.musicplayer.domain.interactors.sleep_timer

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.utils.coroutines.onSuccess
import com.github.anrimian.musicplayer.domain.utils.coroutines.tickerLongValueFlow
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.asObservable
import kotlin.time.Duration.Companion.seconds

const val NO_TIMER = -1L

class SleepTimerInteractor(
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    private val settingsRepository: SettingsRepository,
    private val appComputationScope: CoroutineScope,
) {

    private val timerCountDownFlow = MutableStateFlow(NO_TIMER)
    private val sleepTimerStateFlow = MutableStateFlow(SleepTimerState.DISABLED)

    private var timerJob: Job? = null
    private var remainingMillis: Long = 0

    fun start() {
        startSleepTimer(settingsRepository.sleepTimerTime)
        sleepTimerStateFlow.value = SleepTimerState.ENABLED
    }

    fun stop() {
        timerJob?.cancel()
        remainingMillis = 0L
        timerCountDownFlow.value = NO_TIMER
        sleepTimerStateFlow.value = SleepTimerState.DISABLED
    }

    fun pause() {
        timerJob?.cancel()
        sleepTimerStateFlow.value = SleepTimerState.PAUSED
    }

    fun resume() {
        startSleepTimer(remainingMillis)
        sleepTimerStateFlow.value = SleepTimerState.ENABLED
    }

    fun getSleepTimerCountDownObservable(): Observable<Long> = timerCountDownFlow.asObservable()

    fun getSleepTimerStateFlow(): Flow<SleepTimerState> = sleepTimerStateFlow

    fun setPlayLastSong(playLastSong: Boolean) {
        settingsRepository.isSleepTimerPlayLastSong = playLastSong
    }

    fun setSleepTimerTime(millis: Long) {
        if (sleepTimerStateFlow.value == SleepTimerState.ENABLED) {
            return
        }
        settingsRepository.sleepTimerTime = millis
    }

    fun getSleepTimerTime() = settingsRepository.sleepTimerTime

    private fun startSleepTimer(timeMillis: Long) {
        remainingMillis = timeMillis
        timerJob = libraryPlayerInteractor.getPlayerStateObservable().asFlow()
            .flatMapLatest { playerState ->
                if (playerState == PlayerState.PLAY) {
                    tickerLongValueFlow(remainingMillis, -1000L, 1.seconds, 1.seconds)
                        .onEach { millis -> remainingMillis = millis }
                } else {
                    flowOf(remainingMillis)
                }
            }
            .onStart { timerCountDownFlow.value = remainingMillis }
            .onEach { millis -> timerCountDownFlow.value = millis }
            .takeWhile { millis -> millis >= 0 }
            .onSuccess(::onTimerFinished)
            .launchIn(appComputationScope)
    }

    private fun onTimerFinished() {
        libraryPlayerInteractor.pause()
        timerCountDownFlow.value = NO_TIMER
        sleepTimerStateFlow.value = SleepTimerState.DISABLED
    }

}