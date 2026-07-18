package com.github.anrimian.musicplayer.data.repositories.settings

import com.github.anrimian.musicplayer.domain.models.menu.MenuCategory
import com.github.anrimian.musicplayer.domain.models.menu.MenuConfig
import com.github.anrimian.musicplayer.domain.models.menu.MenuItemConfig

object MenuConfigSerializer {

    private const val ITEM_DELIMITER = ","
    private const val VALUE_DELIMITER = ":"

    fun serialize(config: MenuConfig?): String? {
        if (config == null) {
            return null
        }
        return config.items.joinToString(ITEM_DELIMITER) { item ->
            "${item.id}$VALUE_DELIMITER${item.category.id}"
        }
    }

    fun deserialize(data: String?): MenuConfig? {
        if (data.isNullOrBlank()) {
            return null
        }
        
        val items = data.split(ITEM_DELIMITER).mapNotNull { entry ->
            val parts = entry.split(VALUE_DELIMITER)
            if (parts.size == 2) {
                val id = parts[0].toIntOrNull()
                val catId = parts[1].toIntOrNull()
                val category = MenuCategory.entries.find { category -> category.id == catId }
                    ?: MenuCategory.PRIMARY
                
                if (id != null) {
                    MenuItemConfig(id, category)
                } else {
                    null
                }
            } else {
                null
            }
        }
        return MenuConfig(items)
    }

}