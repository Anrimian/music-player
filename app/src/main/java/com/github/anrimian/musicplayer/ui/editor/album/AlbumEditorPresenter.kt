package com.github.anrimian.musicplayer.ui.editor.album

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import com.github.anrimian.musicplayer.ui.editor.common.performFilesChangeAction
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class AlbumEditorPresenter(
    private val albumId: Long,
    private val editorInteractor: EditorInteractor,
    private val syncInteractor: SyncInteractor<*, *, Long>,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<AlbumEditorView>(uiScheduler, errorParser) {

    private var album: Album? = null

    private var changeDisposable: Disposable? = null
    private var lastEditAction: Completable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadAlbum()
    }

    fun onChangeAuthorClicked() {
        if (album == null) {
            return
        }
        editorInteractor.getAuthorNames()
            .observeOn(uiScheduler)
            .doOnSuccess { artists -> viewState.showEnterAuthorDialog(album!!, artists) }
            .doOnError { throwable ->
                viewState.showEnterAuthorDialog(album!!, null)
                onDefaultError(throwable)
            }
            .ignoreElement()
            .onErrorComplete()
            .subscribe()
    }

    fun onChangeNameClicked() {
        if (album == null) {
            return
        }
        viewState.showEnterNameDialog(album!!)
    }

    fun onNewAuthorEntered(author: String?) {
        performChangeAction { downloadingSubject, editingSubject ->
            editorInteractor.updateAlbumArtist(author, albumId, downloadingSubject, editingSubject)
        }
    }

    fun onNewNameEntered(name: String?) {
        performChangeAction { downloadingSubject, editingSubject ->
            editorInteractor.updateAlbumName(name, albumId, downloadingSubject, editingSubject)
        }
    }

    fun onRetryFailedEditActionClicked() {
        if (lastEditAction != null) {
            RxUtils.dispose(changeDisposable, presenterDisposable)
            changeDisposable = lastEditAction!!
                .doFinally { lastEditAction = null }
                .subscribe(viewState::showErrorMessage)
        }
    }

    fun onEditActionCancelled() {
        changeDisposable?.dispose()
    }

    private fun performChangeAction(
        action: (
            downloadingSubject: BehaviorSubject<Long>,
            editingSubject: BehaviorSubject<Long>
        ) -> Completable
    ) {
        val filesCount = album?.compositionsCount ?: 0

        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = performFilesChangeAction(
            syncInteractor,
            uiScheduler,
            { preparedCount -> viewState.showPreparedFilesCount(preparedCount, filesCount) },
            { progressInfo -> viewState.showDownloadingFileInfo(progressInfo) },
            { editedCount -> viewState.showEditedFilesCount(editedCount, filesCount) }
        ) { downloadingSubject, editingSubject ->
            action(downloadingSubject, editingSubject)
        }.doOnSubscribe { viewState.showRenameProgress() }
            .doFinally { viewState.hideRenameProgress() }
        changeDisposable = lastEditAction!!.subscribe(viewState::showErrorMessage)
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun loadAlbum() {
        editorInteractor.getAlbumObservable(albumId)
            .subscribeOnUi(
                this::onAlbumReceived,
                this::onCompositionLoadingError,
                viewState::closeScreen
            )
    }

    private fun onAlbumReceived(album: Album) {
        this.album = album
        viewState.showAlbum(album)
    }

    private fun onCompositionLoadingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showAlbumLoadingError(errorCommand)
    }
}