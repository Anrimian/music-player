package com.github.anrimian.musicplayer.ui.widgets.menu

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class WidgetMenuPresenter(
    private val interactor: LibraryPlayerInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
): AppPresenter<WidgetMenuView>(uiScheduler, errorParser) {

    private var compositionDisposable: Disposable? = null

    private lateinit var composition: Composition

    private var lastDeleteAction: Completable? = null

    fun onCompositionIdReceived(id: Long) {
        compositionDisposable?.dispose()
        compositionDisposable = interactor.getCompositionObservable(id)
            .observeOn(uiScheduler)
            .subscribe(
                this::onCompositionReceived,
                this::onCompositionReceivingError,
                viewState::closeScreen,
                presenterDisposable
            )
    }

    fun onShareCompositionClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.shareComposition(composition)
        viewState.closeScreen()
    }

    fun onDeleteCompositionClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.showConfirmDeleteDialog(composition)
    }

    fun onDeleteCompositionsDialogConfirmed(composition: Composition) {
        lastDeleteAction = interactor.deleteComposition(composition)
            .observeOn(uiScheduler)
            .doOnSuccess(this::onDeleteCompositionsSuccess)
            .ignoreElement()
        lastDeleteAction!!.justSubscribe(this::onDeleteCompositionError)
    }

    fun onRetryFailedDeleteActionClicked() {
        if (lastDeleteAction != null) {
            lastDeleteAction!!
                .doFinally { lastDeleteAction = null }
                .subscribe({}, this::onDeleteCompositionError)
        }
    }

    private fun onDeleteCompositionsSuccess(compositionsToDelete: DeletedComposition) {
        viewState.showDeleteCompositionMessage(compositionsToDelete)
        viewState.closeScreen()
    }

    private fun onDeleteCompositionError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showDeleteCompositionError(errorCommand)
    }

    private fun onCompositionReceived(composition: Composition) {
        this.composition = composition
        viewState.showComposition(composition)
    }

    private fun onCompositionReceivingError(throwable: Throwable) {
        viewState.showCompositionError(errorParser.parseError(throwable))
    }
}