package com.github.anrimian.musicplayer.ui.sleep_timer

import com.github.anrimian.musicplayer.domain.interactors.player.SleepTimerInteractor
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

interface SleepTimerView : MvpView {

    @AddToEndSingle
    fun showSleepTimerTime(sleepTimerTimeMillis: Long)

    @AddToEndSingle
    fun showSleepTimerState(state: SleepTimerInteractor.SleepTimerState)

    @AddToEndSingle
    fun showRemainingTimeMillis(millis: Long)

}