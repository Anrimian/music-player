package com.github.anrimian.musicplayer.domain.interactors.player.screen

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition

sealed interface CurrentAction
data object NoAction : CurrentAction
data class ScannerRunning(val composition: FullComposition) : CurrentAction
data class MissingCompositions(val count: Int) : CurrentAction