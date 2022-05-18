package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.short_swipe

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.TextPaint
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.*
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener
import kotlin.math.abs
import kotlin.math.max


private const val NO_POSITION = -1
private const val APPEAR_ANIM_SCALE_START = 0f
private const val APPEAR_ANIM_SCALE_END = 1f
private const val ANIMATION_DURATION = 120L

class ShortSwipeCallback(
    context: Context,
    @DrawableRes iconRes: Int,
    @StringRes textResId: Int,
    backgroundColor: Int = context.colorFromAttr(R.attr.listItemBottomBackground),
    textColor: Int = AndroidUtils.getContrastColor(backgroundColor),
    @DimenRes panelWidthRes: Int = R.dimen.swipe_panel_width,
    @DimenRes panelEndPaddingRes: Int = R.dimen.swipe_panel_padding_end,
    @DimenRes textTopPaddingRes: Int = R.dimen.swipe_panel_text_top_padding,
    @DimenRes iconSizeRes: Int = R.dimen.swipe_panel_icon_size,
    @DimenRes textSizeRes: Int = R.dimen.swipe_panel_text_size,
    private val shouldNotSwipeViewHolder: (RecyclerView.ViewHolder) -> Boolean = { false },
    private val swipeCallback: (Int) -> Unit,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {

    private val panelWidth = context.getDimensionPixelSize(panelWidthRes)
    private val panelHorizontalPadding = context.getDimensionPixelSize(panelEndPaddingRes)
    private val iconSize = context.getDimensionPixelSize(iconSizeRes)
    private val textTopPadding = context.getDimensionPixelSize(textTopPaddingRes)

    private val icon = context.getDrawableCompat(iconRes).apply {
        setTint(textColor)
        setBounds(0, 0, iconSize, iconSize)
    }

    private val textPaint = TextPaint().apply {
        color = textColor
        isAntiAlias = true
        textSize = context.resources.getDimension(textSizeRes)
    }

    private val bgPaint = Paint().apply {
        color = backgroundColor
    }

    private val textStaticLayout = createStaticLayout(
            context.getString(textResId),
            textPaint,
            panelWidth,
            4,
            Layout.Alignment.ALIGN_CENTER
    )

    private var itemPositionToAction = NO_POSITION
    private var swipeEffectAnimator: ValueAnimator? = null
    private var currentScale = APPEAR_ANIM_SCALE_START

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE && itemPositionToAction != NO_POSITION) {
            swipeCallback(itemPositionToAction)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        swipeEffectAnimator?.cancel()
        currentScale = APPEAR_ANIM_SCALE_START
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (shouldNotSwipeViewHolder(viewHolder)) {
            return 0
        }
        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            setIsSwiping(viewHolder, abs(dX))

            val itemView = viewHolder.itemView

            val contentHeight = iconSize + textStaticLayout.height
            val contentMarginTop = (itemView.height - contentHeight) / 2f
            val left = itemView.left.toFloat()
            val top = itemView.top.toFloat()
            val right = itemView.right.toFloat()
            val bottom = itemView.bottom.toFloat()

            val panelWidth = max(textStaticLayout.width, iconSize) + panelHorizontalPadding*2

            var startAnimation = false
            var isInfoVisible = false
            if (abs(dX) > panelWidth) {
                isInfoVisible = true
                if (itemPositionToAction == NO_POSITION) {
                    AndroidUtils.playShortVibration(itemView.context)
                    itemPositionToAction = viewHolder.bindingAdapterPosition
                    startAnimation = true
                }
            } else {
                if (itemPositionToAction != NO_POSITION) {
                    itemPositionToAction = NO_POSITION
                    startAnimation = true
                }
            }

            if (startAnimation) {
                var start = if (isInfoVisible) APPEAR_ANIM_SCALE_START else APPEAR_ANIM_SCALE_END
                var animationDuration = ANIMATION_DURATION
                val swipeEffectAnimator = this.swipeEffectAnimator
                if (swipeEffectAnimator?.isRunning == true) {
                    start = swipeEffectAnimator.animatedValue as Float
                    animationDuration = swipeEffectAnimator.currentPlayTime
                    swipeEffectAnimator.cancel()
                }
                val end = if (isInfoVisible) APPEAR_ANIM_SCALE_END else APPEAR_ANIM_SCALE_START
                this.swipeEffectAnimator = ValueAnimator.ofFloat(start, end).also { animator ->
                    animator.duration = animationDuration
                    animator.addUpdateListener { anim ->
                        currentScale = anim.animatedValue as Float
                        recyclerView.invalidate()
                    }
                    animator.interpolator = if (isInfoVisible) AccelerateInterpolator() else DecelerateInterpolator()
                    animator.start()
                }
            }

            //draw bg
            c.drawRect(left, top, right, bottom, bgPaint)

            //draw icon
            c.save()

            c.translate((itemView.right - panelWidth).toFloat(), top)
            c.translate(panelWidth/2f - iconSize/2f, contentMarginTop)
            c.scale(currentScale, currentScale, iconSize/2f, iconSize/2f + textStaticLayout.height/2)
            icon.draw(c)

            //draw text
            c.translate(iconSize/2f - textStaticLayout.width/2f, (iconSize + textTopPadding).toFloat())
            textStaticLayout.draw(c)
            c.restore()


            if (abs(dX) > panelWidth*1.25) {
                return
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun getSwipeEscapeVelocity(defaultValue: Float) = defaultValue * 8f

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 2f//so we can't call onSwiped()

    private fun setIsSwiping(viewHolder: RecyclerView.ViewHolder?, swipeOffset: Float) {
        if (viewHolder == null) {
            return
        }
        if (viewHolder is SwipeListener) {
            viewHolder.onSwipeStateChanged(swipeOffset)
        }
    }

}