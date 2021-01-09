package com.github.anrimian.musicplayer.ui.sleep_timer

import com.github.anrimian.musicplayer.domain.interactors.player.SleepTimerInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class SleepTimerPresenter(private val interactor: SleepTimerInteractor,
                          scheduler: Scheduler,
                          errorParser: ErrorParser
) : AppPresenter<SleepTimerView>(scheduler, errorParser) {
    
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showSleepTimerTime(interactor.sleepTimerTime)
        subscribeOnSleepTimerState()
        subscribeOnSleepTimerRemainingTime()
    }

    fun onSleepTimerTimeChanged(millis: Long) {
        viewState.showSleepTimerTime(millis)
        interactor.sleepTimerTime = millis
    }

    private fun subscribeOnSleepTimerState() {
        interactor.sleepTimerStateObservable.unsafeSubscribeOnUi(viewState::showSleepTimerState)
    }

    private fun subscribeOnSleepTimerRemainingTime() {
        interactor.sleepTimerCountDownObservable.unsafeSubscribeOnUi(viewState::showSleepRemainingSeconds)
    }
}