package com.github.anrimian.musicplayer.ui.settings.folders

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.ui.common.effects.MessageAction
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class ExcludedFoldersState(
    val folders: StatedData<ImmutableList<IgnoredFolder>> = StatedData.Empty()
)

data object RestoreRemovedFolder : MessageAction