package com.github.anrimian.musicplayer.ui.common.navigation

import androidx.fragment.app.FragmentManager

interface SpecialNavigation {

    fun attachShortSyncStateFragment(fm: FragmentManager, containerId: Int)

}