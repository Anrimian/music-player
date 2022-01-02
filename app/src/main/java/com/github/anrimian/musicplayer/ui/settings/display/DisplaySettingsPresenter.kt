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
        viewState.showFileNameEnabled(interactor.isDisplayFileNameEnabled())
        viewState.showCoversChecked(interactor.isCoversEnabled())
        viewState.showCoversInNotificationChecked(interactor.isCoversInNotificationEnabled())
        viewState.showColoredNotificationChecked(interactor.isColoredNotificationEnabled())
        viewState.showNotificationCoverStubChecked(interactor.isNotificationCoverStubEnabled())
        viewState.showCoversOnLockScreenChecked(interactor.isCoversOnLockScreenEnabled())
        subscribeOnCoversEnabledState()
        subscribeOnNotificationCoverSettingsEnabledState()
    }

    fun onCoversChecked(checked: Boolean) {
        viewState.showCoversChecked(checked)
        interactor.setCoversEnabled(checked)
    }

    fun onFileNameChecked(checked: Boolean) {
        viewState.showFileNameEnabled(checked)
        interactor.setDisplayFileName(checked)
    }

    fun onCoversInNotificationChecked(checked: Boolean) {
        viewState.showCoversInNotificationChecked(checked)
        interactor.setCoversInNotificationEnabled(checked)
    }

    fun onColoredNotificationChecked(checked: Boolean) {
        viewState.showColoredNotificationChecked(checked)
        interactor.setColoredNotificationEnabled(checked)
    }

    fun onNotificationCoverStubChecked(checked: Boolean) {
        viewState.showNotificationCoverStubChecked(checked)
        interactor.setNotificationCoverStubEnabled(checked)
    }

    fun onCoversOnLockScreenChecked(checked: Boolean) {
        viewState.showCoversOnLockScreenChecked(checked)
        interactor.setCoversOnLockScreenEnabled(checked)
    }

    private fun subscribeOnNotificationCoverSettingsEnabledState() {
        Observable.combineLatest(interactor.getCoversEnabledObservable(),
                interactor.getCoversInNotificationEnabledObservable(),
                { covers, notification -> covers && notification })
                .unsafeSubscribeOnUi(this::onNotificationCoverSettingsEnabled)
    }

    private fun subscribeOnCoversEnabledState() {
        interactor.getCoversEnabledObservable().unsafeSubscribeOnUi(this::onCoversEnabled)
    }

    private fun onNotificationCoverSettingsEnabled(enabled: Boolean) {
        viewState.showColoredNotificationEnabled(enabled)
        viewState.showNotificationCoverStubEnabled(enabled)
    }

    private fun onCoversEnabled(enabled: Boolean) {
        viewState.showCoversInNotificationEnabled(enabled)
        viewState.showCoversOnLockScreenEnabled(enabled)
    }
}