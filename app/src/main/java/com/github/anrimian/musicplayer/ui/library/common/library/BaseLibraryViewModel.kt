package com.github.anrimian.musicplayer.ui.library.common.library

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.data.models.exceptions.DuplicatePlaylistEntriesException
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.dialogs.share.ShareDialogData
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.format.createAddToPlaylistMessage
import com.github.anrimian.musicplayer.ui.common.format.createAddedToQueueMessage
import com.github.anrimian.musicplayer.ui.common.format.createPlayNextMessage
import com.github.anrimian.musicplayer.ui.common.mvvm.BaseViewModel
import kotlinx.coroutines.rx3.await

abstract class BaseLibraryViewModel<S, P : Parcelable>(
    private val playerInteractor: LibraryPlayerInteractor,
    private val playListsInteractor: PlaylistsInteractor,
    initialState: S,
    initialPersistentState: P,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser,
) : BaseViewModel<S, P>(initialState, initialPersistentState, savedStateHandle, errorParser) {

    private var compositionsForPlaylistFetcher: (suspend () -> List<CompositionModel>)? = null
    private var playlistToInsert: Playlist? = null
    private var insertToPlaylistCompleteAction: (() -> Unit)? = null

    fun onAddDuplicatePlaylistEntriesConfirmed(ignoreDuplicates: Boolean) {
        dismissDialog()

        val compositionsFetcher = compositionsForPlaylistFetcher ?: return
        val playlistToInsert = playlistToInsert ?: return

        launch(onError = ::sendErrorMessage) {
            val compositions = compositionsFetcher()
            val addedCompositions = playListsInteractor.addCompositionsToPlaylist(
                compositions,
                playlistToInsert,
                false,
                ignoreDuplicates
            ).await()
            onAddingPlaylistCompleted(addedCompositions)
        }
    }

    fun onPlaylistDuplicateChecked(isChecked: Boolean) {
        playListsInteractor.setPlaylistDuplicateCheckEnabled(isChecked)
        updateCurrentDialog<PlaylistDuplicateEntryDialog> {
            copy(isDuplicateCheckEnabled = isChecked)
        }
    }

    fun onPlaylistDuplicateEntriesDialogClosed() {
        dismissDialog()
    }

    fun onShareDialogClosed() {
        dismissDialog()
    }

    fun onShareError(errorCommand: ErrorCommand) {
        sendErrorMessage(errorCommand)
    }

    protected fun shareCompositions(compositions: Collection<CompositionModel>) {
        showDialog(ShareDialogData(compositions))
    }

    protected fun shareComposition(composition: Composition) {
        showDialog(ShareDialogData(composition))
    }

    protected fun addCompositionsToPlayNext(compositions: List<CompositionModel>) {
        addCompositionsToPlayNext { compositions }
    }

    protected fun addCompositionsToPlayNext(compositionsFetcher: suspend () -> List<CompositionModel>) {
        launch(onError = ::sendErrorMessage) {
            val compositions = compositionsFetcher()
            val addedCompositions = playerInteractor.addCompositionsToPlayNext(compositions).await()
            sendMessage(createPlayNextMessage(addedCompositions))
        }
    }

    protected fun addCompositionsToEndOfQueue(compositions: List<CompositionModel>) {
        addCompositionsToEndOfQueue { compositions }
    }

    protected fun addCompositionsToEndOfQueue(compositionsFetcher: suspend () -> List<CompositionModel>) {
        launch(onError = ::sendErrorMessage) {
            val compositions = compositionsFetcher()
            val addedCompositions = playerInteractor.addCompositionsToEnd(compositions).await()
            sendMessage(createAddedToQueueMessage(addedCompositions))
        }
    }

    protected fun performAddToPlaylist(
        compositions: List<Composition>,
        playList: Playlist,
        onComplete: () -> Unit
    ) {
        performAddToPlaylist({ compositions }, playList, onComplete)
    }

    protected fun performAddToPlaylist(
        compositionsFetcher: suspend () -> List<CompositionModel>,
        playList: Playlist,
        onComplete: (() -> Unit)? = null
    ) {
        this.compositionsForPlaylistFetcher = compositionsFetcher
        this.playlistToInsert = playList
        this.insertToPlaylistCompleteAction = onComplete

        launchCatching(
            onError = { t -> onAddToPlaylistError(t, playList) }
        ) {
            val compositions = compositionsFetcher()
            val addedCompositions = playListsInteractor.addCompositionsToPlaylist(
                compositions = compositions,
                playList = playList,
                checkForDuplicates = true,
                ignoreDuplicates = false
            ).await()
            onAddingPlaylistCompleted(addedCompositions)
        }
    }

    private fun onAddingPlaylistCompleted(compositions: List<CompositionModel>) {
        val playlistToInsert = playlistToInsert
        if (playlistToInsert != null) {
            sendMessage(createAddToPlaylistMessage(playlistToInsert, compositions))
        }
        insertToPlaylistCompleteAction?.invoke()
        insertToPlaylistCompleteAction = null
        compositionsForPlaylistFetcher = null
        this.playlistToInsert = null
    }

    private fun onAddToPlaylistError(
        throwable: Throwable,
        playList: Playlist
    ) {
        if (throwable is DuplicatePlaylistEntriesException) {
            val topTitles = throwable.duplicates
                .take(MAX_DISPLAY_DUPLICATE_FILES_COUNT)
                .map { composition -> composition.title }

            val dialog = PlaylistDuplicateEntryDialog(
                playlistName = playList.name,
                topDuplicateTitles = topTitles,
                totalDuplicatesCount = throwable.duplicates.size,
                hasNonDuplicates = throwable.hasNonDuplicates,
                isDuplicateCheckEnabled = playListsInteractor.isPlaylistDuplicateCheckEnabled()
            )
            showDialog(dialog)
        } else {
            sendErrorMessage(throwable)
        }
    }

    private companion object {
        const val MAX_DISPLAY_DUPLICATE_FILES_COUNT = 5
    }

}