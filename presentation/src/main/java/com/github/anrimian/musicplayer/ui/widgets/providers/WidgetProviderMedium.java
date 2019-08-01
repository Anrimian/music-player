package com.github.anrimian.musicplayer.ui.widgets.providers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.format.ImageFormatUtils;
import com.github.anrimian.musicplayer.ui.widgets.WidgetActionsReceiver;

import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_REPEAT_MODE;
import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_SHUFFLE_NODE;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeIcon;


public class WidgetProviderMedium extends BaseWidgetProvider {

    @Override
    protected void applyViewLogic(RemoteViews widgetView,
                                  Context context,
                                  boolean play,
                                  String compositionName,
                                  String compositionAuthor,
                                  String compositionFile,
                                  long compositionId,
                                  int queueSize,
                                  boolean enabled,
                                  boolean showCovers,
                                  boolean randomPlayModeEnabled,
                                  int repeatMode) {
        super.applyViewLogic(widgetView,
                context,
                play,
                compositionName,
                compositionAuthor,
                compositionFile,
                compositionId,
                queueSize,
                enabled,
                showCovers,
                randomPlayModeEnabled,
                repeatMode);
        widgetView.setBoolean(R.id.iv_shuffle_mode, "setEnabled", enabled);
        widgetView.setBoolean(R.id.iv_repeat_mode, "setEnabled", enabled);

        if (enabled) {
            int color = ContextCompat.getColor(context,
                    randomPlayModeEnabled? R.color.colorAccent: R.color.primary_button_color);
            widgetView.setInt(R.id.iv_shuffle_mode, "setColorFilter", color);
        }

        @DrawableRes int iconRes = getRepeatModeIcon(repeatMode);
        widgetView.setImageViewResource(R.id.iv_repeat_mode, iconRes);

        if (showCovers) {
            ImageFormatUtils.displayImage(widgetView, R.id.iv_cover, compositionFile, compositionId);
        } else {
            widgetView.setImageViewResource(R.id.iv_cover, R.drawable.ic_music_placeholder);
        }

        Intent intentChangeShuffleMode = new Intent(context, WidgetActionsReceiver.class);
        intentChangeShuffleMode.putExtra(REQUEST_CODE, CHANGE_SHUFFLE_NODE);
        PendingIntent pIntentChangeShuffleMode = PendingIntent.getBroadcast(context,
                CHANGE_SHUFFLE_NODE,
                intentChangeShuffleMode,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.iv_shuffle_mode, pIntentChangeShuffleMode);

        Intent intentChangeRepeatMode = new Intent(context, WidgetActionsReceiver.class);
        intentChangeRepeatMode.putExtra(REQUEST_CODE, CHANGE_REPEAT_MODE);
        PendingIntent pIntentChangeRepeatMode = PendingIntent.getBroadcast(context,
                CHANGE_REPEAT_MODE,
                intentChangeRepeatMode,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.iv_repeat_mode, pIntentChangeRepeatMode);
    }

    @Override
    protected int getRemoteViewId() {
        return R.layout.widget_medium;
    }
}