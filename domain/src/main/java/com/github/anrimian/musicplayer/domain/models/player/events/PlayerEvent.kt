package com.github.anrimian.musicplayer.domain.models.player.events

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource

sealed class PlayerEvent(val source: CompositionSource) {
    class ErrorEvent(val throwable: Throwable, source: CompositionSource): PlayerEvent(source)
    class FinishedEvent(source: CompositionSource): PlayerEvent(source)
    class PreparedEvent(source: CompositionSource): PlayerEvent(source)
}