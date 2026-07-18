package com.github.anrimian.musicplayer.domain.models.player

/**
 * Created on 10.12.2017.
 */
enum class AudioFocusEvent {
    GAIN,
    LOSS,
    LOSS_SHORTLY,
    LOSS_TRANSIENT
}
