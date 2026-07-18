package com.github.anrimian.musicplayer.ui.common.menu.utils

import com.github.anrimian.musicplayer.domain.models.menu.MenuCategory
import com.github.anrimian.musicplayer.domain.models.menu.MenuConfig
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem

object MenuConfigUtil {

    fun applyConfig(
        defaultItems: List<AppMenuItem>,
        config: MenuConfig?,
        includeHidden: Boolean = false
    ): List<AppMenuItem> {
        if (config == null || config.items.isEmpty()) {
            return defaultItems
        }

        val itemsMap = defaultItems.associateByTo(mutableMapOf()) { item -> item.id }
        val resultList = ArrayList<AppMenuItem>(defaultItems.size)

        for (itemConfig in config.items) {
            val item = itemsMap.remove(itemConfig.id) ?: continue

            if (itemConfig.category != MenuCategory.HIDDEN || includeHidden) {
                resultList.add(item.copy(groupId = itemConfig.category.id))
            }
        }

        if (itemsMap.isNotEmpty()) {
            itemsMap.values.forEach { newItem ->
                resultList.add(newItem.copy(groupId = MenuCategory.SECONDARY.id))
            }
        }

        return resultList
    }

}