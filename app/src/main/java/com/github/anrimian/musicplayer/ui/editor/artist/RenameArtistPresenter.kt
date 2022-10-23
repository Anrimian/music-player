package com.github.anrimian.musicplayer.ui.editor.artist

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import com.github.anrimian.musicplayer.ui.editor.common.performFilesChangeAction
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class RenameArtistPresenter(
    private val artistId: Long,
    private val initialName: String,
    private val editorInteractor: EditorInteractor,
    private val syncInteractor: SyncInteractor<*, *, Long>,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
): AppPresenter<RenameArtistView>(uiScheduler, errorParser) {

    private var changeDisposable: Disposable? = null
    private var lastEditAction: Completable? = null

    private var filesCount = 0

    private var currentText = initialName

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showInputState()
        showChangeAllowed()
    }

    fun onChangeButtonClicked() {
        if (!isChangeAllowed()) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = performFilesChangeAction(
            syncInteractor,
            uiScheduler,
            { preparedCount -> viewState.showPreparedFilesCount(preparedCount, filesCount) },
            { progressInfo -> viewState.showDownloadingFileInfo(progressInfo) },
            { editedCount -> viewState.showEditedFilesCount(editedCount, filesCount) }
        ) { downloadingSubject, editingSubject ->
            editorInteractor.updateArtistName(
                currentText,
                artistId,
                { filesCount -> this.filesCount = filesCount },
                downloadingSubject,
                editingSubject
            )
        }.doOnSubscribe { viewState.showProgress() }
        changeDisposable = lastEditAction!!.subscribe(viewState::closeScreen, viewState::showError)
    }

    fun onRetryFailedEditActionClicked() {
        if (lastEditAction != null) {
            lastEditAction!!.doFinally { lastEditAction = null }
                .subscribe(viewState::closeScreen, viewState::showError)
        }
    }

    fun onInputTextChanged(text: String) {
        currentText = text.trim()
        showChangeAllowed()
    }

    private fun showChangeAllowed() {
        viewState.showChangeAllowed(isChangeAllowed())
    }

    private fun isChangeAllowed() = currentText.isNotEmpty() && currentText != initialName

}