package com.github.anrimian.musicplayer.ui.common.theme;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;

public enum AppTheme {
    DEFAULT(0, R.style.PrimaryLightTheme),
    DARK(1, R.style.PrimaryDarkTheme),
    TEST(2, R.style.PrimaryTestTheme);

    private final int id;
    private final int themeResId;

    @NonNull
    public static AppTheme getTheme(int id) {
        for (AppTheme type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return DEFAULT;
    }

    AppTheme(int id, int themeResId) {
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
