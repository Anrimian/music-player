package com.github.anrimian.musicplayer.ui.common.images.models

import java.util.*

data class CompositionImage(
    val id: Long,
    val lastModifyTime: Date,
    val size: Long,
    val isFileExists: Boolean
)