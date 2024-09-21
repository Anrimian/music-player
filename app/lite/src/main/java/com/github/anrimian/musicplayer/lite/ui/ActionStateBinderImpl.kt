package com.github.anrimian.musicplayer.lite.ui

import com.github.anrimian.musicplayer.domain.interactors.player.ActionState
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.player_screen.view.ActionStateBinder

class ActionStateBinderImpl: ActionStateBinder {

    override fun bind(toolbar: AdvancedToolbar, actionState: ActionState) {
        toolbar.setNavigationButtonHintIcon(-1)
    }

}