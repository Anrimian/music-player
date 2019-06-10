package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_swipe;


import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.domain.utils.java.CompositeCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.DragListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.swipe_to_delete.SwipeToDeleteItemDecorator;

import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

public class DragAndSwipeTouchHelperCallback extends ItemTouchHelper.Callback{

    private static final float BOTTOM_Z = 0f;
    private static final float TOP_Z = 8f;

    private final Paint paint = new Paint();

    private final Callback<Integer> swipeCallback;

    private final int swipeFlags;
    private final int dragFlags;

    private OnMovedListener onMovedListener;
    private OnStartDragListener onStartDragListener;
    private OnEndDragListener onEndDragListener;

    private boolean dragging;

    public static DragAndSwipeTouchHelperCallback withSwipeToDelete(RecyclerView recyclerView,
                                                                    @ColorInt int backgroundColor,
                                                                    Callback<Integer> swipeCallback) {
        return withSwipeToDelete(recyclerView,
                backgroundColor,
                swipeCallback,
                ItemTouchHelper.START | ItemTouchHelper.END);
    }

    public static DragAndSwipeTouchHelperCallback withSwipeToDelete(RecyclerView recyclerView,
                                                                    @ColorInt int backgroundColor,
                                                                    Callback<Integer> swipeCallback,
                                                                    int swipeFlags) {
        CompositeCallback<Integer> compositeCallback = new CompositeCallback<>();
        compositeCallback.add(swipeCallback);
        compositeCallback.add(i -> {
            RecyclerView.ItemDecoration decoration = new SwipeToDeleteItemDecorator(backgroundColor);
            recyclerView.addItemDecoration(decoration);
            RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
            if (itemAnimator != null) {
                recyclerView.postDelayed(() ->
                                itemAnimator.isRunning(() -> recyclerView.removeItemDecoration(decoration)),
                        itemAnimator.getRemoveDuration());
            }
        });

        return new DragAndSwipeTouchHelperCallback(backgroundColor, compositeCallback, swipeFlags);
    }

    public DragAndSwipeTouchHelperCallback(@ColorInt int color, Callback<Integer> swipeCallback) {
        this(color, swipeCallback, ItemTouchHelper.START | ItemTouchHelper.END);
    }

    public DragAndSwipeTouchHelperCallback(@ColorInt int color,
                                           Callback<Integer> swipeCallback,
                                           int swipeFlags) {
        this(color, swipeCallback, swipeFlags, UP | DOWN);
    }

    public DragAndSwipeTouchHelperCallback(@ColorInt int color,
                                           Callback<Integer> swipeCallback,
                                           int swipeFlags,
                                           int dragFlags) {
        this.swipeFlags = swipeFlags;
        this.dragFlags = dragFlags;
        this.swipeCallback = swipeCallback;
        paint.setColor(color);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            dragging = true;
            setIsDragging(viewHolder, true);
            if (onStartDragListener != null) {
                onStartDragListener.onStartDrag(viewHolder.getAdapterPosition());
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        if (dragging) {
            dragging = false;
            if (onEndDragListener != null) {
                onEndDragListener.onEndDrag(viewHolder.getAdapterPosition());
            }
            setIsDragging(viewHolder, false);
        }
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
        swipeCallback.call(viewHolder.getAdapterPosition());
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 8;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.33f;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;

            float top = itemView.getTop();
            float bottom = itemView.getBottom();
            float left = dX > 0? itemView.getLeft() : itemView.getRight() + dX;
            float right = dX > 0? dX : itemView.getRight();

            c.drawRect(left, top, right, bottom, paint);

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
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
        float dragElevation = dragging? TOP_Z: BOTTOM_Z;
        View itemView = viewHolder.itemView;
        ObjectAnimator elevationAnimator = ObjectAnimator.ofFloat(itemView,
                "translationZ",
                itemView.getElevation(),
                dragElevation);
        elevationAnimator.setDuration(DEFAULT_DRAG_ANIMATION_DURATION);
        elevationAnimator.start();
    }

    public interface OnMovedListener {
        void onItemMoved(int from, int to);
    }

    public interface OnStartDragListener {
        void onStartDrag(int position);
    }

    public interface OnEndDragListener {
        void onEndDrag(int position);
    }

}
