package com.github.anrimian.musicplayer.ui.utils

import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDialog
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.min


fun View.applyConsumeTopInsets() {
    var viewHeight: Int? = null
    val paddingTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.LayoutParams> {
            if (viewHeight == null) {
                viewHeight = height
            }
            height = viewHeight!! + insets.top
        }
        view.updatePadding(top = paddingTop + insets.top)

        val newInsets = Insets.of(insets.left, 0, insets.right, insets.bottom)
        WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.systemBars(), newInsets)
            .build()
    }
    requestApplyInsetsWhenAttached()
}

fun View.applyTopInsets() {
    var viewHeight: Int? = null
    val paddingTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.LayoutParams> {
            if (viewHeight == null) {
                viewHeight = height
            }
            height = viewHeight!! + insets.top
        }
        view.updatePadding(top = paddingTop + insets.top)

        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun View.applyConstraintTopMarginInsets() {
    val marginTop = marginTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topMargin = marginTop + insets.top
        }
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun AdvancedToolbar.applyTopInsets() {
    var toolbarHeight: Int? = null
    val paddingTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.LayoutParams> {
            if (toolbarHeight == null) {
                toolbarHeight = height
            }
            height = toolbarHeight!! + insets.top
        }
        view.updatePadding(top = paddingTop + insets.top)
        this@applyTopInsets.setStatusBarHeight(insets.top)
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun View.applyBottomInsets() {
    val paddingBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(bottom = paddingBottom + insets.bottom)
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun View.ignoreBottomInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        val newInsets = Insets.of(insets.left, insets.top, insets.right, 0)
        WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.systemBars(), newInsets)
            .build()
    }
}

fun View.applyCoordinatorBottomMarginInsets() {
    val marginBottom = (layoutParams as CoordinatorLayout.LayoutParams).bottomMargin
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            bottomMargin = marginBottom + insets.bottom
        }
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun View.applyBottomMarginInsets() {
    val marginBottom = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = marginBottom + insets.bottom
        }
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun MotionLayout.applyBottomInsets(viewHeight: Int) {
    val behavior = BottomSheetBehavior.from<View>(this)
    val peekHeight = behavior.peekHeight
    val paddingBottom = paddingBottom
    fitsSystemWindows = false
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        if (viewHeight != ViewGroup.LayoutParams.MATCH_PARENT) {
            view.updateLayoutParams<ViewGroup.LayoutParams> {
                height = viewHeight + insets.bottom
            }
        }
        behavior.peekHeight = peekHeight + insets.bottom
        view.updatePadding(bottom = paddingBottom + insets.bottom)
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun View.applyBottomHeightInsets() {
    var viewHeight: Int? = null
    val paddingBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.LayoutParams> {
            if (viewHeight == null) {
                viewHeight = height
            }
            height = viewHeight!! + insets.bottom
        }
        view.updatePadding(bottom = paddingBottom + insets.bottom)
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun ViewGroup.applyHorizontalInsets() {
    val paddingLeft = paddingLeft
    val paddingRight = paddingRight
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = paddingLeft + insets.left,
            right = paddingRight + insets.right
        )
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun View.applyBottomHorizontalInsets() {
    val paddingBottom = paddingBottom
    val paddingLeft = paddingLeft
    val paddingRight = paddingRight
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            bottom = paddingBottom + insets.bottom,
            left = paddingLeft + insets.left,
            right = paddingRight + insets.right
        )
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun EditText.applyImeBottomPaddingInsets(activity: Activity, scrollView: NestedScrollView) {
    applyImeBottomPaddingInsets(activity, scrollView) { editText -> editText.getCursorY() }
}

fun <T : View> T.applyImeBottomPaddingInsets(
    activity: Activity,
    scrollView: NestedScrollView,
    focusYProvider: (T) -> Float
) {
    val paddingBottom = paddingBottom
    applyImeBaseInsets(activity, paddingBottom) { value, _ ->
        val focusY = focusYProvider(this)
        if (focusY != -1f) {
            val scrollY = scrollView.scrollY
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val cursorBottomOffset = screenHeight - paddingBottom - (focusY - scrollY)
            val cursorDiff = value - cursorBottomOffset.toInt()
            if (cursorDiff > 0) {
                scrollView.scrollBy(0, cursorDiff)
            }
        }
        updatePadding(bottom = value)
    }
}

fun View.applyImeConstraintBottomMarginInsets(activity: Activity) {
    applyImeBaseInsets(activity, marginBottom) { value, _ ->
        updateLayoutParams<ConstraintLayout.LayoutParams> {
            bottomMargin = value
        }
    }
}

fun View.applyImeBottomPaddingInsets(activity: Activity) {
    applyImeBaseInsets(activity, paddingBottom) { value, _ ->
        updatePadding(bottom = value)
    }
}

fun View.applyImeOffsetBottomPaddingInsets(activity: Activity, bottomOffset: Int) {
    val initialValue = paddingBottom
    applyImeBaseInsets(activity, paddingBottom) { value, windowInsets ->
        val systemBarInset = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        val yDiff = value - bottomOffset - systemBarInset + initialValue
        if (yDiff >= 0 && paddingBottom != yDiff) {
            updatePadding(bottom = yDiff)
        }
    }
}

fun View.applyImeBaseInsets(
    activity: Activity,
    initialValue: Int,
    valueSetCallback: (value: Int, windowInsets: WindowInsetsCompat) -> Unit,
) {
    var systemBarInset = 0
    var isImeAnimationRunning = false
    var lastExpandAnimatedValue = 0
    var isExpandStateInitialized = false
    var isExpanded = false
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val imeInset = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        if (!isExpandStateInitialized) {
            isExpandStateInitialized = true
            isExpanded = imeInset > 0
        }
        val baseImeInset = initialValue + imeInset
        systemBarInset = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        if (isImeAnimationRunning || imeInset > 0) {
            // if we're in multi-window mode, insets animation callback won't be called here
            // so set it directly
            if (!activity.isMultiWindowMode()) {
                // check if ime value was changed during expand animation.
                // If so, set new value and ignore further set.
                if (lastExpandAnimatedValue != 0 && baseImeInset > lastExpandAnimatedValue) {
                    valueSetCallback(baseImeInset, windowInsets)
                    return@setOnApplyWindowInsetsListener windowInsets
                }
                //we animated this value, ignore set
                return@setOnApplyWindowInsetsListener windowInsets
            }
        }
        val value = baseImeInset + systemBarInset
        valueSetCallback(value, windowInsets)

        windowInsets
    }
    ViewCompat.setWindowInsetsAnimationCallback(
        this,
        object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

            private var minValue = 0
            private var valueDiff = 0

            override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                super.onPrepare(animation)
                isImeAnimationRunning = animation.typeMask and WindowInsetsCompat.Type.ime() != 0
            }

            override fun onStart(
                animation: WindowInsetsAnimationCompat,
                bounds: WindowInsetsAnimationCompat.BoundsCompat,
            ): WindowInsetsAnimationCompat.BoundsCompat {
                val lowerBound = initialValue + bounds.lowerBound.bottom +
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) systemBarInset else 0
                val upperBound = initialValue + bounds.upperBound.bottom
                valueDiff = upperBound - lowerBound
                minValue = min(upperBound, lowerBound)
                return bounds
            }

            override fun onProgress(
                windowInsets: WindowInsetsCompat,
                runningAnimations: MutableList<WindowInsetsAnimationCompat>,
            ): WindowInsetsCompat {
                val imeAnimation = runningAnimations.find { anim ->
                    anim.typeMask and WindowInsetsCompat.Type.ime() != 0
                } ?: return windowInsets

                val interpolatedFraction = if (isExpanded) {
                    (1 - imeAnimation.interpolatedFraction)
                } else {
                    imeAnimation.interpolatedFraction
                }
                val animatedValue = minValue + (valueDiff * interpolatedFraction).toInt()

                valueSetCallback(animatedValue, windowInsets)

                return windowInsets
            }

            override fun onEnd(animation: WindowInsetsAnimationCompat) {
                super.onEnd(animation)
                if (animation.typeMask and WindowInsetsCompat.Type.ime() == 0) {
                    return
                }
                lastExpandAnimatedValue = if (isExpanded) 0 else minValue + valueDiff
                isExpanded = !isExpanded
                isImeAnimationRunning = false
            }

        }
    )
    requestApplyInsetsWhenAttached()
}


fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

@Suppress("DEPRECATION")
fun Activity.attachSystemBarsColor(@ColorInt bottomColor: Int, @ColorInt topColor: Int) {
    val decorView = window.decorView
    var flags = decorView.systemUiVisibility
    if (ColorUtils.calculateLuminance(topColor) >= 0.5f) {//white
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        if (flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR == View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) {
            flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
    decorView.systemUiVisibility = flags

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        //do not draw nav bar on the old versions
        return
    }

    val navMode = Settings.Secure.getInt(contentResolver, "navigation_mode", -1)
    if (navMode > 1) {
        //gesture navigation -> do nothing
        return
    }
    window.navigationBarColor = bottomColor
    if (ColorUtils.calculateLuminance(bottomColor) >= 0.5f) {//white
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    } else {
        if (flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR == View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) {
            flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }
    decorView.systemUiVisibility = flags
}

@Suppress("DEPRECATION")
fun AppCompatDialog.attachSystemBarsColor(backgroundColor: Int) {
    val window = window ?: return

    //required to draw behind navigation bar
    WindowCompat.setDecorFitsSystemWindows(window, false)
    findViewById<View>(com.google.android.material.R.id.container)!!.fitsSystemWindows = false
    findViewById<View>(com.google.android.material.R.id.coordinator)!!.fitsSystemWindows = false

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
        //system handles it, do nothing
        return
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        //do not draw nav bar on the old versions
        return
    }
    val navMode = Settings.Secure.getInt(context.contentResolver, "navigation_mode", -1)
    if (navMode > 1) {
        //gesture navigation -> do nothing
        return
    }
    if (ColorUtils.calculateLuminance(backgroundColor) >= 0.5f) {//white
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
}