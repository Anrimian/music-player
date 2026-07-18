package com.github.anrimian.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

fun ImageView.setAnimatedVectorDrawable(
    @DrawableRes drawableRes: Int,
    animate: Boolean = true,
) {
    val drawable = AppCompatResources.getDrawable(context, drawableRes)
    val tag = tag as Int?
    if (tag != null && tag == drawableRes) {
        return
    }
    setTag(drawableRes)
    setImageDrawable(drawable)
    if (animate && tag != null && drawable is Animatable) {
        (drawable as Animatable).start()
    }
}

fun Context.createActivityPIntent(intent: Intent): PendingIntent = PendingIntent.getActivity(
    this,
    0,
    intent,
    pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
)


fun Context.createBroadcastPIntent(
    intent: Intent,
    requestCode: Int,
): PendingIntent = PendingIntent.getBroadcast(
    this,
    requestCode,
    intent,
    broadcastPendingIntentFlag()
)

fun broadcastPendingIntentFlag(): Int {
    return pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
}

fun pIntentFlag(flags: Int = 0): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    PendingIntent.FLAG_IMMUTABLE or flags
} else {
    flags
}

