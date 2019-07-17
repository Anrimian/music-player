package com.github.anrimian.musicplayer.ui.utils.views.recycler_view;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BaseViewHolder extends RecyclerView.ViewHolder {

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    protected Context getContext() {
        return itemView.getContext();
    }
}
