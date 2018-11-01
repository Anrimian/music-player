package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils;

import android.os.Parcelable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import static android.support.v7.util.DiffUtil.calculateDiff;

public class DiffUtilHelper {

    public static <T> void update(List<T> oldList,
                                  List<T> newList,
                                  ContentCheckFunction<T> contentCheckFunction,
                                  RecyclerView recyclerView) {
        Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        calculateDiff(new SimpleDiffCallback<>(oldList, newList, contentCheckFunction), false)
                .dispatchUpdatesTo(recyclerView.getAdapter());
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    public static void update(DiffUtil.DiffResult diffResult,
                              RecyclerView recyclerView) {
        Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        diffResult.dispatchUpdatesTo(recyclerView.getAdapter());
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }
}
