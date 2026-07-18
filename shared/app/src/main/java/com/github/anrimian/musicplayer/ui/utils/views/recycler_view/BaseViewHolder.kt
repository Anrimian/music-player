package com.github.anrimian.musicplayer.ui.utils.views.recycler_view

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter.MvpViewHolder

open class BaseViewHolder(
    parent: ViewGroup,
    @LayoutRes layoutResId: Int
) : MvpViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
) {
    protected fun getContext(): Context = itemView.context

    protected fun getResources(): Resources = getContext().resources
}