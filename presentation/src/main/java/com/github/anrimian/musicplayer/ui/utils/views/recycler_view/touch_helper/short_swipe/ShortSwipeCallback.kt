package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.short_swipe

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.createStaticLayout
import com.github.anrimian.musicplayer.ui.utils.getDimensionPixelSize
import com.github.anrimian.musicplayer.ui.utils.getDrawableCompat
import kotlin.math.abs

//TODO design + colors
//TODO dynamic threshold calculation
//TODO do not move divider

private const val SWIPE_BORDER_PERCENT = 0.33f
private const val SWIPE_ACTIVE_BORDER_PERCENT = 0.22f
private const val NO_POSITION = -1
private const val APPEAR_ANIM_SCALE_START = 0f
private const val APPEAR_ANIM_SCALE_END = 1f
private const val ANIMATION_DURATION = 120L

//remove annotation after refactoring fragments to kotlin
class ShortSwipeCallback @JvmOverloads constructor(
        context: Context,
        @DrawableRes iconRes: Int,
        @StringRes textResId: Int,
        @DimenRes panelWidthRes: Int = R.dimen.swipe_panel_width,
        @DimenRes panelEndPaddingRes: Int = R.dimen.swipe_panel_padding_end,
        @DimenRes textTopPaddingRes: Int = R.dimen.swipe_panel_text_top_padding,
        @DimenRes iconSizeRes: Int = R.dimen.swipe_panel_icon_size,
        @DimenRes textSizeRes: Int = R.dimen.swipe_panel_text_size,
        private val swipeCallback: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {

    private val panelWidth = context.getDimensionPixelSize(panelWidthRes)
    private val panelEndPadding = context.getDimensionPixelSize(panelEndPaddingRes)
    private val iconSize = context.getDimensionPixelSize(iconSizeRes)
    private val textTopPadding = context.getDimensionPixelSize(textTopPaddingRes)
    private val textColor = Color.WHITE

    private val icon = context.getDrawableCompat(iconRes).apply {
        setTint(textColor)
        setBounds(0, 0, iconSize, iconSize)
    }

    private val textPaint = TextPaint().apply {
        color = textColor
        isAntiAlias = true
        textSize = context.resources.getDimension(textSizeRes)
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

    override fun onChildDraw(c: Canvas,
                             recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder,
                             dX: Float,
                             dY: Float,
                             actionState: Int,
                             isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView

            val contentHeight = iconSize + textStaticLayout.height
            val contentMarginTop = (itemView.height - contentHeight) / 2f
            val top = itemView.top.toFloat()
            val bottom = itemView.bottom.toFloat()
            val left = if (dX > 0) itemView.left.toFloat() else itemView.right + dX
            val right = if (dX > 0) dX else itemView.right.toFloat()
            val contentCenterY = top + contentMarginTop + contentHeight/2
            val centerX = (panelWidth / 2).toFloat()
            val iconTopY = top + contentMarginTop
            val textTopY = iconTopY + iconSize + textTopPadding

            var startAnimation = false
            var isInfoVisible = false
            if (abs(dX) > itemView.width * SWIPE_ACTIVE_BORDER_PERCENT) {
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

            //draw icon
            c.save()

            c.translate((itemView.right - panelWidth).toFloat(), top)
            c.translate(panelWidth / 2f - iconSize / 2f, contentMarginTop)
            c.scale(currentScale, currentScale, iconSize / 2f, iconSize / 2f + textStaticLayout.height / 2)
            icon.draw(c)

            //draw text
            c.translate(iconSize / 2f - textStaticLayout.width / 2f, (iconSize + textTopPadding).toFloat())
            textStaticLayout.draw(c)
            c.restore()


            if (abs(dX) > itemView.width * SWIPE_BORDER_PERCENT) {
                return
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun getSwipeEscapeVelocity(defaultValue: Float) = defaultValue * 8f

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 2f//so we can't call onSwiped()
}