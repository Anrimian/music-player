package com.github.anrimian.musicplayer.ui.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2


fun View.runHighlightAnimation(@ColorInt highlightColor: Int) {
    getHighlightAnimator(Color.TRANSPARENT, highlightColor, ::setBackgroundColor).start()
}

fun getHighlightAnimator(
    @ColorInt colorFrom: Int,
    @ColorInt highlightColor: Int,
    updateListener: (Int) -> Unit
): Animator {
    val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animator ->
        updateListener(animator.animatedValue as Int)
    }
    return AnimatorSet().apply {
        playSequentially(
            ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, highlightColor).apply {
                startDelay = 50
                duration = 300
                interpolator = AccelerateInterpolator()
                addUpdateListener(animatorUpdateListener)
            },
            ValueAnimator.ofObject(ArgbEvaluator(), highlightColor, colorFrom).apply {
                startDelay = 1300
                duration = 300
                interpolator = DecelerateInterpolator()
                addUpdateListener(animatorUpdateListener)
            }
        )
    }
}

fun View.moveToParent(newParent: ViewGroup) {
    val parent = parent as ViewGroup
    parent.removeView(this)
    newParent.addView(this)
}

fun AppCompatActivity.setToolbar(toolbar: Toolbar, @StringRes titleRes: Int) {
    setSupportActionBar(toolbar)
    toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    val actionBar = supportActionBar
    if (actionBar != null) {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setTitle(titleRes)
    }
}

fun ViewPager2.reduceDragSensitivityBy(f: Int) {
    try {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop * f)
    } catch (ignored: NoSuchFieldException) {}
    catch (ignored: IllegalAccessException) {}
}

fun Context.createStyledButton(@StyleRes styleRes: Int): Button {
    return Button(ContextThemeWrapper(this, styleRes), null, styleRes)
}

fun Context.linkify(schema: String, textResId: Int, linkResId: Int): String {
    return "<a href=\"" + schema + getString(linkResId) + "\">" + getString(textResId) + "</a>"
}