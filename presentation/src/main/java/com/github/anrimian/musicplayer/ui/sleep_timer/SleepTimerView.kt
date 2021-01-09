package com.github.anrimian.musicplayer.ui.sleep_timer

import com.github.anrimian.musicplayer.domain.interactors.player.SleepTimerInteractor
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

interface SleepTimerView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showSleepTimerTime(sleepTimerTimeMillis: Long)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showSleepTimerState(sleepTimerState: SleepTimerInteractor.SleepTimerState)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showSleepRemainingSeconds(remainingSeconds: Long)

}