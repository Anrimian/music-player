package com.github.anrimian.musicplayer.ui.playlists.rename

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.SimpleViewModel

class RenamePlaylistViewModel(
    private val playListsInteractor: PlaylistsInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
) : SimpleViewModel<RenamePlaylistState>(
    RenamePlaylistState(),
    savedStateHandle,
    errorParser
) {

    private val args = getArgs<RenamePlaylistDialogData>()

    fun onConfirmClicked(newName: String) {
        if (newName.isBlank()) {
            return
        }

        updateState { copy(isLoading = true, error = null) }

        launch(
            onError = { errorCommand ->
                updateState { copy(isLoading = false, error = errorCommand) }
            }
        ) {
            playListsInteractor.updatePlaylistName(args.playlistId, newName)
            sendEffect(RenamePlaylistEffect.Close)
        }
    }

}