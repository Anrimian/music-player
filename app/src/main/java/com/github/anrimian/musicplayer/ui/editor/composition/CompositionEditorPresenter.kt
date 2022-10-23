package com.github.anrimian.musicplayer.ui.editor.composition

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre
import com.github.anrimian.musicplayer.domain.models.image.ImageSource
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class CompositionEditorPresenter(
    private val compositionId: Long,
    private val editorInteractor: EditorInteractor,
    private val syncInteractor: SyncInteractor<*, *, Long>,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<CompositionEditorView>(uiScheduler, errorParser) {

    private var changeDisposable: Disposable? = null

    private lateinit var composition: FullComposition
    private lateinit var fileSyncState: FileSyncState

    private var removedGenre: ShortGenre? = null
    private var lastEditAction: Completable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnComposition()
        subscribeOnGenres()
        subscribeOnSyncState()
    }

    fun onChangeAuthorClicked() {
        if (!::composition.isInitialized) {
            return
        }
        editorInteractor.getAuthorNames()
            .observeOn(uiScheduler)
            .doOnSuccess { artists -> viewState.showEnterAuthorDialog(composition, artists) }
            .doOnError { throwable ->
                viewState.showEnterAuthorDialog(composition, null)
                onDefaultError(throwable)
            }
            .ignoreElement()
            .onErrorComplete()
            .subscribe()
    }

    fun onChangeTitleClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.showEnterTitleDialog(composition)
    }

    fun onChangeLyricsClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.showEnterLyricsDialog(composition)
    }

    fun onChangeFileNameClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.showEnterFileNameDialog(composition)
    }

    fun onChangeAlbumClicked() {
        if (!::composition.isInitialized) {
            return
        }
        editorInteractor.getAlbumNames()
            .observeOn(uiScheduler)
            .doOnSuccess { albums -> viewState.showEnterAlbumDialog(composition, albums) }
            .doOnError { throwable ->
                viewState.showEnterAlbumDialog(composition, null)
                onDefaultError(throwable)
            }
            .ignoreElement()
            .onErrorComplete()
            .subscribe()
    }

    fun onChangeAlbumArtistClicked() {
        if (!::composition.isInitialized) {
            return
        }
        editorInteractor.getAuthorNames()
            .observeOn(uiScheduler)
            .doOnSuccess { albums -> viewState.showEnterAlbumArtistDialog(composition, albums) }
            .doOnError { throwable ->
                viewState.showEnterAlbumArtistDialog(composition, null)
                onDefaultError(throwable)
            }
            .ignoreElement()
            .onErrorComplete()
            .subscribe()
    }

    fun onAddGenreItemClicked() {
        editorInteractor.getGenreNames()
            .observeOn(uiScheduler)
            .doOnSuccess(viewState::showAddGenreDialog)
            .doOnError { throwable ->
                viewState.showAddGenreDialog(null)
                onDefaultError(throwable)
            }
            .ignoreElement()
            .onErrorComplete()
            .subscribe()
    }

    fun onGenreItemClicked(genre: ShortGenre) {
        editorInteractor.getGenreNames()
            .observeOn(uiScheduler)
            .doOnSuccess { genres -> viewState.showEditGenreDialog(genre, genres) }
            .doOnError { throwable ->
                viewState.showEditGenreDialog(genre, null)
                onDefaultError(throwable)
            }
            .ignoreElement()
            .onErrorComplete()
            .subscribe()
    }

    fun onNewGenreEntered(genre: String) {
        performChangeAction(editorInteractor.addCompositionGenre(compositionId, genre))
    }

    fun onNewGenreNameEntered(newName: String?, oldGenre: ShortGenre?) {
        performChangeAction(editorInteractor.changeCompositionGenre(compositionId, oldGenre, newName))
    }

    fun onRemoveGenreClicked(genre: ShortGenre) {
        performChangeAction(editorInteractor.removeCompositionGenre(compositionId, genre)) {
            onGenreRemoved(genre)
        }
    }

    fun onRestoreRemovedGenreClicked() {
        if (removedGenre == null) {
            return
        }
        onNewGenreEntered(removedGenre!!.name)
    }

    fun onNewAuthorEntered(author: String?) {
        performChangeAction(editorInteractor.editCompositionAuthor(compositionId, author))
    }

    fun onNewAlbumEntered(album: String?) {
        performChangeAction(editorInteractor.editCompositionAlbum(compositionId, album))
    }

    fun onNewAlbumArtistEntered(artist: String?) {
        performChangeAction(editorInteractor.editCompositionAlbumArtist(compositionId, artist))
    }

    fun onNewTitleEntered(title: String) {
        performChangeAction(editorInteractor.editCompositionTitle(compositionId, title))
    }

    fun onNewFileNameEntered(fileName: String) {
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.editCompositionFileName(composition, fileName)
            .observeOn(uiScheduler)
            .doOnSubscribe { viewState.showChangeFileProgress() }
            .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
    }

    fun onNewLyricsEntered(text: String?) {
        performChangeAction(editorInteractor.editCompositionLyrics(compositionId, text))
    }

    fun onCopyFileNameClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.copyFileNameText(composition.fileName)
    }

    fun onChangeCoverClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.showCoverActionsDialog()
    }

    fun onClearCoverClicked() {
        performChangeAction(editorInteractor.removeCompositionAlbumArt(compositionId))
    }

    fun onNewCoverSelected() {
        viewState.showSelectImageFromGalleryScreen()
    }

    fun onNewImageForCoverSelected(imageSource: ImageSource?) {
        performChangeAction(editorInteractor.changeCompositionAlbumArt(compositionId, imageSource))
    }

    fun onRetryFailedEditActionClicked() {
        if (lastEditAction != null) {
            RxUtils.dispose(changeDisposable, presenterDisposable)
            changeDisposable = lastEditAction!!
                .doFinally { lastEditAction = null }
                .subscribe({}, this::onDefaultError, presenterDisposable)
        }
    }

    fun onEditActionCancelled() {
        changeDisposable?.dispose()
    }

    private fun performChangeAction(action: Completable, onComplete: (() -> Unit)? = null) {
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = action
            .observeOn(uiScheduler)
            .doOnSubscribe { viewState.showChangeFileProgress() }
            .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe(
            { onComplete?.invoke() },
            this::onDefaultError,
            presenterDisposable
        )
    }

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun subscribeOnSyncState() {
        syncInteractor.getFileSyncStateObservable(compositionId)
            .unsafeSubscribeOnUi(this::onSyncStateReceived)
    }

    private fun subscribeOnComposition() {
        editorInteractor.getCompositionObservable(compositionId)
            .subscribeOnUi(
                this::onCompositionReceived,
                this::onCompositionLoadingError,
                viewState::closeScreen
            )
    }

    private fun subscribeOnGenres() {
        editorInteractor.getShortGenresInComposition(compositionId)
            .subscribeOnUi(this::onGenresReceived, this::onDefaultError)
    }

    private fun onGenresReceived(shortGenres: List<ShortGenre>) {
        viewState.showGenres(shortGenres)
    }

    private fun onSyncStateReceived(syncState: FileSyncState) {
        this.fileSyncState = syncState
        showSyncState()
    }

    private fun onCompositionReceived(composition: FullComposition) {
        val firstReceive = !this::composition.isInitialized
        if (firstReceive) {
            checkCompositionTagsInSource(composition)
        }
        if (firstReceive || this.composition.dateModified != composition.dateModified) {
            viewState.showCompositionCover(composition)
        }
        this.composition = composition
        viewState.showComposition(composition)
        showSyncState()
    }

    private fun showSyncState() {
        if (::composition.isInitialized && ::fileSyncState.isInitialized) {
            viewState.showSyncState(fileSyncState, composition)
        }
    }

    private fun checkCompositionTagsInSource(composition: FullComposition) {
        editorInteractor.updateTagsFromSource(composition).justSubscribeOnUi(this::onTagCheckError)
    }

    private fun onTagCheckError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showCheckTagsErrorMessage(errorCommand)
    }

    private fun onCompositionLoadingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showCompositionLoadingError(errorCommand)
    }

    private fun onGenreRemoved(genre: ShortGenre) {
        removedGenre = genre
        viewState.showRemovedGenreMessage(genre)
    }
}