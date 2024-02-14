package com.github.anrimian.musicplayer.ui.settings.library

import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class LibrarySettingsPresenter(
    private val interactor: LibrarySettingsInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
): AppPresenter<LibrarySettingsView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        interactor.getAppConfirmDeleteDialogEnabledObservable()
            .unsafeSubscribeOnUi(viewState::showAppConfirmDeleteDialogEnabled)
        interactor.getShowAllAudioFilesEnabledObservable()
            .unsafeSubscribeOnUi(viewState::showAllAudioFilesEnabled)
        interactor.geAudioFileMinDurationMillisObservable()
            .unsafeSubscribeOnUi(viewState::showAudioFileMinDurationMillis)
        interactor.getPlaylistDuplicateCheckObservable()
            .unsafeSubscribeOnUi(viewState::showPlaylistDuplicateCheckEnabled)
        viewState.showPlaylistInsertStartEnabled(interactor.isPlaylistInsertStartEnabled())
    }

    fun doNotAppConfirmDialogChecked(isChecked: Boolean) {
        interactor.setAppConfirmDeleteDialogEnabled(!isChecked)
    }

    fun onShowAllAudioFilesChecked(isChecked: Boolean) {
        interactor.setShowAllAudioFilesEnabled(isChecked)
    }

    fun onAudioFileMinDurationMillisPicked(millis: Long) {
        interactor.setAudioFileMinDurationMillis(millis)
    }

    fun onPlaylistInsertStartChecked(isChecked: Boolean) {
        interactor.setPlaylistInsertStartEnabled(isChecked)
        viewState.showPlaylistInsertStartEnabled(isChecked)
    }

    fun onPlaylistDuplicateCheckChecked(isChecked: Boolean) {
        interactor.setPlaylistDuplicateCheckEnabled(isChecked)
        viewState.showPlaylistDuplicateCheckEnabled(isChecked)
    }

    fun onSelectMinDurationClicked() {
        viewState.showSelectMinAudioDurationDialog(
            interactor.getAudioFileMinDurationMillis()
        )
    }

}