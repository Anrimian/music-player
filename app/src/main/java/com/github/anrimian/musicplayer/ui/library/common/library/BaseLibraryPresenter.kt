package com.github.anrimian.musicplayer.ui.library.common.library

import com.github.anrimian.musicplayer.data.models.exceptions.DuplicatePlaylistEntriesException
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single

abstract class BaseLibraryPresenter<V: BaseLibraryView>(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsInteractor: PlayListsInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser,
) : AppPresenter<V>(uiScheduler, errorParser) {

    private var compositionsForPlayListFetcher: Single<List<Composition>>? = null
    private var playlistToInsert: PlayList? = null
    private var insertToPlaylistCompleteAction: (() -> Unit)? = null

    fun onAddDuplicatePlaylistEntriesConfirmed(ignoreDuplicates: Boolean) {
        if (compositionsForPlayListFetcher == null || playlistToInsert == null) {
            return
        }
        compositionsForPlayListFetcher!!.flatMap { c ->
            playListsInteractor.addCompositionsToPlayList(c, playlistToInsert!!, false, ignoreDuplicates)
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
        playList: PlayList,
        onComplete: () -> Unit
    ) {
        performAddToPlaylist(Single.just(compositions), playList, onComplete)
    }

    protected fun performAddToPlaylist(
        compositionsFetcher: Single<List<Composition>>,
        playList: PlayList,
        onComplete: () -> Unit
    ) {
        this.compositionsForPlayListFetcher = compositionsFetcher
        this.playlistToInsert = playList
        this.insertToPlaylistCompleteAction = onComplete
        compositionsFetcher.flatMap { c ->
            playListsInteractor.addCompositionsToPlayList(c, playList, true, false)
        }.subscribeOnUi(this::onAddingPlaylistCompleted) { t -> this.onAddToPlaylistError(t, playList) }
    }

    private fun onAddingPlaylistCompleted(compositions: List<Composition>) {
        if (playlistToInsert != null) {
            viewState.showAddingToPlayListComplete(playlistToInsert!!, compositions)
        }
        insertToPlaylistCompleteAction?.invoke()
        insertToPlaylistCompleteAction = null
        compositionsForPlayListFetcher = null
        playlistToInsert = null
    }

    private fun onAddToPlaylistError(t: Throwable, playList: PlayList) {
        if (t is DuplicatePlaylistEntriesException) {
            val duplicateCheckEnabled = playListsInteractor.isPlaylistDuplicateCheckEnabled()
            viewState.showPlaylistDuplicateEntryDialog(t.duplicates, t.hasNonDuplicates, playList, duplicateCheckEnabled)
        } else {
            viewState.showErrorMessage(errorParser.parseError(t))
        }
    }

}