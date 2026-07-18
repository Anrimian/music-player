package com.github.anrimian.musicplayer.domain.models.utils

import com.github.anrimian.fsync.models.catalog.ChangedKey
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus
import com.github.anrimian.musicplayer.domain.models.composition.change.ChangedCompositionPath
import com.github.anrimian.musicplayer.domain.models.sync.FileKey

fun CompositionModel.isFileRemote(): Boolean = fileStatus == LocalFileStatus.LIBRARY_ENTRY_ONLY

fun FullComposition.isFileExists() = storageId != null

fun DeletedComposition.toFileKey() = FileKey(fileName, parentPath)

fun List<DeletedComposition>.toFileKeys() = map(DeletedComposition::toFileKey)

fun ChangedCompositionPath.toChangedKey() = ChangedKey(
    oldPath,
    newPath,
    lastPathModifyTime
)

fun List<ChangedCompositionPath>.toChangedKeys() = map(ChangedCompositionPath::toChangedKey)