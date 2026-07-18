package com.github.anrimian.musicplayer.ui.common.dialogs.missing

import com.github.anrimian.musicplayer.domain.interactors.library.MissingFilesInteractor
import com.github.anrimian.musicplayer.domain.models.composition.AudioFileInfo
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class MissingFilesPresenter(
    private val interactor: MissingFilesInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser,
): AppPresenter<MissingFilesView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        interactor.getMissingAudioFilesFlow().subscribe(onNext = viewState::showMissingAudioFiles)
        interactor.getRestoreInProgressFlow().subscribe(onNext = viewState::showRestoreInProgress)
        interactor.getRestoreFilesErrorsFlow().subscribe(onNext = this::onRestoreErrorsReceived)
    }

    fun onDeleteMissingCompositionsClicked(files: List<AudioFileInfo>) {
        viewState.showConfirmDeleteMissingCompositionsDialog(files)
    }

    fun onDeleteMissingCompositionsConfirmed() {
        launch(onError = viewState::showErrorMessage) {
            val compositions = interactor.deleteMissingCompositions()
            viewState.onMissingCompositionsDeleted(compositions)
        }
    }

    fun onRestoreMissingCompositionsClicked(files: List<AudioFileInfo>) {
        interactor.launchRestoreMissingCompositions(files)
    }

    private fun onRestoreErrorsReceived(errors: Map<AudioFileInfo, Throwable>) {
        viewState.showRestoreErrors(
            errors.mapValues { (_, throwable) -> errorParser.parseError(throwable) }
        )
    }

}