package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

/**
 * Created on 17.02.2018.
 */

public class SimpleDiffCallback<T> extends DiffUtil.Callback {

    private final List<T> oldList;
    private final List<T> newList;

    private final ContentCheckFunction<T> contentCheckFunction;
    private final PayloadFunction<T> payloadFunction;

    public SimpleDiffCallback(List<T> oldList, List<T> newList) {
        this(oldList, newList, new EqualContentCheckFunction<>(), new PayloadDefaultFunction<>());
    }

    public SimpleDiffCallback(List<T> oldList,
                              List<T> newList,
                              ContentCheckFunction<T> contentCheckFunction) {
        this(oldList, newList, contentCheckFunction, new PayloadDefaultFunction<>());
    }

    /**
     * if you use this constructor, payload function must return null only if no changes has detected
     */
    public SimpleDiffCallback(List<T> oldList,
                              List<T> newList,
                              PayloadFunction<T> payloadFunction) {
        this(oldList,
                newList,
                new PayloadContentCheckFunction<>(payloadFunction),
                new PayloadDefaultFunction<>());
    }

    public SimpleDiffCallback(List<T> oldList,
                              List<T> newList,
                              ContentCheckFunction<T> contentCheckFunction,
                              PayloadFunction<T> payloadFunction) {
        this.oldList = oldList;
        this.newList = newList;
        this.contentCheckFunction = contentCheckFunction;
        this.payloadFunction = payloadFunction;
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
        T oldItem = oldList.get(oldItemPosition);
        T newItem = newList.get(newItemPosition);
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        T oldItem = oldList.get(oldItemPosition);
        T newItem = newList.get(newItemPosition);
        return contentCheckFunction.areContentsTheSame(oldItem, newItem);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        T oldItem = oldList.get(oldItemPosition);
        T newItem = newList.get(newItemPosition);
        return payloadFunction.getChangePayload(oldItem, newItem);
    }

    public static class EqualContentCheckFunction<T> implements ContentCheckFunction<T> {

        @Override
        public boolean areContentsTheSame(T oldItem, T newItem) {
            return oldItem.equals(newItem);
        }
    }

    public static class PayloadContentCheckFunction<T> implements ContentCheckFunction<T> {

        private final PayloadFunction<T> payloadFunction;

        public PayloadContentCheckFunction(PayloadFunction<T> payloadFunction) {
            this.payloadFunction = payloadFunction;
        }

        @Override
        public boolean areContentsTheSame(T oldItem, T newItem) {
            return payloadFunction.getChangePayload(oldItem, newItem) != null;
        }
    }

    public static class PayloadDefaultFunction<T> implements PayloadFunction<T> {

        @Override
        public List<Object> getChangePayload(T oldItem, T newItem) {
            return null;
        }
    }
}
