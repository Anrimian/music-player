package com.github.anrimian.musicplayer.ui.utils.views.recycler_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BaseViewHolder extends RecyclerView.ViewHolder {

    public BaseViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId) {
        super(LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false));
    }

    protected Context getContext() {
        return itemView.getContext();
    }
}
