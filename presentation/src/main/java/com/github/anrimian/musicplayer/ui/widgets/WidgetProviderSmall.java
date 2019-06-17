package com.github.anrimian.musicplayer.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.utils.Permissions;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_AUTHOR_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_NAME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAY_QUEUE_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.QUEUE_SIZE_ARG;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater.ACTION_UPDATE_COMPOSITION;
import static com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater.ACTION_UPDATE_QUEUE;


public class WidgetProviderSmall extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        String compositionName;
        String compositionAuthor;
        if (ACTION_UPDATE_COMPOSITION.equals(intent.getAction())) {
            compositionName = intent.getStringExtra(COMPOSITION_NAME_ARG);
            compositionAuthor = intent.getStringExtra(COMPOSITION_AUTHOR_ARG);
        } else {
            compositionName = WidgetDataHolder.getCompositionName(context);
            compositionAuthor = WidgetDataHolder.getCompositionAuthor(context);
        }

        int queueSize;
        if (ACTION_UPDATE_QUEUE.equals(intent.getAction())) {
            queueSize = intent.getIntExtra(QUEUE_SIZE_ARG, 0);
        } else {
            queueSize = WidgetDataHolder.getCurrentQueueSize(context);
        }

        boolean play = false;
        if (Permissions.hasFilePermission(context)) {
            play = Components.getAppComponent().musicPlayerInteractor().getPlayerState() == PlayerState.PLAY;
        }

        int[] ids = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int widgetId : ids) {
            updateWidget(context,
                    appWidgetManager,
                    widgetId,
                    play,
                    compositionName,
                    compositionAuthor,
                    queueSize);
        }

    }

    private void updateWidget(Context context,
                              AppWidgetManager appWidgetManager,
                              int widgetId,
                              boolean play,
                              String compositionName,
                              String compositionAuthor,
                              int queueSize) {
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_small);

        boolean enabled = true;
        if (isEmpty(compositionName)) {
            compositionName = context.getString(R.string.no_current_composition);
            compositionAuthor = null;
            enabled = false;
        }
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
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.iv_play_pause, pIntentPlayPause);

        Intent intentSkipToPrevious = new Intent(context, WidgetActionsReceiver.class);
        intentSkipToPrevious.putExtra(REQUEST_CODE, SKIP_TO_PREVIOUS);
        PendingIntent pIntentSkipToPrevious = PendingIntent.getBroadcast(context,
                SKIP_TO_PREVIOUS,
                intentSkipToPrevious,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.iv_skip_to_previous, pIntentSkipToPrevious);

        Intent intentSkipToNext = new Intent(context, WidgetActionsReceiver.class);
        intentSkipToNext.putExtra(REQUEST_CODE, SKIP_TO_NEXT);
        PendingIntent pIntentSkipToNext = PendingIntent.getBroadcast(context,
                SKIP_TO_NEXT,
                intentSkipToNext,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.iv_skip_to_next, pIntentSkipToNext);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(OPEN_PLAY_QUEUE_ARG, true);
        PendingIntent pIntent = PendingIntent.getActivity(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.widget_view, pIntent);

        appWidgetManager.updateAppWidget(widgetId, widgetView);
    }
}
