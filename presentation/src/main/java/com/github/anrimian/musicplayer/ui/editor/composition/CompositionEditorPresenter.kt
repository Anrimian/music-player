package com.github.anrimian.musicplayer.ui.editor.composition

import com.github.anrimian.musicplayer.data.utils.rx.RxUtils
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre
import com.github.anrimian.musicplayer.domain.models.image.ImageSource
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class CompositionEditorPresenter(
        private val compositionId: Long,
        private val editorInteractor: EditorInteractor,
        uiScheduler: Scheduler,
        errorParser: ErrorParser
) : AppPresenter<CompositionEditorView>(uiScheduler, errorParser) {
    
    private var changeDisposable: Disposable? = null
    
    private lateinit var composition: FullComposition
    private var removedGenre: ShortGenre? = null
    private var lastEditAction: Completable? = null
    
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadComposition()
        loadGenres()
    }

    fun onChangeAuthorClicked() {
        if (!::composition.isInitialized) {
            return
        }
        editorInteractor.authorNames
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
        editorInteractor.albumNames
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
        editorInteractor.authorNames
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
        editorInteractor.genreNames
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
        editorInteractor.genreNames
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
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.addCompositionGenre(composition, genre)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showChangeFileProgress() }
                .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
    }

    fun onNewGenreNameEntered(newName: String?, oldGenre: ShortGenre?) {
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.changeCompositionGenre(composition, oldGenre, newName)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showChangeFileProgress() }
                .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
    }

    fun onRemoveGenreClicked(genre: ShortGenre) {
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.removeCompositionGenre(composition, genre)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showChangeFileProgress() }
                .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({ onGenreRemoved(genre) }, this::onDefaultError, presenterDisposable)
    }

    fun onRestoreRemovedGenreClicked() {
        if (removedGenre == null) {
            return
        }
        onNewGenreEntered(removedGenre!!.name)
    }

    fun onNewAuthorEntered(author: String?) {
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.editCompositionAuthor(composition, author)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showChangeFileProgress() }
                .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
    }

    fun onNewAlbumEntered(album: String?) {
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.editCompositionAlbum(composition, album)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showChangeFileProgress() }
                .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
    }

    fun onNewAlbumArtistEntered(artist: String?) {
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.editCompositionAlbumArtist(composition, artist)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showChangeFileProgress() }
                .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
    }

    fun onNewTitleEntered(title: String) {
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.editCompositionTitle(composition, title)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showChangeFileProgress() }
                .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
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
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.editCompositionLyrics(composition, text)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showChangeFileProgress() }
                .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
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
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.removeCompositionAlbumArt(composition)
                .observeOn(uiScheduler)
                .doOnSubscribe { viewState.showChangeFileProgress() }
                .doFinally { viewState.hideChangeFileProgress() }
        changeDisposable = lastEditAction!!.subscribe({}, this::onDefaultError, presenterDisposable)
    }

    fun onNewCoverSelected() {
        viewState.showSelectImageFromGalleryScreen()
    }

    fun onNewImageForCoverSelected(imageSource: ImageSource?) {
        if (!::composition.isInitialized) {
            return
        }
        RxUtils.dispose(changeDisposable, presenterDisposable)
        lastEditAction = editorInteractor.changeCompositionAlbumArt(composition, imageSource)
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

    private fun onDefaultError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showErrorMessage(errorCommand)
    }

    private fun loadComposition() {
        editorInteractor.getCompositionObservable(compositionId)
                .subscribeOnUi(
                        this::onCompositionReceived,
                        this::onCompositionLoadingError,
                        viewState::closeScreen
                )
    }

    private fun loadGenres() {
        editorInteractor.getShortGenresInComposition(compositionId)
                .subscribeOnUi(this::onGenresReceived, this::onDefaultError)
    }

    private fun onGenresReceived(shortGenres: List<ShortGenre>) {
        viewState.showGenres(shortGenres)
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
    }

    private fun checkCompositionTagsInSource(composition: FullComposition) {
        editorInteractor.updateTagsFromSource(composition)
                .justSubscribeOnUi(this::onTagCheckError)
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