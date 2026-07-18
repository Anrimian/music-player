package com.github.anrimian.musicplayer.data.utils.exo_player

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

class PlayerEventListener(
    private val onEnded: () -> Unit,
    private val errorCallback: (PlaybackException) -> Unit
) : Player.Listener {

    override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_ENDED) {
            onEnded()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        errorCallback(error)
    }

}
