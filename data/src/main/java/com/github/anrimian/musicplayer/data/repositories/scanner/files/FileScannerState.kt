package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition

sealed class FileScannerState
object Idle: FileScannerState()
data class Running(val composition: FullComposition): FileScannerState()