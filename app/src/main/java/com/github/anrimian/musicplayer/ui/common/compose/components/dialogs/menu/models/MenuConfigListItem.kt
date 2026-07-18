package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.models

import com.github.anrimian.musicplayer.domain.models.menu.MenuCategory
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem

sealed interface MenuConfigListItem {
    val key: Any

    data class Header(
        val category: MenuCategory,
        val titleRes: Int
    ) : MenuConfigListItem {
        override val key: String = category.name
    }

    data class Item(
        val appMenuItem: AppMenuItem
    ) : MenuConfigListItem {
        override val key: Int = appMenuItem.id
    }
}