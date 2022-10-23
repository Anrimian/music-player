package com.github.anrimian.musicplayer.ui.utils.views.progress_bar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import kotlin.math.min

//TODO check redraw engine, consider copying circular progress indicator
//TODO short blinking (cloud icon after start)
//TODO close animation is too fast or just not works
//TODO when state is translucent overlap is visible
//based on https://github.com/2hamed/ProgressCircula/blob/master/progresscircula/src/main/java/com/hmomeni/progresscircula/ProgressCircula.kt
class ProgressView(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    constructor(
        context: Context,
        attributeSet: AttributeSet? = null
    ) : this(context, attributeSet, 0) {
        speed = 0.5f
        iconTint = context.colorFromAttr(android.R.attr.textColorPrimaryInverse)
    }

    private val progressRect = RectF()
    private var progressBarPadding = 2f

    private val intermediateSweepAngle = 6f
    private var sweepAngle = 0f
    private var sweepStep = 4
    private var startAngle = 0f
    private var speed = 4f

    private var bgRadius = 0f

    private var centerX = 0f
    private var centerY = 0f

    private var progressStrokeWidth = 3f

    private var progress = -1
    private var indeterminate = false

    private var iconDrawable: Drawable? = null
    private var iconPadding = 10//can be calculated
    private var iconTint = 0

    private var scale = 0f

    private val progressPaint = Paint().apply {
        color = context.colorFromAttr(android.R.attr.textColorPrimaryInverse)
        strokeWidth = progressStrokeWidth
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val cornerSize = 2f
    private val cornerPaint = Paint().apply {
        color = context.colorFromAttr(R.attr.listItemBackground)
    }

    private val bgPaint = Paint().apply {
        color = context.colorFromAttr(R.attr.colorAccent)
    }

    private var visibilityAnimator: ValueAnimator? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()

        bgRadius = min(width, height)/2

        val progressBarRadius = bgRadius - paddingBottom - progressStrokeWidth/2 - progressBarPadding - cornerSize

        centerX = width/2
        centerY = height/2

        progressRect.set(
            centerX - progressBarRadius,
            centerY - progressBarRadius,
            centerX + progressBarRadius,
            centerY + progressBarRadius
        )

        iconDrawable?.setBounds(
            iconPadding,
            iconPadding,
            measuredWidth - iconPadding,
            measuredHeight - iconPadding
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.scale(scale, scale, centerX, centerY)

        if (iconDrawable != null || isProgressActive()) {
            canvas.drawCircle(centerX, centerX, bgRadius, cornerPaint)
            canvas.drawCircle(centerX, centerX, bgRadius - cornerSize, bgPaint)
        }

        iconDrawable?.draw(canvas)

        if (isProgressActive()) {
            startAngle += sweepStep * speed
            if (startAngle > 360f) {
                startAngle = 0f
            }

            sweepAngle = if (indeterminate) {
                intermediateSweepAngle
            } else {
                (progress / 100f * 360).coerceAtLeast(intermediateSweepAngle)
            }

            canvas.drawArc(progressRect, startAngle, sweepAngle, false, progressPaint)
            postInvalidate()
        }

    }

    fun setProgress(progress: Int) {
        this.progress = progress
        this.indeterminate = false
        invalidate()
        startVisibilityAnimation()
    }

    fun setIndeterminate(indeterminate: Boolean) {
        this.indeterminate = indeterminate
        invalidate()
        startVisibilityAnimation()
    }

    fun clearProgress() {
        this.indeterminate = false
        progress = -1
        startVisibilityAnimation()
    }

    fun setIconResource(@DrawableRes resId: Int) {
        iconDrawable = ContextCompat.getDrawable(context, resId)
        iconDrawable?.run {
            setBounds(
                iconPadding,
                iconPadding,
                measuredWidth - iconPadding,
                measuredHeight - iconPadding
            )
            setTint(iconTint)
        }
        invalidate()
        startVisibilityAnimation()
    }

    fun clearIcon() {
        iconDrawable = null
        invalidate()
        startVisibilityAnimation()
    }

    private fun isProgressActive() = indeterminate || progress >= 0

    private fun startVisibilityAnimation() {
        val targetScale = if (isProgressActive()) 1f else 0f
        if (iconDrawable != null) {
            scale = 1f
            invalidate()
            return
        }
        if (targetScale == scale) {
            return
        }
        visibilityAnimator?.cancel()
        visibilityAnimator = ValueAnimator.ofFloat(scale, targetScale).apply {
            addUpdateListener { animator ->
                scale = animator.animatedValue as Float
                invalidate()
            }
            interpolator = if (targetScale == 1f) DecelerateInterpolator() else AccelerateInterpolator()
            duration = 300
            start()
        }
    }

}