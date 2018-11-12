package com.github.anrimian.musicplayer.ui.utils.views.recycler_view;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;

public class RecyclerViewUtils {

    public static <T extends RecyclerView.ViewHolder> void viewHolders(RecyclerView recyclerView,
                                                                       Callback<T> callback) {
        for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            View view = recyclerView.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            //noinspection unchecked
            callback.call((T) viewHolder);
        }
    }

    public static void smoothScrollToTop(int position,
                                         RecyclerView.LayoutManager layoutManager,
                                         Context context,
                                         int duration) {
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(context) {

            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected int calculateTimeForScrolling(int dx) {
                return duration;
            }
        };
        smoothScroller.setTargetPosition(position);
        layoutManager.startSmoothScroll(smoothScroller);
    }
}
