package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils;

public interface PayloadFunction<T> {

    Object getChangePayload(T oldItem, T newItem);
}
