package com.github.anrimian.musicplayer.domain.models.scanner

import com.github.anrimian.musicplayer.domain.models.composition.change.ChangedCompositionPath
import com.github.anrimian.musicplayer.domain.models.sync.FileKey

class StorageAnalyzeResult(
    val disappearedFiles: List<FileKey>,
    val reappearedFiles: List<FileKey>,
    val movedFiles: List<ChangedCompositionPath>,
    val modifyTime: Long,
    val hasChanges: Boolean,
)