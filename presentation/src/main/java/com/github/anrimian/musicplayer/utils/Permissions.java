package com.github.anrimian.musicplayer.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class Permissions {

    public static boolean hasFilePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
}
