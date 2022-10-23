package com.github.anrimian.musicplayer.ui.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2


fun View.runHighlightAnimation(@ColorInt highlightColor: Int) {
    val colorAnimator = getHighlightAnimator(Color.TRANSPARENT, highlightColor)
    colorAnimator.addUpdateListener { animator -> setBackgroundColor(animator.animatedValue as Int) }
    colorAnimator.start()
}

fun getHighlightAnimator(@ColorInt colorFrom: Int, @ColorInt highlightColor: Int): ValueAnimator {
    val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, highlightColor, colorFrom)
    colorAnimation.interpolator = AccelerateInterpolator()
    colorAnimation.duration = 600
    colorAnimation.repeatCount = 2
    return colorAnimation
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