package com.github.anrimian.musicplayer.ui.settings.themes.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;

public class ThemesAdapter extends RecyclerView.Adapter<ThemesViewHolder> {

    private final AppTheme[] themes;
    private final Callback<AppTheme> themeClickListener;

    public ThemesAdapter(AppTheme[] themes, Callback<AppTheme> themeClickListener) {
        this.themes = themes;
        this.themeClickListener = themeClickListener;
    }

    @NonNull
    @Override
    public ThemesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ThemesViewHolder(inflater, parent, themeClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemesViewHolder holder, int position) {
        holder.bind(themes[position]);
    }

    @Override
    public int getItemCount() {
        return themes.length;
    }
}
