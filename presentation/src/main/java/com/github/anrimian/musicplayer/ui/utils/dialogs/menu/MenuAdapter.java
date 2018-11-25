package com.github.anrimian.musicplayer.ui.utils.dialogs.menu;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MenuAdapter extends RecyclerView.Adapter<MenuViewHolder> {

    private final Menu menu;

    private OnItemClickListener<MenuItem> onItemClickListener;

    public MenuAdapter(Menu menu) {
        this.menu = menu;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        holder.bind(menu.getItem(position));
    }

    @Override
    public int getItemCount() {
        return menu.size();
    }

    public void setOnItemClickListener(OnItemClickListener<MenuItem> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
