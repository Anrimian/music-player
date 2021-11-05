package com.github.anrimian.musicplayer.ui.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes

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
