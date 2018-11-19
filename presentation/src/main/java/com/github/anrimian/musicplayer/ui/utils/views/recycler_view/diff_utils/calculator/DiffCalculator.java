package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator;

import com.github.anrimian.musicplayer.domain.utils.java.Function;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.ContentCheckFunction;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.PayloadFunction;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffCallback;

import java.util.List;

import static androidx.recyclerview.widget.DiffUtil.calculateDiff;

public class DiffCalculator<T> {

    private final Function<List<T>> currentListFunction;
    private final ContentCheckFunction<T> contentCheckFunction;
    private final PayloadFunction<T> payloadFunction;
    private final boolean detectMoves;

    public DiffCalculator(Function<List<T>> currentListFunction,
                          ContentCheckFunction<T> contentCheckFunction,
                          PayloadFunction<T> payloadFunction,
                          boolean detectMoves) {
        this.currentListFunction = currentListFunction;
        this.contentCheckFunction = contentCheckFunction;
        this.payloadFunction = payloadFunction;
        this.detectMoves = detectMoves;
    }

    public DiffCalculator(Function<List<T>> currentListFunction,
                          ContentCheckFunction<T> contentCheckFunction,
                          PayloadFunction<T> payloadFunction) {
        this(currentListFunction, contentCheckFunction, payloadFunction, true);
    }

    public DiffCalculator(Function<List<T>> currentListFunction,
                          ContentCheckFunction<T> contentCheckFunction) {
        this(currentListFunction,
                contentCheckFunction,
                new SimpleDiffCallback.PayloadDefaultFunction<>(),
                true);
    }

    public ListUpdate<T> calculateChange(List<T> newList) {
        return calculateChange(newList, detectMoves);
    }

    public ListUpdate<T> calculateChange(List<T> newList, boolean detectMoves) {
        return new ListUpdate<>(newList, calculateDiff(new SimpleDiffCallback<>(
                currentListFunction.call(),
                newList,
                contentCheckFunction,
                payloadFunction), detectMoves));
    }
}
