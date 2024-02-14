package com.github.anrimian.musicplayer.di.app.settings

import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsPresenter
import com.github.anrimian.musicplayer.ui.settings.headset.HeadsetSettingsPresenter
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsPresenter
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsPresenter
import com.github.anrimian.musicplayer.ui.settings.player.impls.EnabledMediaPlayersPresenter
import dagger.Subcomponent

@Subcomponent
interface SettingsComponent {

    fun displaySettingsPresenter(): DisplaySettingsPresenter
    fun playerSettingsPresenter(): PlayerSettingsPresenter
    fun librarySettingsPresenter(): LibrarySettingsPresenter
    fun headsetSettingsPresenter(): HeadsetSettingsPresenter
    fun enabledMediaPlayersPresenter(): EnabledMediaPlayersPresenter

}