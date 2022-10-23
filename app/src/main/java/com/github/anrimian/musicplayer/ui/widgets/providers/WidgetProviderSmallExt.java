package com.github.anrimian.musicplayer.ui.widgets.providers;

import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_REPEAT_MODE;
import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_SHUFFLE_NODE;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeIcon;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtilsKt;
import com.github.anrimian.musicplayer.ui.widgets.WidgetActionsReceiver;


public class WidgetProviderSmallExt extends BaseWidgetProvider {

    @Override
    protected int getRemoteViewId() {
        return R.layout.widget_small_ext;
    }

    @Override
    protected void applyViewLogic(RemoteViews widgetView,
                                  AppWidgetManager appWidgetManager,
                                  int widgetId,
                                  Context context,
                                  int playerState,
                                  String compositionName,
                                  String compositionAuthor,
                                  long compositionId,
                                  long compositionUpdateTime,
                                  long compositionSize,
                                  boolean isFileExists,
                                  int queueSize,
                                  boolean enabled,
                                  boolean showCovers,
                                  boolean randomPlayModeEnabled,
                                  int repeatMode) {
        super.applyViewLogic(widgetView,
                appWidgetManager,
                widgetId,
                context,
                playerState,
                compositionName,
                compositionAuthor,
                compositionId,
                compositionUpdateTime,
                compositionSize,
                isFileExists,
                queueSize,
                enabled,
                showCovers,
                randomPlayModeEnabled,
                repeatMode);

        widgetView.setBoolean(R.id.iv_shuffle_mode, "setEnabled", enabled);
        widgetView.setBoolean(R.id.ivRepeatMode, "setEnabled", enabled);

        if (enabled) {
            int color = ContextCompat.getColor(context,
                    randomPlayModeEnabled? getWidgetAccentColorRes(): R.color.primary_button_color);
            widgetView.setInt(R.id.iv_shuffle_mode, "setColorFilter", color);
        }

        @DrawableRes int iconRes = getRepeatModeIcon(repeatMode);
        widgetView.setImageViewResource(R.id.ivRepeatMode, iconRes);

        Intent intentChangeShuffleMode = new Intent(context, WidgetActionsReceiver.class);
        intentChangeShuffleMode.putExtra(REQUEST_CODE, CHANGE_SHUFFLE_NODE);
        PendingIntent pIntentChangeShuffleMode = PendingIntent.getBroadcast(context,
                CHANGE_SHUFFLE_NODE,
                intentChangeShuffleMode,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));
        widgetView.setOnClickPendingIntent(R.id.iv_shuffle_mode, pIntentChangeShuffleMode);

        Intent intentChangeRepeatMode = new Intent(context, WidgetActionsReceiver.class);
        intentChangeRepeatMode.putExtra(REQUEST_CODE, CHANGE_REPEAT_MODE);
        PendingIntent pIntentChangeRepeatMode = PendingIntent.getBroadcast(context,
                CHANGE_REPEAT_MODE,
                intentChangeRepeatMode,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));
        widgetView.setOnClickPendingIntent(R.id.ivRepeatMode, pIntentChangeRepeatMode);
    }
}
