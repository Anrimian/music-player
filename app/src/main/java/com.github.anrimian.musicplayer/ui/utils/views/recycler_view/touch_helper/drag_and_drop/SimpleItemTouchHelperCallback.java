package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop;


import android.animation.ObjectAnimator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{

    private static final float BOTTOM_Z = 0f;
    private static final float TOP_Z = 8f;

    private final boolean horizontalDrag;
    private final boolean isLongPressDragEnabled;

    private OnMovedListener onMovedListener;
    private OnStartDragListener onStartDragListener;
    private OnEndDragListener onEndDragListener;

    public SimpleItemTouchHelperCallback(boolean isLongPressDragEnabled) {
        this(false, isLongPressDragEnabled);
    }

    public SimpleItemTouchHelperCallback(boolean horizontalDrag, boolean isLongPressDragEnabled) {
        this.horizontalDrag = horizontalDrag;
        this.isLongPressDragEnabled = isLongPressDragEnabled;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (onStartDragListener != null && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            onStartDragListener.onStartDrag();
        }
        setIsDragging(viewHolder, true);
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        if (onEndDragListener != null) {
            onEndDragListener.onEndDrag();
        }
        setIsDragging(viewHolder, false);
        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        if (onMovedListener != null) {
            onMovedListener.onItemMoved(viewHolder.getBindingAdapterPosition(), target.getBindingAdapterPosition());
            return true;
        }
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        int directions = ItemTouchHelper.DOWN | ItemTouchHelper.UP;
        if (horizontalDrag) {
            directions |= ItemTouchHelper.START | ItemTouchHelper.END;
        }
        return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, directions);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return isLongPressDragEnabled;
    }

    public void setOnMovedListener(OnMovedListener onMovedListener) {
        this.onMovedListener = onMovedListener;
    }

    public void setOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    public void setOnEndDragListener(OnEndDragListener onEndDragListener) {
        this.onEndDragListener = onEndDragListener;
    }

    private void setIsDragging(RecyclerView.ViewHolder viewHolder, boolean dragging) {
        if (viewHolder == null) {
            return;
        }
        if (viewHolder instanceof DragListener) {
            ((DragListener) viewHolder).onDragStateChanged(dragging);
        }
        float baseElevation = dragging? BOTTOM_Z: TOP_Z;
        float dragElevation = dragging? TOP_Z: BOTTOM_Z;
        ObjectAnimator elevationAnimator = ObjectAnimator.ofFloat(viewHolder.itemView,
                "translationZ",
                baseElevation,
                dragElevation);
        elevationAnimator.setDuration(DEFAULT_DRAG_ANIMATION_DURATION);
        elevationAnimator.start();
    }

    public interface OnMovedListener {
        void onItemMoved(int from, int to);
    }

    public interface OnStartDragListener {
        void onStartDrag();
    }

    public interface OnEndDragListener {
        void onEndDrag();
    }

}
