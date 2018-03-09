package com.github.anrimian.simplemusicplayer.ui.utils.views.recycler_view;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * Created on 17.02.2018.
 */

public class SimpleDiffCallback extends DiffUtil.Callback {

    private List oldList;
    private List newList;

    public SimpleDiffCallback(List oldList, List newList) {
        this.oldList = oldList;
        this.newList = newList;
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
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
