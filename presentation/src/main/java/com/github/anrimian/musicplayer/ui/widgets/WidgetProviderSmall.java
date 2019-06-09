package com.github.anrimian.musicplayer.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;

import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_AUTHOR_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_NAME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_ARG;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater.ACTION_UPDATE_COMPOSITION;
import static com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater.ACTION_UPDATE_PLAY_STATE;

public class WidgetProviderSmall extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int widgetId : ids) {
            updateWidget(context, appWidgetManager, widgetId, intent);
        }

    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId, Intent intent) {
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_small);
        if (ACTION_UPDATE_COMPOSITION.equals(intent.getAction())) {
            String compositionName = intent.getStringExtra(COMPOSITION_NAME_ARG);
            String compositionAuthor = intent.getStringExtra(COMPOSITION_AUTHOR_ARG);
            widgetView.setTextViewText(R.id.tv_composition, compositionName);
            widgetView.setTextViewText(R.id.tv_composition_author, compositionAuthor);
        }
        if (ACTION_UPDATE_PLAY_STATE.equals(intent.getAction())) {
            boolean isPlaying = intent.getBooleanExtra(PLAY_ARG, false);

        }
        appWidgetManager.updateAppWidget(widgetId, widgetView);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("KEK", "onUpdate widget");

        for (int widgetId: appWidgetIds) {
            RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                    R.layout.widget_small);

//            MediaSessionManager sessionService = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);

//            sessionService.addOnActiveSessionsChangedListener(new MediaSessionManager.OnActiveSessionsChangedListener() {
//                @Override
//                public void onActiveSessionsChanged(@Nullable List<MediaController> controllers) {
//                    MediaMetadata mediaMetadata = controllers.get(0).getMetadata();
//                    String title = mediaMetadata.getString(METADATA_KEY_TITLE);
//                    widgetView.setTextViewText(R.id.tv_composition, title);
//
//                    appWidgetManager.updateAppWidget(widgetId, widgetView);
//                }
//            }, null);

            Intent intentSkipToPrevious = new Intent(context, MusicService.class);
            intentSkipToPrevious.putExtra(REQUEST_CODE, SKIP_TO_PREVIOUS);
            PendingIntent pIntentSkipToPrevious = PendingIntent.getService(context,
                    widgetId,
                    intentSkipToPrevious,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            widgetView.setOnClickPendingIntent(R.id.iv_skip_to_previous, pIntentSkipToPrevious);

            Intent intentSkipToNext = new Intent(context, MusicService.class);
            intentSkipToNext.putExtra(REQUEST_CODE, SKIP_TO_NEXT);
            PendingIntent pIntentSkipToNext = PendingIntent.getService(context,
                    widgetId,
                    intentSkipToNext,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            widgetView.setOnClickPendingIntent(R.id.iv_skip_to_next, pIntentSkipToNext);

            appWidgetManager.updateAppWidget(widgetId, widgetView);
        }
    }
}
