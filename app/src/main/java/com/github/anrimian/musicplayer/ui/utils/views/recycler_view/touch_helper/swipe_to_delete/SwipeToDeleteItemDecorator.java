package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.swipe_to_delete;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class SwipeToDeleteItemDecorator extends RecyclerView.ItemDecoration {

    private final Paint paint = new Paint();

    public SwipeToDeleteItemDecorator(@ColorInt int color) {
        paint.setColor(color);
    }

    @Override
    public void onDraw(@NonNull Canvas c,
                       @NonNull RecyclerView recyclerView,
                       @NonNull RecyclerView.State state) {

        RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
        if (itemAnimator != null && itemAnimator.isRunning()) {

            View lastViewComingDown = null;
            View firstViewComingUp = null;

            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) {
                return;
            }
            int childCount = layoutManager.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getLayoutManager().getChildAt(i);
                if (child == null) {
                    return;
                }
                if (child.getTranslationY() < 0) {
                    lastViewComingDown = child;
                } else if (child.getTranslationY() > 0) {
                    if (firstViewComingUp == null) {
                        firstViewComingUp = child;
                    }
                }
            }

            final int left = 0;
            final int right = recyclerView.getWidth();

            int top = 0;
            int bottom = 0;

            if (lastViewComingDown != null && firstViewComingUp != null) {
                top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
            } else if (lastViewComingDown != null) {
                top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                bottom = lastViewComingDown.getBottom();
            } else if (firstViewComingUp != null) {
                top = firstViewComingUp.getTop();
                bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
            }

            c.drawRect(left, top, right, bottom, paint);
        }
        super.onDraw(c, recyclerView, state);
    }


}
