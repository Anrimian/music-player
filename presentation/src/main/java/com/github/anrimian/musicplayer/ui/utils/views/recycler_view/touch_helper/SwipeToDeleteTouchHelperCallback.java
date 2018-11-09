package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;

import static com.arellomobile.mvp.MvpFacade.init;

public class SwipeToDeleteTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    Drawable background;
    Drawable xMark;
    int xMarkMargin;
    boolean initiated;

    private final Callback<Integer> swipedCallback;

    public SwipeToDeleteTouchHelperCallback(Context context, Callback<Integer> swipedCallback) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.swipedCallback = swipedCallback;
        background = new ColorDrawable(Color.RED);
        xMark = ContextCompat.getDrawable(context, R.drawable.ic_menu);
        xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        initiated = true;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        swipedCallback.call(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;

        // not sure why, but this method get's called for viewholder that are already swiped away
        if (viewHolder.getAdapterPosition() == -1) {
            // not interested in those
            return;
        }

        if (!initiated) {
            init();
        }

        // draw red background
        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        background.draw(c);

        int xMarkLeft = 0;
        int xMarkRight = 0;
        int xMarkTop = itemView.getTop() + (100 - xMark.getIntrinsicHeight()) / 2;
        int xMarkBottom = xMarkTop + xMark.getIntrinsicHeight();
        if (dX < 0) {
            xMarkLeft = itemView.getRight() - xMarkMargin - xMark.getIntrinsicWidth();
            xMarkRight = itemView.getRight() - xMarkMargin;
        } else {
            xMarkLeft = itemView.getLeft() + xMarkMargin;
            xMarkRight = itemView.getLeft() + xMarkMargin + xMark.getIntrinsicWidth();
        }
        xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
        xMark.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
