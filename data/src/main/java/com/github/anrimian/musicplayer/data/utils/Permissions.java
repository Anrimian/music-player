package com.github.anrimian.musicplayer.data.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class Permissions {

    public static boolean hasFilePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, getFilePermissionName())
                == PackageManager.PERMISSION_GRANTED;
    }

    public static String getFilePermissionName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_AUDIO;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        } else {
            return Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }
    }
}
