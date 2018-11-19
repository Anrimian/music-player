package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class ListUpdate<T> {

    private final List<T> newList;
    private final DiffUtil.DiffResult diffResult;

    ListUpdate(List<T> newList, DiffUtil.DiffResult diffResult) {
        this.newList = newList;
        this.diffResult = diffResult;
    }

    public List<T> getNewList() {
        return newList;
    }

    public DiffUtil.DiffResult getDiffResult() {
        return diffResult;
    }
}
