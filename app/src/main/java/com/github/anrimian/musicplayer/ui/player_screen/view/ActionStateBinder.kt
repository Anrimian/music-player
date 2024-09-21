package com.github.anrimian.musicplayer.ui.player_screen.view

import com.github.anrimian.musicplayer.domain.interactors.player.ActionState
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar

interface ActionStateBinder {

    fun bind(toolbar: AdvancedToolbar, actionState: ActionState)

}