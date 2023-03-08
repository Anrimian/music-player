package com.github.anrimian.musicplayer.ui.widgets.binders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.utils.broadcastPendingIntentFlag
import com.github.anrimian.musicplayer.ui.widgets.WidgetActionsReceiver
import com.github.anrimian.musicplayer.ui.widgets.models.WidgetColors
import com.github.anrimian.musicplayer.ui.widgets.models.WidgetData
import com.github.anrimian.musicplayer.ui.widgets.providers.WidgetProviderSmallExt

open class SmallExtWidgetBinder: WidgetBinder() {

    override fun getWidgetProviderClass(): Class<*> = WidgetProviderSmallExt::class.java

    override fun getRemoteViewId() = R.layout.widget_small_ext

    override fun applyViewLogic(
        widgetView: RemoteViews,
        context: Context,
        widgetColors: WidgetColors?,
        widgetData: WidgetData,
    ) {
        super.applyViewLogic(widgetView, context, widgetColors, widgetData)

        widgetView.setBoolean(R.id.ivShuffleMode, "setEnabled", widgetData.isEnabled())
        widgetView.setBoolean(R.id.ivRepeatMode, "setEnabled", widgetData.isEnabled())

        if (widgetColors != null) {
            val buttonColor = if (widgetData.isEnabled()) {
                widgetColors.buttonColor
            } else {
                ContextCompat.getColor(context, R.color.disabled_color)
            }
            widgetView.setInt(R.id.ivRepeatMode, "setColorFilter", buttonColor)
            widgetView.setInt(R.id.ivShuffleMode, "setColorFilter", buttonColor)
        }

        @DrawableRes val randomIconRes = FormatUtils.getRandomModeIcon(widgetData.randomPlayModeEnabled)
        widgetView.setImageViewResource(R.id.ivShuffleMode, randomIconRes)

        @DrawableRes val iconRes = FormatUtils.getRepeatModeIcon(widgetData.repeatMode)
        widgetView.setImageViewResource(R.id.ivRepeatMode, iconRes)

        val intentChangeShuffleMode = Intent(context, WidgetActionsReceiver::class.java)
        intentChangeShuffleMode.putExtra(
            MusicService.REQUEST_CODE,
            Constants.Actions.CHANGE_SHUFFLE_NODE
        )
        val pIntentChangeShuffleMode = PendingIntent.getBroadcast(
            context,
            Constants.Actions.CHANGE_SHUFFLE_NODE,
            intentChangeShuffleMode,
            broadcastPendingIntentFlag()
        )
        widgetView.setOnClickPendingIntent(R.id.ivShuffleMode, pIntentChangeShuffleMode)

        val intentChangeRepeatMode = Intent(context, WidgetActionsReceiver::class.java)
        intentChangeRepeatMode.putExtra(
            MusicService.REQUEST_CODE,
            Constants.Actions.CHANGE_REPEAT_MODE
        )
        val pIntentChangeRepeatMode = PendingIntent.getBroadcast(
            context,
            Constants.Actions.CHANGE_REPEAT_MODE,
            intentChangeRepeatMode,
            broadcastPendingIntentFlag()
        )
        widgetView.setOnClickPendingIntent(R.id.ivRepeatMode, pIntentChangeRepeatMode)
    }
}