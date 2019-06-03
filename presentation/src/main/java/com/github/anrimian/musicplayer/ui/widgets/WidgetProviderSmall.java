package com.github.anrimian.musicplayer.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.anrimian.musicplayer.R;

import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.ui.widgets.WidgetActionsReceiver.WIDGET_ACTION;

public class WidgetProviderSmall extends AppWidgetProvider {

//    @Override
//    public void onReceive(Context context, Intent intent) {
//        super.onReceive(context, intent);
//        if (intent.getAction().equalsIgnoreCase(UPDATE_ALL_WIDGETS)) {
//            ComponentName thisAppWidget = new ComponentName(
//                    context.getPackageName(), getClass().getName());
//            AppWidgetManager appWidgetManager = AppWidgetManager
//                    .getInstance(context);
//            int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
//            for (int appWidgetID : ids) {
//                updateWidget(context, appWidgetManager, appWidgetID);
//            }
//        }
//    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("KEK", "onUpdate widget");

        for (int widgetId: appWidgetIds) {
            RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                    R.layout.widget_small);

            Intent skipToPreviousIntent = new Intent(context, WidgetActionsReceiver.class);
            skipToPreviousIntent.putExtra(WIDGET_ACTION, SKIP_TO_PREVIOUS);
            PendingIntent pIntentSkipToPrevious = PendingIntent.getBroadcast(context,
                    widgetId,
                    skipToPreviousIntent,
                    0);
            widgetView.setOnClickPendingIntent(R.id.iv_skip_to_previous, pIntentSkipToPrevious);

            Intent skipToNextIntent = new Intent(context, WidgetActionsReceiver.class);
            skipToNextIntent.putExtra(WIDGET_ACTION, SKIP_TO_NEXT);
            PendingIntent pIntentSkipToNext = PendingIntent.getBroadcast(context,
                    widgetId,
                    skipToNextIntent,
                    0);
            widgetView.setOnClickPendingIntent(R.id.iv_skip_to_next, pIntentSkipToNext);

            appWidgetManager.updateAppWidget(widgetId, widgetView);
        }
    }
}
