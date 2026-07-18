package com.github.anrimian.musicplayer.ui.library.common.library

import com.github.anrimian.musicplayer.data.models.exceptions.DuplicatePlaylistEntriesException
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single

abstract class BaseLibraryPresenter<V: BaseLibraryView>(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsInteractor: PlaylistsInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser,
) : AppPresenter<V>(uiScheduler, errorParser) {

    private var compositionsForPlaylistFetcher: Single<List<Composition>>? = null
    private var playlistToInsert: Playlist? = null
    private var insertToPlaylistCompleteAction: (() -> Unit)? = null

    fun onAddDuplicatePlaylistEntriesConfirmed(ignoreDuplicates: Boolean) {
        if (compositionsForPlaylistFetcher == null || playlistToInsert == null) {
            return
        }
        compositionsForPlaylistFetcher!!.flatMap { c ->
            playListsInteractor.addCompositionsToPlaylist(c, playlistToInsert!!, false, ignoreDuplicates)
        }.launchOnUi(this::onAddingPlaylistCompleted, viewState::showErrorMessage)
    }

    fun onPlaylistDuplicateChecked(isChecked: Boolean) {
        playListsInteractor.setPlaylistDuplicateCheckEnabled(isChecked)
    }

    protected fun addCompositionsToPlayNext(compositions: List<Composition>) {
        addCompositionsToPlayNext(Single.just(compositions))
    }

    protected fun addCompositionsToPlayNext(compositionsFetcher: Single<List<Composition>>) {
        compositionsFetcher.flatMap(playerInteractor::addCompositionsToPlayNext)
            .launchOnUi(viewState::onCompositionsAddedToPlayNext, viewState::showErrorMessage)
    }

    protected fun addCompositionsToEndOfQueue(compositions: List<Composition>) {
        addCompositionsToEndOfQueue(Single.just(compositions))
    }

    protected fun addCompositionsToEndOfQueue(compositionsFetcher: Single<List<Composition>>) {
        compositionsFetcher.flatMap(playerInteractor::addCompositionsToEnd)
            .launchOnUi(viewState::onCompositionsAddedToQueue, viewState::showErrorMessage)
    }

    protected fun performAddToPlaylist(
        compositions: List<Composition>,
        playList: Playlist,
        onComplete: () -> Unit
    ) {
        performAddToPlaylist(Single.just(compositions), playList, onComplete)
    }

    protected fun performAddToPlaylist(
        compositionsFetcher: Single<List<Composition>>,
        playList: Playlist,
        onComplete: () -> Unit
    ) {
        this.compositionsForPlaylistFetcher = compositionsFetcher
        this.playlistToInsert = playList
        this.insertToPlaylistCompleteAction = onComplete
        compositionsFetcher.flatMap { c ->
            playListsInteractor.addCompositionsToPlaylist(c, playList, true, false)
        }.subscribeOnUi(this::onAddingPlaylistCompleted) { t -> this.onAddToPlaylistError(t, playList) }
    }

    private fun onAddingPlaylistCompleted(compositions: List<CompositionModel>) {
        if (playlistToInsert != null) {
            viewState.showAddingToPlaylistComplete(playlistToInsert!!, compositions)
        }
        insertToPlaylistCompleteAction?.invoke()
        insertToPlaylistCompleteAction = null
        compositionsForPlaylistFetcher = null
        playlistToInsert = null
    }

    private fun onAddToPlaylistError(t: Throwable, playList: Playlist) {
        if (t is DuplicatePlaylistEntriesException) {
            val duplicateCheckEnabled = playListsInteractor.isPlaylistDuplicateCheckEnabled()
            viewState.showPlaylistDuplicateEntryDialog(t.duplicates, t.hasNonDuplicates, playList, duplicateCheckEnabled)
        } else {
            viewState.showErrorMessage(errorParser.parseError(t))
        }
    }

}