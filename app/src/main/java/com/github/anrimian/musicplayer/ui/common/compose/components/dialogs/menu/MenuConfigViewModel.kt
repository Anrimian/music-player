package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.interactors.settings.MenuConfigInteractor
import com.github.anrimian.musicplayer.domain.models.menu.AppMenu
import com.github.anrimian.musicplayer.domain.models.menu.MenuCategory
import com.github.anrimian.musicplayer.domain.models.menu.MenuConfig
import com.github.anrimian.musicplayer.domain.models.menu.MenuItemConfig
import com.github.anrimian.musicplayer.ui.common.compose.components.dialogs.menu.models.MenuConfigListItem
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.menu.utils.MenuConfigUtil
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuDefinitions
import com.github.anrimian.musicplayer.ui.common.models.menu.MenuIds
import com.github.anrimian.musicplayer.ui.common.mvvm.SimpleViewModel

class MenuConfigViewModel(
    private val menuConfigInteractor: MenuConfigInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
) : SimpleViewModel<MenuConfigState>(
    initialState = MenuConfigState(),
    savedStateHandle = savedStateHandle,
    errorParser = errorParser
) {

    private val appMenu = getArgs<MenuConfigDialogData>().menu

    init {
        loadItems()
    }

    fun onItemMove(from: Int, to: Int) {
        val list = currentState.items
        list.apply { add(to, removeAt(from)) }
    }

    fun onSave() {
        val uiOrder = currentState.items.toList()
        val resultConfig = mutableListOf<MenuItemConfig>()

        var currentCategory: MenuCategory = MenuCategory.PRIMARY
        for (row in uiOrder) {
            when (row) {
                is MenuConfigListItem.Header -> {
                    currentCategory = row.category
                }
                is MenuConfigListItem.Item -> {
                    resultConfig.add(MenuItemConfig(row.appMenuItem.id, currentCategory))
                }
            }
        }

        menuConfigInteractor.setMenuConfig(appMenu, MenuConfig(resultConfig))
        sendEffect(MenuConfigEffect.Close)
    }

    fun onReset() {
        menuConfigInteractor.setMenuConfig(appMenu, null)
        sendEffect(MenuConfigEffect.Close)
    }

    private fun loadItems() {
        val mergedItems = MenuConfigUtil.applyConfig(
            defaultItems = getMenuItems(),
            config = menuConfigInteractor.getMenuConfig(appMenu),
            includeHidden = true
        )

        val uiList = ArrayList<MenuConfigListItem>(mergedItems.size + 3)

        fun addSection(category: MenuCategory, titleRes: Int) {
            uiList.add(MenuConfigListItem.Header(category, titleRes))

            val targetGroupId = category.id
            for (i in mergedItems.indices) {
                val item = mergedItems[i]
                if (item.id != MenuIds.MENU_CONFIG && item.groupId == targetGroupId) {
                    uiList.add(MenuConfigListItem.Item(item))
                }
            }
        }

        addSection(MenuCategory.PRIMARY, R.string.menu_group_primary)
        addSection(MenuCategory.SECONDARY, R.string.menu_group_secondary)
        addSection(MenuCategory.HIDDEN, R.string.menu_group_hidden)

        currentState.items.addAll(uiList)
    }

    private fun getMenuItems() = when (appMenu) {
        AppMenu.PLAYLIST -> AppMenuDefinitions.PlaylistMenuItems
        AppMenu.PLAYLIST_ENTRY -> AppMenuDefinitions.PlaylistEntryMenuItems
    }
}