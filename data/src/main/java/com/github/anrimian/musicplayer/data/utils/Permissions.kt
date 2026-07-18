package com.github.anrimian.musicplayer.data.utils

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build

fun Uri.hasPersistedReadPermission(context: Context): Boolean {
    return try {
        val persistedPermissions = context.contentResolver.persistedUriPermissions
        val hasPermission = persistedPermissions.any { it.uri == this && it.isReadPermission }
        hasPermission
    } catch (_: Exception) {
        false
    }
}

fun hasBluetoothConnectPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Permissions.hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        true
    }
}