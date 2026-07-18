package com.github.anrimian.musicplayer.ui.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.addListener
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2


fun View.onLongClick(onClick: () -> Unit) {
    setOnLongClickListener { v ->
        onClick()
        true
    }
}

fun CompoundButton.onCheckChanged(listener: (Boolean) -> Unit) {
    setOnCheckedChangeListener { _, isChecked -> listener(isChecked) }
}

fun CompoundButton.setCheckedImmediately(checked: Boolean) {
    if (isChecked != checked) {
        isChecked = checked
        jumpDrawablesToCurrentState()
    }
}

fun View.runHighlightAnimation(@ColorInt highlightColor: Int) {
    getHighlightAnimator(Color.TRANSPARENT, highlightColor, ::setBackgroundColor).start()
}

fun getHighlightAnimator(
    @ColorInt colorFrom: Int,
    @ColorInt highlightColor: Int,
    updateListener: (Int) -> Unit,
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

fun AppCompatActivity.setToolbar(toolbar: Toolbar, @StringRes titleRes: Int = 0) {
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

fun ViewPager2.setCurrentItem(
    item: Int,
    duration: Long,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    pagePxWidth: Int = width, // Default value taken from getWidth() from ViewPager2 view
) {
    val pxToDrag: Int = pagePxWidth * (item - currentItem)
    val animator = ValueAnimator.ofInt(0, pxToDrag)
    var previousValue = 0
    animator.addUpdateListener { valueAnimator ->
        val currentValue = valueAnimator.animatedValue as Int
        val currentPxToDrag = (currentValue - previousValue).toFloat()
        fakeDragBy(-currentPxToDrag)
        previousValue = currentValue
    }
    animator.addListener(onStart = { beginFakeDrag() }, onEnd = { endFakeDrag() })
    animator.interpolator = interpolator
    animator.duration = duration
    animator.start()
}

fun ViewPager2.onPageSelected(onPageSelected: (position: Int) -> Unit) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onPageSelected(position)
        }
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    })
}

fun ViewPager2.onPageScrolled(
    onScrolled: (
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int,
    ) -> Unit,
) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {}

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
            if (positionOffset < 0f || positionOffset > 1f) {
                return
            }
            onScrolled(position, positionOffset, positionOffsetPixels)
        }
    })
}

fun Context.createStyledButton(@StyleRes styleRes: Int): Button {
    return Button(ContextThemeWrapper(this, styleRes), null, styleRes)
}

fun Context.linkify(schema: String, textResId: Int, linkResId: Int): String {
    return "<a href=\"" + schema + getString(linkResId) + "\">" + getString(textResId) + "</a>"
}

fun Context.linkify(schema: String, textResId: Int, link: String): String {
    return "<a href=\"" + schema + link + "\">" + getString(textResId) + "</a>"
}

fun EditText.getCursorY(): Float {
    val pos = selectionStart
    val layout = layout ?: return -1f
    val line = layout.getLineForOffset(pos)
    val baseline = layout.getLineBaseline(line)
    val ascent = layout.getLineAscent(line)
    return (baseline + ascent).toFloat()
}

fun DrawerLayout.attachBackPressedCallback(
    activity: FragmentActivity,
    drawerGravity: Int = GravityCompat.START,
) {
    val onBackPressedCallback = object : OnBackPressedCallback(isDrawerOpen(drawerGravity)) {
        override fun handleOnBackPressed() {
            if (isDrawerOpen(drawerGravity)) {
                closeDrawer(drawerGravity)
            }
        }
    }
    activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
    addDrawerStateListener(
        onDrawerSlide = { _, slideOffset ->
            onBackPressedCallback.isEnabled = slideOffset > 0
        },
    )
}

inline fun DrawerLayout.addDrawerStateListener(
    crossinline onDrawerSlide: (drawerView: View, slideOffset: Float) -> Unit = { _, _ -> },
    crossinline onDrawerOpened: (drawerView: View) -> Unit = {},
    crossinline onDrawerClosed: (drawerView: View) -> Unit = {},
) {
    addDrawerListener(object : DrawerLayout.DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            onDrawerSlide(drawerView, slideOffset)
        }

        override fun onDrawerOpened(drawerView: View) {
            onDrawerOpened(drawerView)
        }

        override fun onDrawerClosed(drawerView: View) {
            onDrawerClosed(drawerView)
        }

        override fun onDrawerStateChanged(newState: Int) {}
    })
}

fun TextView.getCharY(charIndex: Int): Int {
    layout ?: return -1 // Layout may be null right after change to the text view

    val lineOfText = layout.getLineForOffset(charIndex)
    return layout.getLineTop(lineOfText)
}

@SuppressLint("ClickableViewAccessibility")
fun View.doOnTouch(action: (MotionEvent) -> Unit) {
    setOnTouchListener { _, event ->
        action(event)
        return@setOnTouchListener false
    }
}