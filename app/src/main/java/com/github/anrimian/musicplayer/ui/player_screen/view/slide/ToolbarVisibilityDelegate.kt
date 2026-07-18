package com.github.anrimian.musicplayer.ui.player_screen.view.slide

import android.animation.ArgbEvaluator
import android.view.View
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate

/**
 * Created on 21.01.2018.
 */
class ToolbarVisibilityDelegate(private val toolbar: AdvancedToolbar) : SlideDelegate {

    private val actionMenuView by lazy { toolbar.getActionMenuView() }

    override fun onSlide(slideOffset: Float) {
        if (actionMenuView.isLaidOut) {
            makeVisible(slideOffset)
        } else {
            actionMenuView.post { makeVisible(slideOffset) }
        }
    }

    private fun makeVisible(slideOffset: Float) {
        if (toolbar.isInActionMode()) {
            val startColor = toolbar.context.attrColor(R.attr.colorPrimary)
            val endColor = toolbar.context.attrColor(R.attr.actionModeBackgroundColor)
            val argbEvaluator = ArgbEvaluator()
            val color = argbEvaluator.evaluate(slideOffset, startColor, endColor) as Int
            toolbar.setToolbarBackgroundColor(color)
        } else if (!toolbar.isInSearchMode()) {
            val contentVisibility = if (slideOffset == 0f) View.INVISIBLE else View.VISIBLE
            actionMenuView.visibility = contentVisibility
            actionMenuView.alpha = slideOffset
            toolbar.setContentAlpha(slideOffset)
        }
    }

}
