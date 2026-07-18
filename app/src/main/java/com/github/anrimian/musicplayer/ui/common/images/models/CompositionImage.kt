package com.github.anrimian.musicplayer.ui.common.images.models

data class CompositionImage(
    val id: Long,
    val lastModifyTime: Long,
    val lastCoverModifyTime: Long,
    val size: Long,
    val isFileExists: Boolean
)