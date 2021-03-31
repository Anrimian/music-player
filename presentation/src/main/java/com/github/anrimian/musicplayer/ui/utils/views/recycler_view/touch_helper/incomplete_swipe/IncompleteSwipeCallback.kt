package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.incomplete_swipe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.R

fun createSwipeCallback(recyclerView: RecyclerView,
//                      @ColorInt backgroundColor: Int,
                        @DrawableRes iconRes: Int,
                        @StringRes textResId: Int,
                        swipeCallback: (Int) -> Unit): IncompleteSwipeCallback {
    return IncompleteSwipeCallback(
//            recyclerView,
//            backgroundColor,
//            swipeFlags,
            recyclerView.context,
            iconRes,
            textResId,
            R.dimen.swipe_panel_width,
            R.dimen.swipe_panel_padding_end,
            R.dimen.swipe_panel_text_top_padding,
            R.dimen.swipe_panel_icon_size,
            R.dimen.swipe_panel_text_size,
            swipeCallback,
    )
}


class IncompleteSwipeCallback(context: Context,
                              @DrawableRes iconRes: Int,
                              @StringRes textResId: Int,
                              @DimenRes panelWidthRes: Int,
                              @DimenRes panelEndPaddingRes: Int,
                              @DimenRes textTopPaddingRes: Int,
                              @DimenRes iconSizeRes: Int,
                              @DimenRes textSizeRes: Int,
                              private val swipeCallback: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {

    private val panelWidth = context.resources.getDimensionPixelSize(panelWidthRes)
    private val panelEndPadding = context.resources.getDimensionPixelSize(panelEndPaddingRes)
    private val iconSize = context.resources.getDimensionPixelSize(iconSizeRes)
    private val textTopPadding = context.resources.getDimensionPixelSize(textTopPaddingRes)
    private val textColor = Color.WHITE

    private val icon = context.resources.getDrawable(iconRes).apply {
        setTint(textColor)
        setBounds(0, 0, iconSize, iconSize)
    }

    private val textPaint = TextPaint().apply {
        color = textColor
        isAntiAlias = true
        textSize = context.resources.getDimension(textSizeRes)
    }

    private val textStaticLayout = StaticLayout(
            context.getString(textResId),
            textPaint,
            panelWidth,
            Layout.Alignment.ALIGN_CENTER,
            1.0f,
            0.0f,
            false
    )


    override fun onMove(recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipeCallback(viewHolder.adapterPosition)
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 8
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.33f
    }

    override fun getBoundingBoxMargin(): Int {
        return 50
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
            val top = itemView.top.toFloat()
            val bottom = itemView.bottom.toFloat()
            val left = if (dX > 0) itemView.left.toFloat() else itemView.right + dX
            val right = if (dX > 0) dX else itemView.right.toFloat()
            val centerY = top + itemView.height / 2f
            val centerX: Float = (panelWidth shr 1).toFloat()

//            if (abs(dX) > itemView.width * getSwipeThreshold(viewHolder)) {
//                return
//            }

            //draw icon
            c.save()
            c.translate(itemView.right - (centerX + panelEndPadding + (iconSize shr 1)), centerY - iconSize)
            icon.draw(c)
            c.restore()

            //draw text
            c.translate((itemView.right - (panelWidth + panelEndPadding)).toFloat(), centerY + textTopPadding)
            textStaticLayout.draw(c)
            c.restore()
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}