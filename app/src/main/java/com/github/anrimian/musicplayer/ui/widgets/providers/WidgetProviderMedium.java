package com.github.anrimian.musicplayer.ui.widgets.providers;

import static com.github.anrimian.musicplayer.Constants.Actions.FAST_FORWARD;
import static com.github.anrimian.musicplayer.Constants.Actions.REWIND;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtilsKt;
import com.github.anrimian.musicplayer.ui.widgets.WidgetActionsReceiver;

public class WidgetProviderMedium extends WidgetProviderSmallExt {

    @Override
    protected int getRemoteViewId() {
        return R.layout.widget_medium;
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
        widgetView.setBoolean(R.id.iv_rewind, "setEnabled", enabled);
        widgetView.setBoolean(R.id.iv_fast_forward, "setEnabled", enabled);

        if (showCovers) {
            Components.getAppComponent().imageLoader()
                    .displayImage(widgetView,
                            R.id.iv_cover,
                            widgetId,
                            compositionId,
                            compositionUpdateTime,
                            compositionSize,
                            isFileExists,
                            R.drawable.ic_music_widget_placeholder);
        } else {
            widgetView.setImageViewResource(R.id.iv_cover, R.drawable.ic_music_placeholder);
        }

        Intent intentRewind = new Intent(context, WidgetActionsReceiver.class);
        intentRewind.putExtra(REQUEST_CODE, REWIND);
        PendingIntent pIntentRewind = PendingIntent.getBroadcast(context,
                REWIND,
                intentRewind,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));
        widgetView.setOnClickPendingIntent(R.id.iv_rewind, pIntentRewind);

        Intent intentFastForward = new Intent(context, WidgetActionsReceiver.class);
        intentFastForward.putExtra(REQUEST_CODE, FAST_FORWARD);
        PendingIntent pIntentFastForward = PendingIntent.getBroadcast(context,
                FAST_FORWARD,
                intentFastForward,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));
        widgetView.setOnClickPendingIntent(R.id.iv_fast_forward, pIntentFastForward);
    }
}
