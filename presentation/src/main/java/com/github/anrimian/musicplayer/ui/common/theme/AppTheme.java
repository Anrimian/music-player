package com.github.anrimian.musicplayer.ui.common.theme;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;

public enum AppTheme {
    WHITE_DEFAULT(0,
            R.style.PrimaryLightTheme,
            R.string.white_theme,
            R.color.colorPrimary,
            R.color.light_light_gray),
    DARK(1,
            R.style.PrimaryDarkTheme,
            R.string.dark_theme,
            R.color.darkColorPrimary,
            R.color.dark_background_level_0);

    private final int id;
    private final int themeResId;
    private final int descriptionId;
    private final int primaryColorId;
    private final int backgroundColorId;

    @NonNull
    public static AppTheme getTheme(int id) {
        for (AppTheme type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return WHITE_DEFAULT;
    }

    AppTheme(int id, int themeResId, int descriptionId, int primaryColorId, int backgroundColorId) {
        this.id = id;
        this.themeResId = themeResId;
        this.descriptionId = descriptionId;
        this.primaryColorId = primaryColorId;
        this.backgroundColorId = backgroundColorId;
    }

    public int getId() {
        return id;
    }

    public int getThemeResId() {
        return themeResId;
    }

    public int getDescriptionId() {
        return descriptionId;
    }

    public int getPrimaryColorId() {
        return primaryColorId;
    }

    public int getBackgroundColorId() {
        return backgroundColorId;
    }
}
