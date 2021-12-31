package com.github.anrimian.musicplayer.ui.widgets.providers;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_AUTHOR_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_NAME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_UPDATE_TIME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COVERS_ENABLED_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAY_QUEUE_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.QUEUE_SIZE_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.RANDOM_PLAY_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.REPEAT_ARG;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater.UPDATE_FROM_INTENT;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtilsKt;
import com.github.anrimian.musicplayer.ui.widgets.WidgetActionsReceiver;
import com.github.anrimian.musicplayer.ui.widgets.WidgetDataHolder;


public abstract class BaseWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager == null) {
            return;
        }
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());

        String compositionName;
        String compositionAuthor;
        long compositionId;
        long compositionUpdateTime;
        int queueSize;
        boolean play = false;
        boolean randomPlayModeEnabled;
        int repeatMode;
        boolean showCovers;
        if (intent.getBooleanExtra(UPDATE_FROM_INTENT, false)) {
            compositionName = intent.getStringExtra(COMPOSITION_NAME_ARG);
            compositionAuthor = intent.getStringExtra(COMPOSITION_AUTHOR_ARG);
            compositionId = intent.getLongExtra(COMPOSITION_ID_ARG, 0);
            compositionUpdateTime = intent.getLongExtra(COMPOSITION_UPDATE_TIME_ARG, 0);
            queueSize = intent.getIntExtra(QUEUE_SIZE_ARG, 0);
            play = intent.getBooleanExtra(PLAY_ARG, false);
            randomPlayModeEnabled = intent.getBooleanExtra(RANDOM_PLAY_ARG, false);
            repeatMode = intent.getIntExtra(REPEAT_ARG, RepeatMode.NONE);
            showCovers = intent.getBooleanExtra(COVERS_ENABLED_ARG, false);
        } else {
            compositionName = WidgetDataHolder.getCompositionName(context);
            compositionAuthor = WidgetDataHolder.getCompositionAuthor(context);
            compositionId = WidgetDataHolder.getCompositionId(context);
            compositionUpdateTime = WidgetDataHolder.getCompositionUpdateTime(context);
            queueSize = WidgetDataHolder.getCurrentQueueSize(context);
            randomPlayModeEnabled = WidgetDataHolder.isRandomPlayModeEnabled(context);
            repeatMode = WidgetDataHolder.getRepeatMode(context);
            showCovers = WidgetDataHolder.isShowCoversEnabled(context);
        }

        boolean enabled = true;
        if (isEmpty(compositionName)) {
            compositionName = context.getString(R.string.no_current_composition);
            compositionAuthor = null;
            enabled = false;
        }

        int[] ids = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int widgetId : ids) {
            updateWidget(context,
                    appWidgetManager,
                    widgetId,
                    play,
                    compositionName,
                    compositionAuthor,
                    compositionId,
                    compositionUpdateTime,
                    queueSize,
                    enabled,
                    showCovers,
                    randomPlayModeEnabled,
                    repeatMode);
        }
    }

    protected void applyViewLogic(RemoteViews widgetView,
                                  AppWidgetManager appWidgetManager,
                                  int widgetId,
                                  Context context,
                                  boolean play,
                                  String compositionName,
                                  String compositionAuthor,
                                  long compositionId,
                                  long compositionUpdateTime,
                                  int queueSize,
                                  boolean enabled,
                                  boolean showCovers,
                                  boolean randomPlayModeEnabled,
                                  int repeatMode) {
        widgetView.setBoolean(R.id.iv_skip_to_previous, "setEnabled", enabled);
        widgetView.setBoolean(R.id.iv_play_pause, "setEnabled", enabled);
        widgetView.setBoolean(R.id.iv_skip_to_next, "setEnabled", enabled && queueSize > 1);
        widgetView.setTextViewText(R.id.tv_composition, compositionName);
        widgetView.setTextViewText(R.id.tv_composition_author, compositionAuthor);

        widgetView.setImageViewResource(R.id.iv_play_pause, play? R.drawable.ic_pause: R.drawable.ic_play);

        int requestCode = play? PAUSE : PLAY;
        Intent intentPlayPause = new Intent(context, WidgetActionsReceiver.class);
        intentPlayPause.putExtra(REQUEST_CODE, requestCode);
        PendingIntent pIntentPlayPause = PendingIntent.getBroadcast(context,
                requestCode,
                intentPlayPause,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));
        widgetView.setOnClickPendingIntent(R.id.iv_play_pause, pIntentPlayPause);

        Intent intentSkipToPrevious = new Intent(context, WidgetActionsReceiver.class);
        intentSkipToPrevious.putExtra(REQUEST_CODE, SKIP_TO_PREVIOUS);
        PendingIntent pIntentSkipToPrevious = PendingIntent.getBroadcast(context,
                SKIP_TO_PREVIOUS,
                intentSkipToPrevious,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));
        widgetView.setOnClickPendingIntent(R.id.iv_skip_to_previous, pIntentSkipToPrevious);

        Intent intentSkipToNext = new Intent(context, WidgetActionsReceiver.class);
        intentSkipToNext.putExtra(REQUEST_CODE, SKIP_TO_NEXT);
        PendingIntent pIntentSkipToNext = PendingIntent.getBroadcast(context,
                SKIP_TO_NEXT,
                intentSkipToNext,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));
        widgetView.setOnClickPendingIntent(R.id.iv_skip_to_next, pIntentSkipToNext);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(OPEN_PLAY_QUEUE_ARG, enabled);
        PendingIntent pIntent = PendingIntent.getActivity(context,
                0,
                intent,
                AndroidUtilsKt.pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));
        widgetView.setOnClickPendingIntent(R.id.widget_view, pIntent);
    }

    private void updateWidget(Context context,
                              AppWidgetManager appWidgetManager,
                              int widgetId,
                              boolean play,
                              String compositionName,
                              String compositionAuthor,
                              long compositionId,
                              long compositionUpdateTime,
                              int queueSize,
                              boolean enabled,
                              boolean showCovers,
                              boolean randomPlayModeEnabled,
                              int repeatMode) {
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), getRemoteViewId());

        applyViewLogic(widgetView,
                appWidgetManager,
                widgetId,
                context,
                play,
                compositionName,
                compositionAuthor,
                compositionId,
                compositionUpdateTime,
                queueSize,
                enabled,
                showCovers,
                randomPlayModeEnabled,
                repeatMode);

        appWidgetManager.updateAppWidget(widgetId, widgetView);
    }

    @ColorRes
    protected int getWidgetAccentColorRes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return android.R.color.system_accent1_500;
        } else {
            return R.color.colorAccent;
        }
    }

    @LayoutRes
    protected abstract int getRemoteViewId();
}
