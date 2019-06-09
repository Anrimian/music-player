package com.github.anrimian.musicplayer.ui.utils.views.recycler_view;

import android.content.Context;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.domain.utils.java.CompositeCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.swipe_to_delete.SwipeToDeleteItemDecorator;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.swipe_to_delete.SwipeToDeleteTouchHelperCallback;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;

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

            @Override
            protected int getVerticalSnapPreference() {
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

    public static void attachSwipeToDelete(RecyclerView recyclerView,
                                           @ColorInt int backgroundColor,
                                           Callback<Integer> swipeCallback) {
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

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteTouchHelperCallback(
                backgroundColor, compositeCallback));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public static void attachDynamicShadow(RecyclerView recyclerView, View shadow) {
        shadow.setVisibility(
                recyclerView.computeVerticalScrollOffset() > 0? VISIBLE: View.GONE
        );
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                animateVisibility(
                        shadow,
                        recyclerView.computeVerticalScrollOffset() > 0? VISIBLE: GONE
                );
            }
        });
    }
}
