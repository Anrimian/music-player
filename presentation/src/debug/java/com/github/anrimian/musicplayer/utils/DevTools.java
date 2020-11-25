package com.github.anrimian.musicplayer.utils;

import android.app.Application;

import com.github.anrimian.acrareportdialog.AcraReportDialog;

public class DevTools {

    public static void run(Application application) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {//not works in android 11
            AcraReportDialog.setupCrashDialog(application);
//        }
    }
}
