package com.github.anrimian.musicplayer.ui.sleep_timer

import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class SleepTimerPresenter(
    private val interactor: SleepTimerInteractor,
    scheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<SleepTimerView>(scheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showSleepTimerTime(interactor.getSleepTimerTime())
        interactor.getSleepTimerStateFlow().subscribe(onNext = viewState::showSleepTimerState)
        interactor.getSleepTimerCountDownObservable().unsafeSubscribeOnUi(viewState::showRemainingTimeMillis)
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

}