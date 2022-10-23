package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable

class LyricsPresenter(
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    private val editorInteractor: EditorInteractor,
    errorParser: ErrorParser,
    uiScheduler: Scheduler
): AppPresenter<LyricsView>(uiScheduler, errorParser) {

    private var changeDisposable: Disposable? = null

    private var currentLyrics: String? = null
    private var lastEditAction: Completable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        libraryPlayerInteractor.getCurrentQueueItemObservable()
            .unsafeSubscribeOnUi { onCurrentQueueItemChanged() }
        libraryPlayerInteractor.getCurrentCompositionLyrics()
            .unsafeSubscribeOnUi(this::onLyricsReceived)
    }

    fun onEditLyricsClicked() {
        if (currentLyrics != null) {
            viewState.showEnterLyricsDialog(currentLyrics!!)
        }
    }

    fun onNewLyricsEntered(text: String?) {
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = getCurrentCompositionId()
            .flatMapCompletable { id -> editorInteractor.editCompositionLyrics(id, text) }
            .observeOn(uiScheduler)
            .doOnSubscribe { viewState.showChangeFileProgress() }
            .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
    }

    fun onRetryFailedEditActionClicked() {
        if (lastEditAction != null) {
            RxUtils.dispose(changeDisposable, presenterDisposable)
            changeDisposable = lastEditAction!!
                .doFinally { lastEditAction = null }
                .subscribe({}, this::onDefaultError, presenterDisposable)
        }
    }

    private fun onLyricsReceived(lyrics: Optional<String>) {
        currentLyrics = lyrics.value
        viewState.showLyrics(currentLyrics)
    }

    private fun onCurrentQueueItemChanged() {
        viewState.resetTextPosition()
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun getCurrentCompositionId(): Single<Long> {
        return libraryPlayerInteractor.getCurrentCompositionObservable()
            .flatMap { currentComposition ->
                val composition = currentComposition.composition
                if (composition != null) {
                    Observable.fromCallable { composition.id }
                } else {
                    Observable.empty()
                }
            }.firstOrError()
    }
}