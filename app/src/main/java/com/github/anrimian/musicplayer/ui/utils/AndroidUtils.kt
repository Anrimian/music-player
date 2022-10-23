package com.github.anrimian.musicplayer.ui.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.annotation.RequiresApi

fun Context.getDimensionPixelSize(@DimenRes resId: Int): Int {
    return resources.getDimensionPixelSize(resId)
}

fun Context.colorFromAttr(@AttrRes attr: Int) = AndroidUtils.getColorFromAttr(this, attr)

fun startAppSettings(activity: Activity) {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.data = Uri.parse("package:${activity.packageName}")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    activity.startActivity(intent)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun startAppLocaleSettings(activity: Activity) {
    val intent = Intent()
    intent.action = Settings.ACTION_APP_LOCALE_SETTINGS
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.data = Uri.parse("package:${activity.packageName}")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    activity.startActivity(intent)
}

fun pIntentFlag(flags: Int = 0): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    PendingIntent.FLAG_IMMUTABLE or flags
} else {
    flags
}

@SuppressLint("ClickableViewAccessibility")
fun View.onMotionDown(callback: () -> Unit) {
    setOnTouchListener { _, event ->
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            callback()
        }
        return@setOnTouchListener false
    }
}

fun showSystemColors(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        fun ViewGroup.addColorRow(colorResId: Int, description: String) {
            addView(TextView(context).apply {
                val color = ContextCompat.getColor(context, colorResId)
                setBackgroundColor(color)
                text = description
                setTextColor(if (ColorUtils.calculateLuminance(color) >= 0.5f) {
                    Color.BLACK
                } else {
                    Color.WHITE
                })
                setPadding(8, 0, 8, 0)
            })
        }

        val rootView = LinearLayout(context)
        rootView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rootView.orientation = LinearLayout.HORIZONTAL

        val ac1View = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            addColorRow(android.R.color.system_accent1_0, "ac1_0")
            addColorRow(android.R.color.system_accent1_10, "ac1_10")
            addColorRow(android.R.color.system_accent1_50, "ac1_50")
            addColorRow(android.R.color.system_accent1_100, "ac1_100")
            addColorRow(android.R.color.system_accent1_200, "ac1_200")
            addColorRow(android.R.color.system_accent1_300, "ac1_300")
            addColorRow(android.R.color.system_accent1_400, "ac1_400")
            addColorRow(android.R.color.system_accent1_500, "ac1_500")
            addColorRow(android.R.color.system_accent1_600, "ac1_600")
            addColorRow(android.R.color.system_accent1_700, "ac1_700")
            addColorRow(android.R.color.system_accent1_800, "ac1_800")
            addColorRow(android.R.color.system_accent1_900, "ac1_900")
            addColorRow(android.R.color.system_accent1_1000, "ac1_1000")
        }
        rootView.addView(ac1View)

        val ac2View = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            addColorRow(android.R.color.system_accent2_0, "ac2_0")
            addColorRow(android.R.color.system_accent2_10, "ac2_10")
            addColorRow(android.R.color.system_accent2_50, "ac2_50")
            addColorRow(android.R.color.system_accent2_100, "ac2_100")
            addColorRow(android.R.color.system_accent2_200, "ac2_200")
            addColorRow(android.R.color.system_accent2_300, "ac2_300")
            addColorRow(android.R.color.system_accent2_400, "ac2_400")
            addColorRow(android.R.color.system_accent2_500, "ac2_500")
            addColorRow(android.R.color.system_accent2_600, "ac2_600")
            addColorRow(android.R.color.system_accent2_700, "ac2_700")
            addColorRow(android.R.color.system_accent2_800, "ac2_800")
            addColorRow(android.R.color.system_accent2_900, "ac2_900")
            addColorRow(android.R.color.system_accent2_1000, "ac2_1000")
        }
        rootView.addView(ac2View)

        val ac3View = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            addColorRow(android.R.color.system_accent3_0, "ac3_0")
            addColorRow(android.R.color.system_accent3_10, "ac3_10")
            addColorRow(android.R.color.system_accent3_50, "ac3_50")
            addColorRow(android.R.color.system_accent3_100, "ac3_100")
            addColorRow(android.R.color.system_accent3_200, "ac3_200")
            addColorRow(android.R.color.system_accent3_300, "ac3_300")
            addColorRow(android.R.color.system_accent3_400, "ac3_400")
            addColorRow(android.R.color.system_accent3_500, "ac3_500")
            addColorRow(android.R.color.system_accent3_600, "ac3_600")
            addColorRow(android.R.color.system_accent3_700, "ac3_700")
            addColorRow(android.R.color.system_accent3_800, "ac3_800")
            addColorRow(android.R.color.system_accent3_900, "ac3_900")
            addColorRow(android.R.color.system_accent3_1000, "ac3_1000")
        }
        rootView.addView(ac3View)

        val n1View = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            addColorRow(android.R.color.system_neutral1_0, "n1_0")
            addColorRow(android.R.color.system_neutral1_10, "n1_10")
            addColorRow(android.R.color.system_neutral1_50, "n1_50")
            addColorRow(android.R.color.system_neutral1_100, "n1_100")
            addColorRow(android.R.color.system_neutral1_200, "n1_200")
            addColorRow(android.R.color.system_neutral1_300, "n1_300")
            addColorRow(android.R.color.system_neutral1_400, "n1_400")
            addColorRow(android.R.color.system_neutral1_500, "n1_500")
            addColorRow(android.R.color.system_neutral1_600, "n1_600")
            addColorRow(android.R.color.system_neutral1_700, "n1_700")
            addColorRow(android.R.color.system_neutral1_800, "n1_800")
            addColorRow(android.R.color.system_neutral1_900, "n1_900")
            addColorRow(android.R.color.system_neutral1_1000, "n1_1000")
        }
        rootView.addView(n1View)

        val n2View = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            addColorRow(android.R.color.system_neutral2_0, "n2_0")
            addColorRow(android.R.color.system_neutral2_10, "n2_10")
            addColorRow(android.R.color.system_neutral2_50, "n2_50")
            addColorRow(android.R.color.system_neutral2_100, "n2_100")
            addColorRow(android.R.color.system_neutral2_200, "n2_200")
            addColorRow(android.R.color.system_neutral2_300, "n2_300")
            addColorRow(android.R.color.system_neutral2_400, "n2_400")
            addColorRow(android.R.color.system_neutral2_500, "n2_500")
            addColorRow(android.R.color.system_neutral2_600, "n2_600")
            addColorRow(android.R.color.system_neutral2_700, "n2_700")
            addColorRow(android.R.color.system_neutral2_800, "n2_800")
            addColorRow(android.R.color.system_neutral2_900, "n2_900")
            addColorRow(android.R.color.system_neutral2_1000, "n2_1000")
        }
        rootView.addView(n2View)

        AlertDialog.Builder(context)
            .setView(rootView)
            .setNegativeButton(android.R.string.cancel) {_,_ -> }
            .create()
            .apply {
                setCanceledOnTouchOutside(false)
                show()
            }
    }
}
