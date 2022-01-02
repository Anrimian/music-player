package com.github.anrimian.musicplayer.ui.utils.views.recycler_view;

import android.view.View;

/**
 * Created on 17.12.2017.
 */

public interface OnTransitionItemClickListener<T> {

    void onItemClick(T data, View... views);
}
