package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{

    private OnMovedListener onMovedListener;
    private OnStartDragListener onStartDragListener;
    private OnEndDragListener onEndDragListener;

    public SimpleItemTouchHelperCallback() {
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
            onMovedListener.onItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
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
        return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,ItemTouchHelper.DOWN
                | ItemTouchHelper.UP
                | ItemTouchHelper.START
                | ItemTouchHelper.END);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
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
        if (viewHolder instanceof DragListener) {
            ((DragListener) viewHolder).onDragStateChanged(dragging);
        }

        if (viewHolder != null) {
            float scale = dragging ? 1.05f : 1f;
            viewHolder.itemView.animate()
                    .setDuration(150)
                    .scaleX(scale)
                    .scaleY(scale)
                    .start();
        }
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
