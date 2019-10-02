package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator;


import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.domain.utils.java.Function;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.ContentCheckFunction;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.PayloadFunction;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffCallback;

import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.DiffUtil.calculateDiff;

@Deprecated
public class DiffCalculator<T> {

    private final Function<List<T>> currentListFunction;
    private final ContentCheckFunction<T> contentCheckFunction;
    private final PayloadFunction<T> payloadFunction;
    private final boolean detectMoves;

    @Nullable
    private final Callback<T> onDeletedCallback;

    public DiffCalculator(Function<List<T>> currentListFunction,
                          ContentCheckFunction<T> contentCheckFunction,
                          PayloadFunction<T> payloadFunction) {
        this(currentListFunction,
                contentCheckFunction,
                payloadFunction,
                true);
    }

    public DiffCalculator(Function<List<T>> currentListFunction,
                          ContentCheckFunction<T> contentCheckFunction) {
        this(currentListFunction,
                contentCheckFunction,
                new SimpleDiffCallback.PayloadDefaultFunction<>(),
                true);
    }

    public DiffCalculator(Function<List<T>> currentListFunction,
                          ContentCheckFunction<T> contentCheckFunction,
                          Callback<T> onDeletedCallback) {
        this(currentListFunction,
                contentCheckFunction,
                new SimpleDiffCallback.PayloadDefaultFunction<>(),
                true,
                onDeletedCallback);
    }

    public DiffCalculator(Function<List<T>> currentListFunction,
                          ContentCheckFunction<T> contentCheckFunction,
                          PayloadFunction<T> payloadFunction,
                          boolean detectMoves) {
        this(currentListFunction,
                contentCheckFunction,
                payloadFunction,
                detectMoves,
                null);
    }

    public DiffCalculator(Function<List<T>> currentListFunction,
                          ContentCheckFunction<T> contentCheckFunction,
                          PayloadFunction<T> payloadFunction,
                          boolean detectMoves,
                          @Nullable Callback<T> onDeletedCallback) {
        this.currentListFunction = currentListFunction;
        this.contentCheckFunction = contentCheckFunction;
        this.payloadFunction = payloadFunction;
        this.detectMoves = detectMoves;
        this.onDeletedCallback = onDeletedCallback;
    }

    public ListUpdate<T> calculateChange(List<T> newList) {
        return calculateChange(newList, detectMoves);
    }

    //unsafe of often calls, think about it
    public ListUpdate<T> calculateChange(List<T> newList, boolean detectMoves) {
        synchronized (this) {
            List<T> oldList = currentListFunction.call();
            DiffUtil.DiffResult diffResult = calculateDiff(new SimpleDiffCallback<>(
                    oldList,
                    newList,
                    contentCheckFunction,
                    payloadFunction), detectMoves);
            DiffUpdateCallback diffUpdateCallback = new DiffUpdateCallback(oldList);
            diffResult.dispatchUpdatesTo(diffUpdateCallback);
            return new ListUpdate<>(new ArrayList<>(newList), diffResult);
        }
    }

    private class DiffUpdateCallback implements ListUpdateCallback {

        private final List<T> oldList;

        private DiffUpdateCallback(List<T> oldList) {
            this.oldList = oldList;
        }

        @Override
        public void onInserted(int position, int count) {

        }

        @Override
        public void onRemoved(int position, int count) {
            if (onDeletedCallback != null) {
                for (int i = position; i < position+count; i++) {
                    T oldItem = oldList.get(position);
                    onDeletedCallback.call(oldItem);
                }
            }
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {

        }

        @Override
        public void onChanged(int position, int count, @Nullable Object payload) {

        }
    }
}
