package com.github.anrimian.musicplayer.ui.utils.views.recycler_view

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class ItemDrawable: Drawable() {

    private val path = Path()
    private val corners = FloatArray(8) { 0f }
    private val bgPaint = Paint()

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        updatePath()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, bgPaint)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int = PixelFormat.UNKNOWN

    fun setColor(@ColorInt color: Int) {
        bgPaint.color = color
        invalidateSelf()
    }

    fun getColor() = bgPaint.color

    fun setCornerRadius(
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float,
    ) {
        corners[0] = topLeft
        corners[1] = topLeft
        corners[2] = topRight
        corners[3] = topRight
        corners[4] = bottomRight
        corners[5] = bottomRight
        corners[6] = bottomLeft
        corners[7] = bottomLeft

        updatePath()
        invalidateSelf()
    }

    private fun updatePath() {
        path.reset()
        path.addRoundRect(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            corners,
            Path.Direction.CW
        )
    }
}