package com.github.anrimian.musicplayer.ui.sleep_timer

import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerState
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

interface SleepTimerView : MvpView {

    @AddToEndSingle
    fun showSleepTimerTime(sleepTimerTimeMillis: Long)

    @AddToEndSingle
    fun showSleepTimerState(state: SleepTimerState)

    @AddToEndSingle
    fun showRemainingTimeMillis(millis: Long)

}