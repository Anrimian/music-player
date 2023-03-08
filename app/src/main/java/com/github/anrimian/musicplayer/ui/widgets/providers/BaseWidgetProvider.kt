package com.github.anrimian.musicplayer.ui.widgets.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.github.anrimian.musicplayer.ui.widgets.WidgetDataHolder
import com.github.anrimian.musicplayer.ui.widgets.binders.WidgetBinder

abstract class BaseWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action
        if (Intent.ACTION_LOCALE_CHANGED == action) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val thisAppWidget = ComponentName(context.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val remoteViews = getWidgetBinder().getBoundRemoteViews(
            context,
            WidgetDataHolder.getWidgetColors(context),
            WidgetDataHolder.getWidgetData(context)
        )
        for (widgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }

    protected abstract fun getWidgetBinder(): WidgetBinder

}