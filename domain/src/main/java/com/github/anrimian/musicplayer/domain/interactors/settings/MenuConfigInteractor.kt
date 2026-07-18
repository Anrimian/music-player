package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.musicplayer.domain.models.menu.AppMenu
import com.github.anrimian.musicplayer.domain.models.menu.MenuConfig
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository

class MenuConfigInteractor(private val settingsRepository: SettingsRepository) {

    fun getMenuConfig(appMenu: AppMenu): MenuConfig? {
        return settingsRepository.getMenuConfig(appMenu)
    }

    fun setMenuConfig(appMenu: AppMenu, menuConfig: MenuConfig?) {
        return settingsRepository.setMenuConfig(appMenu, menuConfig)
    }

}