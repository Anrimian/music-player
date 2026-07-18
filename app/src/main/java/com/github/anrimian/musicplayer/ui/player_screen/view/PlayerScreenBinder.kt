package com.github.anrimian.musicplayer.ui.player_screen.view

import android.view.MenuItem
import androidx.annotation.DrawableRes
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.interactors.player.screen.ActionState
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.google.android.material.navigation.NavigationView

open class PlayerScreenBinder {

    open fun setupMenu(navigationView: NavigationView) {
        navigationView.inflateMenu(R.menu.drawer_menu)
    }

    open fun onNavigationItemSelected(item: MenuItem, navigationView: NavigationView): Boolean {
        return false
    }

    open fun bindActionState(toolbar: AdvancedToolbar, actionState: ActionState) {
        val iconRes = getActionIcon(actionState)
        toolbar.setNavigationButtonHintIcon(iconRes)
    }

    @DrawableRes
    protected open fun getActionIcon(actionState: ActionState): Int {
        return when(actionState) {
            ActionState.ACTION_REQUIRED -> R.drawable.ic_alert
            else -> -1
        }
    }
}