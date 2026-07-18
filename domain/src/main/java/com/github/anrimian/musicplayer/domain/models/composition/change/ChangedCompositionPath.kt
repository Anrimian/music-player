package com.github.anrimian.musicplayer.domain.models.composition.change

import com.github.anrimian.musicplayer.domain.models.sync.FileKey

data class ChangedCompositionPath(
    val oldPath: FileKey,
    val newPath: FileKey,
    val lastPathModifyTime: Long?
)