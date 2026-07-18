package com.github.anrimian.musicplayer.ui.utils.views.recycler_view

import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.attachFastScroller(useFabPadding: Boolean = false) {
    RecyclerViewUtils.attachFastScroller(this, useFabPadding)
}