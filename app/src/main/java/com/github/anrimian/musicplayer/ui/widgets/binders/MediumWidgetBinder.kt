package com.github.anrimian.musicplayer.ui.widgets.binders

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService
import com.github.anrimian.musicplayer.ui.utils.broadcastPendingIntentFlag
import com.github.anrimian.musicplayer.ui.widgets.WidgetActionsReceiver
import com.github.anrimian.musicplayer.ui.widgets.models.WidgetColors
import com.github.anrimian.musicplayer.ui.widgets.models.WidgetData
import com.github.anrimian.musicplayer.ui.widgets.providers.WidgetProviderMedium

open class MediumWidgetBinder: SmallExtWidgetBinder() {

    override fun getWidgetProviderClass(): Class<*> = WidgetProviderMedium::class.java

    override fun getRemoteViewId() = R.layout.widget_medium

    override fun applyViewLogic(
        widgetView: RemoteViews,
        context: Context,
        widgetColors: WidgetColors?,
        widgetData: WidgetData,
    ) {
        super.applyViewLogic(widgetView, context, widgetColors, widgetData)

        widgetView.setBoolean(R.id.ivRewind, "setEnabled", widgetData.isEnabled())
        widgetView.setBoolean(R.id.ivFastForward, "setEnabled", widgetData.isEnabled())

        if (widgetColors != null) {
            val buttonColor = if (widgetData.isEnabled()) {
                widgetColors.buttonColor
            } else {
                ContextCompat.getColor(context, R.color.disabled_color)
            }
            widgetView.setInt(R.id.ivRewind, "setColorFilter", buttonColor)
            widgetView.setInt(R.id.ivFastForward, "setColorFilter", buttonColor)
        }

        if (widgetData.isCoversEnabled) {
            Components.getAppComponent().imageLoader()
                .displayImage(
                    widgetView,
                    R.id.ivCover,
                    ComponentName(context, getWidgetProviderClass()),
                    widgetData.compositionId,
                    widgetData.compositionUpdateTime,
                    widgetData.coverModifyTime,
                    widgetData.compositionSize,
                    widgetData.isFileExists,
                    R.drawable.ic_music_widget_placeholder
                )
        } else {
            widgetView.setImageViewResource(R.id.ivCover, R.drawable.ic_music_placeholder)
        }

        val intentRewind = Intent(context, WidgetActionsReceiver::class.java)
        intentRewind.putExtra(MusicService.REQUEST_CODE, Constants.Actions.REWIND)
        val pIntentRewind = PendingIntent.getBroadcast(
            context,
            Constants.Actions.REWIND,
            intentRewind,
            broadcastPendingIntentFlag()
        )
        widgetView.setOnClickPendingIntent(R.id.ivRewind, pIntentRewind)

        val intentFastForward = Intent(context, WidgetActionsReceiver::class.java)
        intentFastForward.putExtra(MusicService.REQUEST_CODE, Constants.Actions.FAST_FORWARD)
        val pIntentFastForward = PendingIntent.getBroadcast(
            context,
            Constants.Actions.FAST_FORWARD,
            intentFastForward,
            broadcastPendingIntentFlag()
        )
        widgetView.setOnClickPendingIntent(R.id.ivFastForward, pIntentFastForward)
    }
}