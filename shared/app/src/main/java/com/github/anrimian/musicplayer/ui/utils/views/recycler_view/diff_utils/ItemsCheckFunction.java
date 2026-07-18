package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils;

import androidx.annotation.NonNull;

public interface ItemsCheckFunction<T> {

    boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem);
}
