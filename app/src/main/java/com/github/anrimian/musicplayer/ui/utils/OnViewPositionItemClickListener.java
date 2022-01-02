package com.github.anrimian.musicplayer.ui.utils;

import android.view.View;

public interface OnViewPositionItemClickListener<T> {

    void onItemClick(View view, T data, int position);
}
