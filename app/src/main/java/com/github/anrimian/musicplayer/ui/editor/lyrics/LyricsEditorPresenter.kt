package com.github.anrimian.musicplayer.ui.editor.lyrics

import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class LyricsEditorPresenter(
    private val compositionId: Long,
    private val editorInteractor: EditorInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<LyricsEditorView>(uiScheduler, errorParser) {

    private var changeDisposable: Disposable? = null
    private var lastEditAction: Completable? = null

    private var initialText: String? = null
    private var currentText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadLyrics()
    }

    fun onTryAgainButtonClicked() {
        loadLyrics()
    }

    fun onTextChanged(text: String) {
        if (currentText != text) {
            currentText = text
        }
    }

    fun onChangeButtonClicked() {
        if (currentText == initialText) {
            viewState.closeScreen()
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.editCompositionLyrics(compositionId, currentText)
            .observeOn(uiScheduler)
            .doOnSubscribe { viewState.showChangeFileProgress() }
            .doFinally { viewState.hideChangeFileProgress() }
            .doOnComplete { viewState.closeScreen() }
        changeDisposable = lastEditAction!!.subscribe(viewState::showErrorMessage)
    }

    fun onRetryFailedEditActionClicked() {
        if (lastEditAction != null) {
            RxUtils.dispose(changeDisposable, presenterDisposable)
            changeDisposable = lastEditAction!!
                .doFinally { lastEditAction = null }
                .subscribe(viewState::showErrorMessage)
        }
    }

    private fun loadLyrics() {
        editorInteractor.getCompositionLyrics(compositionId)
            .runOnUi(this::onLyricsReceived, viewState::showLyricsLoadingError)
    }

    private fun onLyricsReceived(text: String) {
        initialText = text
        currentText = text
        viewState.showLyrics(text)
    }

}