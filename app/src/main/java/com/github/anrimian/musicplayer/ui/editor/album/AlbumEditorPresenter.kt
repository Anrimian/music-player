package com.github.anrimian.musicplayer.ui.editor.album

import com.github.anrimian.musicplayer.data.utils.rx.RxUtils
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class AlbumEditorPresenter(
        private val albumId: Long,
        private val editorInteractor: EditorInteractor,
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
        editorInteractor.authorNames
                .observeOn(uiScheduler)
                .doOnSuccess { artists -> viewState.showEnterAuthorDialog(album, artists) }
                .doOnError { throwable ->
                    viewState.showEnterAuthorDialog(album, null)
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
        viewState.showEnterNameDialog(album)
    }

    fun onNewAuthorEntered(author: String?) {
        if (album == null) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.updateAlbumArtist(author, album!!.id)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showRenameProgress() }
                .doFinally { viewState.hideRenameProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
    }

    fun onNewNameEntered(name: String?) {
        if (album == null) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.updateAlbumName(name, album!!.id)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showRenameProgress() }
                .doFinally { viewState.hideRenameProgress() }
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