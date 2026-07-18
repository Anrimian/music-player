package com.github.anrimian.musicplayer.ui.playlists.create

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlaylistsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.SimpleViewModel
import kotlinx.coroutines.rx3.await

class CreatePlaylistViewModel(
    private val playListsInteractor: PlaylistsInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
) : SimpleViewModel<CreatePlaylistState>(
    CreatePlaylistState(),
    savedStateHandle,
    errorParser
) {

    fun onConfirmClicked(name: String) {
        if (name.isBlank()) {
            return
        }

        updateState { copy(isLoading = true, error = null) }

        launch(
            onError = { errorCommand ->
                updateState { copy(isLoading = false, error = errorCommand) }
            }
        ) {
            playListsInteractor.createPlaylist(name).await()
            sendEffect(CreatePlaylistEffect.Close)
        }
    }

}
