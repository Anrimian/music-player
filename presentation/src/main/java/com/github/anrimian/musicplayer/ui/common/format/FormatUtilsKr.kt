package com.github.anrimian.musicplayer.ui.common.format

import androidx.annotation.StringRes
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.player.MediaPlayers

@StringRes
fun getMediaPlayerName(mediaPlayerId: Int) = when(mediaPlayerId) {
    MediaPlayers.EXO_MEDIA_PLAYER -> R.string.exo_media_player
    else -> R.string.android_media_player
}

@StringRes
fun getMediaPlayerDescription(mediaPlayerId: Int) = when(mediaPlayerId) {
    MediaPlayers.EXO_MEDIA_PLAYER -> R.string.exo_media_player_description
    else -> R.string.android_media_player_description
}