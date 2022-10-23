package com.github.anrimian.musicplayer.ui.common.dialogs.share

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.filesync.models.state.file.Downloading
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ShareCompositionsPresenter(
    private val ids: LongArray,
    private val sourceInteractor: CompositionSourceInteractor,
    private val syncInteractor: SyncInteractor<*, *, Long>,
    uiScheduler: Scheduler,
    errorParser: ErrorParser,
): AppPresenter<ShareCompositionsView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showShareError(null)
        prepareFiles()
    }

    fun onTryAgainClicked() {
        viewState.showShareError(null)
        prepareFiles()
    }

    private fun prepareFiles() {
        val currentFileIdSubject = BehaviorSubject.create<Long>()
        var preparedCount = 0
        val disposable = currentFileIdSubject
            .switchMap { id ->
                syncInteractor.getFileSyncStateObservable(id)
                    .observeOn(uiScheduler)
                    .doOnSubscribe { viewState.showProcessedFileCount(++preparedCount, ids.size) }
            }
            .subscribe { fileSyncState ->
                if (fileSyncState is Downloading) {
                    viewState.showDownloadingFileInfo(fileSyncState.getProgress())
                }
            }
        sourceInteractor.getLibraryCompositionSources(ids.asIterable(), currentFileIdSubject)
            .doFinally { disposable.dispose() }
            .runOnUi(viewState::showShareDialog, viewState::showShareError)
    }

}