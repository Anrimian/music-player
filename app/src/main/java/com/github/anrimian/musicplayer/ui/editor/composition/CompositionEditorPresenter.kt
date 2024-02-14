package com.github.anrimian.musicplayer.ui.editor.composition

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.image.ImageSource
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.domain.models.utils.isFileExists
import com.github.anrimian.musicplayer.domain.utils.ListUtils
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Collections

class CompositionEditorPresenter(
    private val compositionId: Long,
    private val editorInteractor: EditorInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<CompositionEditorView>(uiScheduler, errorParser) {

    private var changeDisposable: Disposable? = null

    private lateinit var composition: FullComposition
    private lateinit var genres: MutableList<String>
    private lateinit var fileSyncState: FileSyncState

    private var lastEditAction: Completable? = null

    private var startGenreDragPosition = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnComposition()
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
        editorInteractor.getGenreNames(compositionId)
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

    fun onGenreItemClicked(genre: String) {
        editorInteractor.getGenreNames(compositionId)
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

    fun onNewGenreNameEntered(newName: String, oldGenre: String) {
        performChangeAction(editorInteractor.changeCompositionGenre(compositionId, oldGenre, newName))
    }

    fun onGenreItemMoved(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                swapGenreItems(i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                swapGenreItems(i, i - 1)
            }
        }
    }

    fun onGenreItemDragStarted(position: Int) {
        startGenreDragPosition = position
    }

    fun onGenreItemDragEnded(position: Int) {
        if (startGenreDragPosition == position
            || !ListUtils.isIndexInRange(genres, startGenreDragPosition)
            || !ListUtils.isIndexInRange(genres, position)
        ) {
            return
        }
        performChangeAction(editorInteractor.moveGenre(compositionId, startGenreDragPosition, position))
    }

    fun onEditRequestDenied() {
        showComposition()
    }

    fun onRemoveGenreClicked(genre: String) {
        performChangeAction(editorInteractor.removeCompositionGenre(compositionId, genre)) {
            viewState.showRemovedGenreMessage(genre)
        }
    }

    fun onRestoreRemovedGenreClicked() {
        performChangeAction(editorInteractor.restoreRemovedCompositionGenre())
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
        performChangeAction(editorInteractor.editCompositionFileName(compositionId, fileName))
    }

    fun onNewTrackNumberEntered(number: Long?) {
        performChangeAction(editorInteractor.editCompositionTrackNumber(compositionId, number))
    }

    fun onNewDiscNumberEntered(number: Long?) {
        performChangeAction(editorInteractor.editCompositionDiscNumber(compositionId, number))
    }

    fun onNewCommentEntered(text: String?) {
        performChangeAction(editorInteractor.editCompositionComment(compositionId, text))
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

    fun onChangeTrackNumberClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.showEnterTrackNumberDialog(composition)
    }

    fun onChangeDiscNumberClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.showEnterDiscNumberDialog(composition)
    }

    fun onChangeCommentClicked() {
        if (!::composition.isInitialized) {
            return
        }
        viewState.showEnterCommentDialog(composition)
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

    private fun swapGenreItems(from: Int, to: Int) {
        if (!ListUtils.isIndexInRange(genres, from) || !ListUtils.isIndexInRange(genres, to)) {
            return
        }

        Collections.swap(genres, from, to)
        viewState.notifyGenreItemMoved(from, to)
    }

    private fun performChangeAction(action: Completable, onComplete: (() -> Unit)? = null) {
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = action
            .observeOn(uiScheduler)
            .doOnSubscribe { viewState.showChangeFileProgress() }
            .doFinally { viewState.hideChangeFileProgress() }
            .doOnComplete { onComplete?.invoke() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
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
            .runOnUi(
                this::onCompositionReceived,
                viewState::showCompositionLoadingError,
                viewState::closeScreen
            )
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
        if (firstReceive
            || this.composition.coverModifyTime != composition.coverModifyTime
            || this.composition.isFileExists() != composition.isFileExists()) {
            viewState.showCompositionCover(composition)
        }
        this.composition = composition
        showComposition()
        showSyncState()
    }

    private fun showComposition() {
        this.genres = CompositionHelper.splitGenres(composition.genres).toMutableList()
        viewState.showComposition(composition, genres)
    }

    private fun showSyncState() {
        if (::composition.isInitialized && ::fileSyncState.isInitialized) {
            viewState.showSyncState(fileSyncState, composition)
        }
    }

    private fun checkCompositionTagsInSource(composition: FullComposition) {
        editorInteractor.updateTagsFromSource(composition)
            .runOnUi(viewState::showCheckTagsErrorMessage)
    }

}