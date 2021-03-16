package com.github.anrimian.musicplayer.ui.sleep_timer

import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class SleepTimerPresenter(private val interactor: SleepTimerInteractor,
                          scheduler: Scheduler,
                          errorParser: ErrorParser
) : AppPresenter<SleepTimerView>(scheduler, errorParser) {
    
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showSleepTimerTime(interactor.getSleepTimerTime())
        subscribeOnSleepTimerState()
        subscribeOnSleepTimerRemainingTime()
    }

    fun onSleepTimerTimeChanged(millis: Long) {
        interactor.setSleepTimerTime(millis)
    }

    fun onStartClicked() {
        interactor.start()
        viewState.showSleepTimerTime(interactor.getSleepTimerTime())
    }

    fun onResumeClicked() {
        interactor.resume()
    }

    fun onStopClicked() {
        interactor.stop()
    }

    fun onResetButtonClicked() {
        interactor.stop()
    }

    private fun subscribeOnSleepTimerState() {
        interactor.getSleepTimerStateObservable().unsafeSubscribeOnUi(viewState::showSleepTimerState)
    }

    private fun subscribeOnSleepTimerRemainingTime() {
        interactor.getSleepTimerCountDownObservable().unsafeSubscribeOnUi(viewState::showRemainingTimeMillis)
    }
}