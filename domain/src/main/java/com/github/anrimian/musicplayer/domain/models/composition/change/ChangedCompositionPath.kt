package com.github.anrimian.musicplayer.domain.models.composition.change

data class ChangedCompositionPath(
    val oldPath: CompositionPath,
    val newPath: CompositionPath
)