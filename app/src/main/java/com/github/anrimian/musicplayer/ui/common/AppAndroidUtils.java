package com.github.anrimian.musicplayer.ui.common;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor;
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtilsKt;

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
}
