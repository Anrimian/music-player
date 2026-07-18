package com.github.anrimian.musicplayer.domain.models.composition


data class CurrentComposition(val composition: CompositionModel?, val isPlaying: Boolean) {
    val id = composition?.id
}
