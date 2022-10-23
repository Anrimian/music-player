package com.github.anrimian.musicplayer.ui.utils.views.recycler_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter;

public class BaseViewHolder extends MvpDiffAdapter.MvpViewHolder {

    public BaseViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId) {
        super(LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false));
    }

    protected Context getContext() {
        return itemView.getContext();
    }
}
