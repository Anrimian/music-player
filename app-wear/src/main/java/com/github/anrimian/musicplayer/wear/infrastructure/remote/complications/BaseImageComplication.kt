package com.github.anrimian.musicplayer.wear.infrastructure.remote.complications

import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Icon
import android.text.TextPaint
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.wear.complications.ComplicationProviderService
import androidx.wear.complications.ComplicationRequest
import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.data.ComplicationType
import androidx.wear.complications.data.MonochromaticImage
import androidx.wear.complications.data.MonochromaticImageComplicationData
import androidx.wear.complications.data.PlainComplicationText
import com.github.anrimian.musicplayer.wear.R
import com.github.anrimian.musicplayer.wear.di.Components
import kotlin.math.min


//TODO-W image complication: generate image with current composition text
//TODO-W three more: text like, volume up/volume down
//TODO-W no connection state
abstract class BaseImageComplication: ComplicationProviderService() {

    private val wearStateInteractor = Components.getAppComponent().wearStateInteractor()

    private var bitmap: Bitmap? = null

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return createComplicationData(R.drawable.ic_play, "title", "artist").build()//TODO-W finish, set from res
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener,
    ) {
        val playStateIcon = if (wearStateInteractor.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play

        val title: String
        val artist: String
        val composition = wearStateInteractor.getCurrentComposition()
        if (composition == null) {
            title = "--"
            artist = "--"
        } else {
            title = composition.title.substring(0, min(8, composition.title.length))
            val a = composition.artist ?: "--"
            artist = a.substring(0, min(8, a.length))
        }

        val data = createComplicationData(playStateIcon, title, artist)
            .setTapAction(getTapAction())
            .build()
        listener.onComplicationData(data)
    }

    private fun createComplicationData(
        @DrawableRes playStateIcon: Int,
        title: String,
        artist: String,
    ): MonochromaticImageComplicationData.Builder {
        bitmap?.recycle()
        val conf = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(48, 48, conf)
        val canvas = Canvas(bmp)
        val myIcon = ContextCompat.getDrawable(this, playStateIcon)!!
        myIcon.setBounds(0, 0, 12, 12)
        myIcon.draw(canvas)
        myIcon.alpha = 100
        val paint = TextPaint(Color.WHITE)
        paint.isAntiAlias = true
        canvas.drawText(title, 5f, 20f, paint)//TODO-W text is twisted
        canvas.drawText(artist, 5f, 35f, paint)
        bitmap = bmp
        val image = Icon.createWithBitmap(bmp)
        val ambientImage = Icon.createWithBitmap(bmp)
        return MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(image)
                .setAmbientImage(ambientImage)
                .build(),
            contentDescription = PlainComplicationText.Builder("").build()//TODO-W finish
        )
    }

    protected abstract fun getTapAction(): PendingIntent

}