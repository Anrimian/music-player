package com.github.anrimian.musicplayer.ui.settings.display

import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler

class DisplaySettingsPresenter(private val interactor: DisplaySettingsInteractor,
                               uiScheduler: Scheduler,
                               errorParser: ErrorParser
) : AppPresenter<DisplaySettingsView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showCoversChecked(interactor.isCoversEnabled)
        viewState.showCoversInNotificationChecked(interactor.isCoversInNotificationEnabled)
        viewState.showColoredNotificationChecked(interactor.isColoredNotificationEnabled)
        viewState.showCoversOnLockScreenChecked(interactor.isCoversOnLockScreenEnabled)
        subscribeOnCoversEnabledState()
        subscribeOnColoredNotificationEnabledState()
    }

    fun onCoversChecked(checked: Boolean) {
        viewState.showCoversChecked(checked)
        interactor.isCoversEnabled = checked
    }

    fun onCoversInNotificationChecked(checked: Boolean) {
        viewState.showCoversInNotificationChecked(checked)
        interactor.isCoversInNotificationEnabled = checked
    }

    fun onColoredNotificationChecked(checked: Boolean) {
        viewState.showColoredNotificationChecked(checked)
        interactor.isColoredNotificationEnabled = checked
    }

    fun onCoversOnLockScreenChecked(checked: Boolean) {
        viewState.showCoversOnLockScreenChecked(checked)
        interactor.isCoversOnLockScreenEnabled = checked
    }

    private fun subscribeOnColoredNotificationEnabledState() {
        Observable.combineLatest(interactor.coversEnabledObservable,
                interactor.coversInNotificationEnabledObservable,
                { covers, notification -> covers && notification })
                .unsafeSubscribeOnUi(viewState::showColoredNotificationEnabled)
    }

    private fun subscribeOnCoversEnabledState() {
        interactor.coversEnabledObservable.unsafeSubscribeOnUi(this::onCoversEnabled)
    }

    private fun onCoversEnabled(enabled: Boolean) {
        viewState.showCoversInNotificationEnabled(enabled)
        viewState.showShowCoversOnLockScreenEnabled(enabled)
    }
}