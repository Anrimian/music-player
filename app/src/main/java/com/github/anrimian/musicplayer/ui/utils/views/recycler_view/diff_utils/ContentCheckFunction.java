package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils;

public interface ContentCheckFunction<T> {

    boolean areContentsTheSame(T oldItem, T newItem);
}
