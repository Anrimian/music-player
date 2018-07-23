package com.github.anrimian.simplemusicplayer.ui.utils.views.recycler_view.diff_utils;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * Created on 17.02.2018.
 */

public class SimpleDiffCallback<T> extends DiffUtil.Callback {

    private List<T> oldList;
    private List<T> newList;

    private ContentCheckFunction<T> contentCheckFunction;

    public SimpleDiffCallback(List<T> oldList, List<T> newList) {
        this(oldList, newList, new EqualContentCheckFunction<>());
    }

    public SimpleDiffCallback(List<T> oldList,
                              List<T> newList,
                              ContentCheckFunction<T> contentCheckFunction) {
        this.oldList = oldList;
        this.newList = newList;
        this.contentCheckFunction = contentCheckFunction;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return contentCheckFunction.areContentsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

    private static class EqualContentCheckFunction<T> implements ContentCheckFunction<T> {

        @Override
        public boolean areContentsTheSame(T oldItem, T newItem) {
            return oldItem.equals(newItem);
        }
    }
}
