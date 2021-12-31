package com.github.anrimian.musicplayer.ui.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils.TruncateAt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable {
    return ContextCompat.getDrawable(this, resId) ?: throw IllegalStateException("resource not found")
}

fun createStaticLayout(text: CharSequence,
                       paint: TextPaint,
                       width: Int,
                       maxLines: Int,
                       alignment: Layout.Alignment): StaticLayout {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
                .setAlignment(alignment)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .setEllipsize(TruncateAt.END)
                .setEllipsizedWidth(width)
                .setMaxLines(maxLines)
                .build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(text,
                paint,
                width,
                alignment,
                1f,
                0f,
                false)
    }
}