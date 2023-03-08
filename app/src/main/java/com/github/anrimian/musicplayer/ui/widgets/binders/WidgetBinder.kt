package com.github.anrimian.musicplayer.ui.widgets.binders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService
import com.github.anrimian.musicplayer.ui.common.AppAndroidUtils
import com.github.anrimian.musicplayer.ui.common.format.getRemoteViewPlayerStateIcon
import com.github.anrimian.musicplayer.ui.main.MainActivity
import com.github.anrimian.musicplayer.ui.utils.broadcastPendingIntentFlag
import com.github.anrimian.musicplayer.ui.widgets.WidgetActionsReceiver
import com.github.anrimian.musicplayer.ui.widgets.menu.WidgetMenuActivity
import com.github.anrimian.musicplayer.ui.widgets.models.WidgetColors
import com.github.anrimian.musicplayer.ui.widgets.models.WidgetData

abstract class WidgetBinder {

    fun getBoundRemoteViews(
        context: Context,
        widgetColors: WidgetColors?,
        widgetData: WidgetData
    ): RemoteViews {
        val widgetView = RemoteViews(context.packageName, getRemoteViewId())
        applyViewLogic(widgetView, context, widgetColors, widgetData)
        return widgetView
    }

    abstract fun getWidgetProviderClass() : Class<*>

    @LayoutRes
    protected abstract fun getRemoteViewId(): Int

    protected open fun applyViewLogic(
        widgetView: RemoteViews,
        context: Context,
        widgetColors: WidgetColors?,
        widgetData: WidgetData
    ) {
        widgetView.setBoolean(R.id.ivMenu, "setEnabled", widgetData.isEnabled())
        widgetView.setBoolean(R.id.ivSkipToPrevious, "setEnabled", widgetData.isEnabled())
        widgetView.setBoolean(R.id.ivPlayPause, "setEnabled", widgetData.isEnabled())
        widgetView.setBoolean(
            R.id.ivSkipToNext,
            "setEnabled",
            widgetData.isEnabled() && widgetData.queueSize > 1
        )

        widgetView.setTextViewText(R.id.tvComposition, widgetData.getFormattedCompositionName(context))
        widgetView.setTextViewText(R.id.tvCompositionAuthor, widgetData.compositionAuthor)

        if (widgetColors != null) {
            //set background like bitmap breaks widget animation on android 12
            /*val bgColor = widgetColors.backgroundColor
            widgetView.setInt(R.id.ivBackground, "setColorFilter", bgColor)
            val alpha = bgColor shr 24
            widgetView.setInt(R.id.ivBackground, "setAlpha", alpha)*/

            val buttonColor = if (widgetData.isEnabled()) {
                widgetColors.buttonColor
            } else {
                ContextCompat.getColor(context, R.color.disabled_color)
            }
            widgetView.setInt(R.id.ivSkipToPrevious, "setColorFilter", buttonColor)
            widgetView.setInt(R.id.ivPlayPause, "setColorFilter", buttonColor)
            widgetView.setInt(R.id.ivSkipToNext, "setColorFilter", buttonColor)

            widgetView.setInt(R.id.tvComposition, "setTextColor", widgetColors.textPrimaryColor)
            widgetView.setInt(R.id.tvCompositionAuthor,
                "setTextColor",
                widgetColors.textSecondaryColor)
        }

        widgetView.setImageViewResource(
            R.id.ivPlayPause,
            getRemoteViewPlayerStateIcon(widgetData.playerState)
        )

        val requestCode = if (widgetData.playerState == Constants.RemoteViewPlayerState.PAUSE) Constants.Actions.PLAY else Constants.Actions.PAUSE
        val intentPlayPause = Intent(context, WidgetActionsReceiver::class.java)
        intentPlayPause.putExtra(MusicService.REQUEST_CODE, requestCode)
        val pIntentPlayPause = AppAndroidUtils.broadcastPendingIntent(
            context,
            requestCode,
            intentPlayPause,
        )
        widgetView.setOnClickPendingIntent(R.id.ivPlayPause, pIntentPlayPause)

        val intentSkipToPrevious = Intent(context, WidgetActionsReceiver::class.java)
        intentSkipToPrevious.putExtra(MusicService.REQUEST_CODE, Constants.Actions.SKIP_TO_PREVIOUS)
        val pIntentSkipToPrevious = AppAndroidUtils.broadcastPendingIntent(
            context,
            Constants.Actions.SKIP_TO_PREVIOUS,
            intentSkipToPrevious
        )
        widgetView.setOnClickPendingIntent(R.id.ivSkipToPrevious, pIntentSkipToPrevious)

        val intentSkipToNext = Intent(context, WidgetActionsReceiver::class.java)
        intentSkipToNext.putExtra(MusicService.REQUEST_CODE, Constants.Actions.SKIP_TO_NEXT)
        val pIntentSkipToNext = AppAndroidUtils.broadcastPendingIntent(
            context,
            Constants.Actions.SKIP_TO_NEXT,
            intentSkipToNext,
        )
        widgetView.setOnClickPendingIntent(R.id.ivSkipToNext, pIntentSkipToNext)

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(Constants.Arguments.OPEN_PLAYER_PANEL_ARG, widgetData.isEnabled())
        val pIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            broadcastPendingIntentFlag()
        )
        widgetView.setOnClickPendingIntent(android.R.id.background, pIntent)

        val menuIntent = Intent(context, WidgetMenuActivity::class.java)
        menuIntent.putExtra(Constants.Arguments.ID_ARG, widgetData.compositionId)
        val pMenuIntent = PendingIntent.getActivity(
            context,
            0,
            menuIntent,
            broadcastPendingIntentFlag()
        )
        widgetView.setOnClickPendingIntent(R.id.ivMenu, pMenuIntent)
    }

    protected fun WidgetData.isEnabled() = !compositionName.isNullOrEmpty()

    private fun WidgetData.getFormattedCompositionName(context: Context): String {
        val name = compositionName
        return if (name.isNullOrEmpty()) {
            context.getString(R.string.no_current_composition)
        } else {
            name
        }
    }

}