package com.github.anrimian.musicplayer.ui.settings.themes.view;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;

public class ThemesAdapter extends RecyclerView.Adapter<ThemesViewHolder> {

    private final AppTheme[] themes;
    private final AppTheme currentTheme;
    private final Callback<AppTheme> themeClickListener;

    public ThemesAdapter(AppTheme[] themes,
                         AppTheme currentTheme,
                         Callback<AppTheme> themeClickListener) {
        this.themes = themes;
        this.currentTheme = currentTheme;
        this.themeClickListener = themeClickListener;
    }

    @NonNull
    @Override
    public ThemesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThemesViewHolder(parent, themeClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemesViewHolder holder, int position) {
        AppTheme theme = themes[position];
        holder.bind(theme, theme.equals(currentTheme));
    }

    @Override
    public int getItemCount() {
        return themes.length;
    }
}
