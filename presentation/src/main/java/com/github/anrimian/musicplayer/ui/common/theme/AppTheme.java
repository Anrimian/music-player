package com.github.anrimian.musicplayer.ui.common.theme;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;

public enum AppTheme {
    WHITE_PURPLE_DEFAULT(0,
            R.style.PrimaryLightTheme,
            R.string.white_purple_theme,
            R.color.colorPrimary,
            android.R.color.white,
            false),
    WHITE_INDIGO(2,
            R.style.PrimaryBlueTheme,
            R.string.white_indigo_theme,
            R.color.colorBluePrimary,
            android.R.color.white,
            false),
    WHITE_TEAL(3,
            R.style.PrimaryTealTheme,
            R.string.white_teal_theme,
            R.color.colorTealPrimary,
            android.R.color.white,
            false),
    DARK(1,
            R.style.PrimaryDarkTheme,
            R.string.dark_theme,
            R.color.darkColorPrimary,
            android.R.color.black,
            true);

    private final int id;
    private final int themeResId;
    private final int descriptionId;
    private final int primaryColorId;
    private final int backgroundColorId;
    private final boolean isDark;

    @NonNull
    public static AppTheme getTheme(int id) {
        for (AppTheme type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return WHITE_PURPLE_DEFAULT;
    }

    AppTheme(int id,
             int themeResId,
             int descriptionId,
             int primaryColorId,
             int backgroundColorId,
             boolean isDark) {
        this.id = id;
        this.themeResId = themeResId;
        this.descriptionId = descriptionId;
        this.primaryColorId = primaryColorId;
        this.backgroundColorId = backgroundColorId;
        this.isDark = isDark;
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

    public boolean isDark() {
        return isDark;
    }
}
