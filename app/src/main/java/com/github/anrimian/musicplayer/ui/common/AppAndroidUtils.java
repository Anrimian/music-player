package com.github.anrimian.musicplayer.ui.common;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor;
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtilsKt;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class AppAndroidUtils {

    public static Uri createUri(Context context, File file) {
        try {
            return FileProvider.getUriForFile(context,
                    context.getString(R.string.file_provider_authorities),
                    file);
        } catch (Exception e) {
            Toast.makeText(context,
                    context.getString(R.string.file_uri_extract_error, file.getPath()),
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static void playPause(Context context, PlayerInteractor playerInteractor) {
        if (playerInteractor.isPlaying()) {
            playerInteractor.pause();
        } else {
            SystemServiceControllerImpl.startPlayForegroundService(context);
        }
    }

    public static PendingIntent broadcastPendingIntent(Context context,
                                                       int requestCode,
                                                       Intent intent) {
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                AndroidUtilsKt.broadcastPendingIntentFlag()
        );
    }

    public static void copyText(CoordinatorLayout cl, String text, String label) {
        AndroidUtils.copyText(cl.getContext(), text, label);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            MessagesUtils.makeSnackbar(cl, R.string.copied_message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
