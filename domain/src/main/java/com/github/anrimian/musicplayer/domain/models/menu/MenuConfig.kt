package com.github.anrimian.musicplayer.domain.models.menu

enum class MenuCategory(val id: Int) {
    PRIMARY(0),
    SECONDARY(1),
    HIDDEN(-1)
}

data class MenuItemConfig(
    val id: Int,
    val category: MenuCategory
)

data class MenuConfig(
    val items: List<MenuItemConfig>
)