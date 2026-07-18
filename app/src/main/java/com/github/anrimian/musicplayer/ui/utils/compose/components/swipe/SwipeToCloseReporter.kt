package com.github.anrimian.musicplayer.ui.utils.compose.components.swipe

import androidx.compose.runtime.Immutable
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation

//tmp class
@Immutable
class SwipeToCloseReporter(
    private val navigation: FragmentNavigation,
    private val toolbar: AdvancedToolbar
) {

    fun onDismiss() {
        navigation.goBack(0)
    }

    fun onDragProgress(progress: Float) {
        if (navigation.screensCount <= 2) {
            toolbar.setNavigationButtonProgress(1f - progress)
        }
    }

}