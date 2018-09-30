package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils;

import android.support.v7.util.DiffUtil;

import com.github.anrimian.musicplayer.domain.utils.java.Function;

import java.util.List;

import static android.support.v7.util.DiffUtil.*;

public class ListChangeCalculator<T> {

    private final Function<List<T>> currentListFunction;
    private final ContentCheckFunction<T> contentCheckFunction;

    public ListChangeCalculator(Function<List<T>> currentListFunction,
                                ContentCheckFunction<T> contentCheckFunction) {
        this.currentListFunction = currentListFunction;
        this.contentCheckFunction = contentCheckFunction;
    }

    public ChangeResult<T> calculateChange(List<T> newList) {
        return new ChangeResult<>(newList, calculateDiff(new SimpleDiffCallback<>(currentListFunction.call(),
                newList,
                contentCheckFunction), false));
    }

    public static class ChangeResult<T> {

        private final List<T> newList;
        private final DiffResult diffResult;

        public ChangeResult(List<T> newList, DiffResult diffResult) {
            this.newList = newList;
            this.diffResult = diffResult;
        }

        public List<T> getNewList() {
            return newList;
        }

        public DiffResult getDiffResult() {
            return diffResult;
        }
    }
}
