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
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import kotlin.math.min

//short blinking (cloud icon after start) - reproduce
class ProgressView(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    constructor(
        context: Context,
        attributeSet: AttributeSet? = null
    ) : this(context, attributeSet, 0) {
        iconTint = context.colorFromAttr(android.R.attr.textColorPrimaryInverse)
    }

    private val progressBarPadding = 2f
    private val progressStrokeWidth = 4f
    private val intermediateSweepAngle = 6f
    private val iconPaddingExtra = 1
    private val strokeSize = 2f


    private val progressRect = RectF()

    private var startAngle = 0f
    private var sweepAngle = 0f

    private var bgRadius = 0f

    private var centerX = 0f
    private var centerY = 0f

    private var progress = -1
    private var indeterminate = false

    private var iconDrawable: Drawable? = null
    private var iconPadding = 0
    private var iconTint = 0

    private var scale = 0f

    private val progressPaint = Paint().apply {
        color = context.colorFromAttr(android.R.attr.textColorPrimaryInverse)
        strokeWidth = progressStrokeWidth
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val strokePaint = Paint().apply {
        color = context.colorFromAttr(R.attr.listItemBackground)
    }

    private val bgPaint = Paint().apply {
        color = context.colorFromAttr(R.attr.colorAccent)
    }

    private val invalidateAnimator = createInvalidateAnimator()
    private var sweepAngleAnimator: ValueAnimator? = null
    private var visibilityAnimator: ValueAnimator? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()

        bgRadius = min(width, height)/2

        val pbCenter = paddingBottom + progressStrokeWidth/2 + progressBarPadding + strokeSize
        val progressBarRadius = bgRadius - pbCenter
        iconPadding = (pbCenter + progressStrokeWidth/2).toInt() + iconPaddingExtra

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

        canvas.drawCircle(centerX, centerX, bgRadius, strokePaint)
        canvas.drawCircle(centerX, centerX, bgRadius - strokeSize, bgPaint)

        iconDrawable?.draw(canvas)

        if (isProgressActive()) {
            canvas.drawArc(progressRect, startAngle, sweepAngle, false, progressPaint)
        }
    }

    fun setProgress(progress: Int) {
        updateProgress(progress, false)
        setProgressInvalidation(true)
    }

    fun setIndeterminate(indeterminate: Boolean) {
        updateProgress(progress, indeterminate)
        setProgressInvalidation(true)
    }

    fun clearProgress() {
        updateProgress(-1, false)
        setProgressInvalidation(false)
    }

    fun setIconResource(@DrawableRes resId: Int) {
        val iconDrawable = ContextCompat.getDrawable(context, resId)
        iconDrawable?.run {
            setBounds(
                iconPadding,
                iconPadding,
                measuredWidth - iconPadding,
                measuredHeight - iconPadding
            )
            setTint(iconTint)
        }
        setIconDrawable(iconDrawable)
    }

    fun setIconDrawable(drawable: Drawable?) {
        iconDrawable = drawable
        invalidate()
    }

    fun clearIcon() {
        iconDrawable = null
        invalidate()
    }

    fun setVisible(
        isVisible: Boolean,
        animate: Boolean,
        clearIcon: Boolean = false,
        clearProgress: Boolean = false,
    ) {
        val targetScale = if (isVisible) 1f else 0f
        if (targetScale == scale) {
            return
        }
        if (!animate) {
            scale = targetScale
            invalidate()
            return
        }
        visibilityAnimator?.cancel()
        visibilityAnimator = ValueAnimator.ofFloat(scale, targetScale).apply {
            addUpdateListener { animator ->
                scale = animator.animatedValue as Float
                invalidate()
            }
            addListener {
                doOnEnd {
                    if (clearIcon) {
                        clearIcon()
                    }
                    if (clearProgress) {
                        clearProgress()
                    }
                }
            }
            interpolator = if (isVisible) DecelerateInterpolator() else AccelerateInterpolator()
            duration = if (isVisible) 300 else 150
            start()
        }
    }

    private fun isProgressActive() = indeterminate || progress >= 0

    private fun setProgressInvalidation(isStarted: Boolean) {
        if (isStarted) {
            if (!invalidateAnimator.isRunning) {
                invalidateAnimator.start()
            }
        } else {
            invalidateAnimator.cancel()
        }
    }

    private fun updateProgress(progress: Int, indeterminate: Boolean) {
        this.indeterminate = indeterminate
        this.progress = progress

        val newSweepAngle = if (indeterminate) {
            intermediateSweepAngle
        } else {
            (progress / 100f * 360).coerceAtLeast(intermediateSweepAngle)
        }
        if (sweepAngle != newSweepAngle) {
            sweepAngleAnimator?.cancel()
            val anim = ValueAnimator.ofFloat(sweepAngle, newSweepAngle)
            anim.duration = 900
            anim.addUpdateListener { a ->
                sweepAngle = a.animatedValue as Float
            }
            anim.interpolator = LinearInterpolator()
            anim.start()
            sweepAngleAnimator = anim
        }
    }

    private fun createInvalidateAnimator(): ValueAnimator {
        val anim = ValueAnimator.ofFloat(0f, 360f)
        anim.duration = 2500
        anim.repeatCount = ValueAnimator.INFINITE
        anim.repeatMode = ValueAnimator.RESTART
        anim.addUpdateListener { a ->
            startAngle = a.animatedValue as Float
            invalidate()
        }
        anim.interpolator = LinearInterpolator()
        return anim
    }

}