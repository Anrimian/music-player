package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.github.anrimian.musicplayer.domain.models.menu.AppMenu
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.models.MenuConfigListItem
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.mvvm.AppDialog
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuConfigDialogData(val menu: AppMenu) : AppDialog

data class MenuConfigState(
    val items: SnapshotStateList<MenuConfigListItem> = mutableStateListOf()
)

sealed interface MenuConfigEffect : BaseEffect {
    data object Close : MenuConfigEffect
}