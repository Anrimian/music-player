package com.github.anrimian.musicplayer.ui.common;

import static com.github.anrimian.utils.AndroidUtilsKt.broadcastPendingIntentFlag;

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
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerType;
import com.github.anrimian.musicplayer.infrastructure.providers.ProviderAuthorities;
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

import javax.annotation.Nullable;

public class AppAndroidUtils {

    public static Uri createUri(Context context, File file) {
        try {
            return FileProvider.getUriForFile(context,
                    ProviderAuthorities.of(context, FileProvider.class),
                    file);
        } catch (Exception e) {
            Toast.makeText(context,
                    context.getString(R.string.file_uri_extract_error, file.getPath()),
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static void playPause(Context context, PlayerInteractor playerInteractor) {
        playPause(context, playerInteractor, null);
    }

    public static void playPause(Context context,
                                 PlayerInteractor playerInteractor,
                                 @Nullable PlayerType playerType) {
        if (playerInteractor.isPlaying()) {
            playerInteractor.pause();
        } else {
            SystemServiceControllerImpl.startPlayForegroundService(context, 0, playerType);
        }
    }

    public static PendingIntent broadcastPendingIntent(Context context,
                                                       int requestCode,
                                                       Intent intent) {
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                broadcastPendingIntentFlag()
        );
    }

    public static void copyText(CoordinatorLayout cl, String text, String label) {
        AndroidUtils.copyText(cl.getContext(), text, label);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            MessagesUtils.makeSnackbar(cl, R.string.copied_message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
