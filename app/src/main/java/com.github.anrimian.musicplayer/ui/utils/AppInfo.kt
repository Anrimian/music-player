package com.github.anrimian.musicplayer.ui.utils

import android.content.Context

@Suppress("DEPRECATION")
fun Context.getAppInfo(): AppInfo {
    val pInfo = packageManager.getPackageInfo(packageName, 0)
    return AppInfo(pInfo.versionName, pInfo.versionCode.toLong())
}

class AppInfo(val versionName: String, val versionCode: Long)