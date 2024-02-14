package com.github.anrimian.musicplayer.ui.editor.batch

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import com.github.anrimian.musicplayer.ui.editor.common.performFilesChangeAction
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

abstract class BatchEditorPresenter<T: BatchEditorView>(
    private val initialValue: String,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
): AppPresenter<T>(uiScheduler, errorParser) {

    private var changeDisposable: Disposable? = null
    private var lastEditAction: Completable? = null

    private var filesCount = 0

    private var currentText = initialValue

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
            performEditAction(
                currentText,
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

    protected abstract fun performEditAction(
        currentText: String,
        affectedFilesCount: (Int) -> Unit,
        downloadingSubject: BehaviorSubject<Long>,
        editingSubject: BehaviorSubject<Long>
    ): Completable

    private fun showChangeAllowed() {
        viewState.showChangeAllowed(isChangeAllowed())
    }

    private fun isChangeAllowed() = currentText.isNotEmpty() && currentText != initialValue

}