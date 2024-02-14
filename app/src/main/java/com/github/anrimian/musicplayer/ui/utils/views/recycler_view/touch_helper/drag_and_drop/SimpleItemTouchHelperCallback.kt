package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop

import android.animation.ObjectAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SimpleItemTouchHelperCallback(
    private val horizontalDrag: Boolean = false,
    private val isLongPressDragEnabled: Boolean = true,
    private val shouldNotDragViewHolder: (RecyclerView.ViewHolder) -> Boolean = { false },
    private val dragElevation: Float = TOP_Z
) : ItemTouchHelper.Callback() {

    private var onMovedListener: ((from: Int, to: Int) -> Unit)? = null
    private var onStartDragListener: ((position: Int) -> Unit)? = null
    private var onEndDragListener: ((position: Int) -> Unit)? = null

    private var startDragX = 0f
    private var startDragY = 0f

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (onStartDragListener != null && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            onStartDragListener!!(viewHolder!!.bindingAdapterPosition)
            startDragX = viewHolder.itemView.x
            startDragY = viewHolder.itemView.y
        }
        setIsDragging(viewHolder, true)
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (onEndDragListener != null) {
            onEndDragListener!!(viewHolder.bindingAdapterPosition)
        }
        setIsDragging(viewHolder, false)
        super.clearView(recyclerView, viewHolder)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        if (shouldNotDragViewHolder(target)) {
            return false
        }
        if (onMovedListener != null) {
            onMovedListener!!(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        }
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        if (shouldNotDragViewHolder(viewHolder)) {
            return ItemTouchHelper.ACTION_STATE_IDLE
        }
        var directions = ItemTouchHelper.DOWN or ItemTouchHelper.UP
        if (horizontalDrag) {
            directions = directions or (ItemTouchHelper.START or ItemTouchHelper.END)
        }
        return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, directions)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return isLongPressDragEnabled
    }

    fun setOnMovedListener(onMovedListener: (from: Int, to: Int) -> Unit) {
        this.onMovedListener = onMovedListener
    }

    fun setOnStartDragListener(onStartDragListener: (position: Int) -> Unit) {
        this.onStartDragListener = onStartDragListener
    }

    fun setOnEndDragListener(onEndDragListener: (position: Int) -> Unit) {
        this.onEndDragListener = onEndDragListener
    }

    private fun setIsDragging(viewHolder: RecyclerView.ViewHolder?, dragging: Boolean) {
        if (viewHolder == null) {
            return
        }
        if (viewHolder is DragListener) {
            viewHolder.onDragStateChanged(dragging)
        }
        val baseElevation = if (dragging) BOTTOM_Z else dragElevation
        val dragElevation = if (dragging) dragElevation else BOTTOM_Z
        val elevationAnimator = ObjectAnimator.ofFloat(
            viewHolder.itemView,
            "translationZ",
            baseElevation,
            dragElevation
        )
        elevationAnimator.duration = DEFAULT_DRAG_ANIMATION_DURATION.toLong()
        elevationAnimator.start()
    }

    companion object {
        private const val BOTTOM_Z = 0f
        private const val TOP_Z = 8f
    }

}