package com.github.anrimian.musicplayer.ui.common.format.wrappers

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ItemDrawable

class ItemBackgroundWrapper(rootView: View, clickableView: FrameLayout) {

    private val backgroundDrawable = ItemDrawable()
    private val stateDrawable = ItemDrawable()
    private val rippleMaskDrawable = ItemDrawable()

    init {
        val context = rootView.context
        backgroundDrawable.setColor(context.colorFromAttr(R.attr.listItemBackground))
        rootView.background = backgroundDrawable
        stateDrawable.setColor(Color.TRANSPARENT)
        clickableView.background = stateDrawable
        clickableView.foreground = RippleDrawable(
            ColorStateList.valueOf(context.colorFromAttr(android.R.attr.colorControlHighlight)),
            null,
            rippleMaskDrawable
        )
    }

    fun animateItemDrawableCorners(from: Float, to: Float, duration: Int) {
        AndroidUtils.animateItemDrawableCorners(
            from,
            to,
            duration,
            backgroundDrawable,
            stateDrawable,
            rippleMaskDrawable
        )
    }

    fun showStateColor(@ColorInt color: Int, animate: Boolean) {
        if (animate) {
            stateDrawable.setColor(color)
        } else {
            ViewUtils.animateItemDrawableColor(stateDrawable, color)
        }
    }

    fun setBackgroundColor(@ColorInt color: Int) {
        backgroundDrawable.setColor(color)
    }

}