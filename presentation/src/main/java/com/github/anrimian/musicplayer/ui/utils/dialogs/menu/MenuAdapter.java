package com.github.anrimian.musicplayer.ui.utils.dialogs.menu;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getMenuItems;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuViewHolder> {

    private final List<? extends MenuItem> items;

    @LayoutRes
    private final int menuViewRes;

    @Nullable
    private final MenuItem selectedMenuItem;

    private OnItemClickListener<MenuItem> onItemClickListener;

    public MenuAdapter(Menu menu, @LayoutRes int menuViewRes) {
        this(menu, menuViewRes, null);
    }
    public MenuAdapter(Menu menu,
                       @LayoutRes int menuViewRes,
                       @Nullable MenuItem selectedMenuItem) {
        this(getMenuItems(menu), menuViewRes, selectedMenuItem);
    }

    public MenuAdapter(List<? extends MenuItem> items,
                       @LayoutRes int menuViewRes) {
        this(items, menuViewRes, null);
    }

    public MenuAdapter(List<? extends MenuItem> items,
                       @LayoutRes int menuViewRes,
                       @Nullable MenuItem selectedMenuItem) {
        this.items = items;
        this.menuViewRes = menuViewRes;
        this.selectedMenuItem = selectedMenuItem;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                menuViewRes,
                onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem menuItem = items.get(position);
        holder.bind(items.get(position), menuItem.equals(selectedMenuItem));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(OnItemClickListener<MenuItem> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
