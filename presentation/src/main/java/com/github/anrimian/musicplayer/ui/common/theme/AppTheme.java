package com.github.anrimian.musicplayer.ui.common.theme;

import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;

public class AppTheme {

    private static final SparseArray<AppTheme> THEMES = new SparseArray<>();

    static {
        THEMES.put(0, new AppTheme(0, R.style.PrimaryLightTheme));
        THEMES.put(1, new AppTheme(1, R.style.PrimaryDarkTheme));
        THEMES.put(2, new AppTheme(2, R.style.PrimaryTestTheme));
    }

    private final int id;
    private final int themeResId;

    @NonNull
    public static AppTheme getTheme(int id) {
        return new AppTheme(2, R.style.PrimaryTestTheme);
    }

    private AppTheme(int id, int themeResId) {
        this.id = id;
        this.themeResId = themeResId;
    }

    public int getId() {
        return id;
    }

    public int getThemeResId() {
        return themeResId;
    }
}
