package com.github.anrimian.musicplayer.wear.infrastructure.remote.complications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.wear.complications.ComplicationProviderService
import androidx.wear.complications.ComplicationRequest
import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.data.ComplicationType
import androidx.wear.complications.data.MonochromaticImage
import androidx.wear.complications.data.PlainComplicationText
import androidx.wear.complications.data.RangedValueComplicationData
import com.github.anrimian.musicplayer.wear.Constants
import com.github.anrimian.musicplayer.wear.R
import com.github.anrimian.musicplayer.wear.di.Components

//TODO-W image complication: generate image with current composition text
//TODO-W three more: text like, volume up/volume down
//TODO-W no connection state
abstract class BaseRangedComplication: ComplicationProviderService() {

    private val wearStateInteractor = Components.getAppComponent().wearStateInteractor()

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return createComplicationData(R.drawable.ic_play, 3, 9).build()
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener,
    ) {
        val volume = wearStateInteractor.getVolumeState()

        val playStateIcon = if (wearStateInteractor.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play

        val intentPlayPause = Intent(this, ComplicationActionReceiver::class.java)
        intentPlayPause.putExtra(Constants.Actions.ACTION, Constants.Actions.PLAY_PAUSE)

        val data = createComplicationData(playStateIcon, volume.getVolume(), volume.getMaxVolume())
            .setTapAction(getTapAction())
            .build()
        listener.onComplicationData(data)
    }

    private fun createComplicationData(
        @DrawableRes playStateIcon: Int,
        currentVolume: Int,
        maxVolume: Int
    ) = RangedValueComplicationData.Builder(
        value = currentVolume.toFloat(),
        min = 0f,
        max = maxVolume.toFloat(),
        contentDescription = PlainComplicationText.Builder("$currentVolume/$maxVolume").build()
    ).setMonochromaticImage(
        MonochromaticImage.Builder(Icon.createWithResource(this, playStateIcon)).build()
    )

    protected abstract fun getTapAction(): PendingIntent

}