package com.github.anrimian.musicplayer.ui.settings.library

import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class LibrarySettingsPresenter(private val librarySettingsInteractor: LibrarySettingsInteractor,
                               uiScheduler: Scheduler,
                               errorParser: ErrorParser
): AppPresenter<LibrarySettingsView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        librarySettingsInteractor.getAppConfirmDeleteDialogEnabledObservable()
            .unsafeSubscribeOnUi(viewState::showAppConfirmDeleteDialogEnabled)
        librarySettingsInteractor.getShowAllAudioFilesEnabledObservable()
            .unsafeSubscribeOnUi(viewState::showAllAudioFilesEnabled)
        librarySettingsInteractor.geAudioFileMinDurationMillisObservable()
            .unsafeSubscribeOnUi(viewState::showAudioFileMinDurationMillis)
    }

    fun doNotAppConfirmDialogChecked(isChecked: Boolean) {
        librarySettingsInteractor.setAppConfirmDeleteDialogEnabled(!isChecked)
    }

    fun onShowAllAudioFilesChecked(isChecked: Boolean) {
        librarySettingsInteractor.setShowAllAudioFilesEnabled(isChecked)
    }

    fun onAudioFileMinDurationMillisPicked(millis: Long) {
        librarySettingsInteractor.setAudioFileMinDurationMillis(millis)
    }

    fun onSelectMinDurationClicked() {
        viewState.showSelectMinAudioDurationDialog(
            librarySettingsInteractor.getAudioFileMinDurationMillis()
        )
    }

}