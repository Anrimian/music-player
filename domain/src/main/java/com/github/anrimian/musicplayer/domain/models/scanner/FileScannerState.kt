package com.github.anrimian.musicplayer.domain.models.scanner

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition

sealed interface FileScannerState
object Idle: FileScannerState
data class Running(val composition: FullComposition): FileScannerState