package com.github.anrimian.musicplayer.ui.common.theme;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;

//TODO more dark themes and save previous selected dark theme for night mode
//TODO completely light and dark themes, dynamic/disable shadows
public enum AppTheme {
    WHITE_PURPLE_DEFAULT(0,
            R.style.PrimaryLightTheme,
            0,
            R.color.colorPrimary,
            R.color.light_background_level_0,
            R.color.colorAccent,
            R.color.color_control_highlight,
            false),
    WHITE_INDIGO(2,
            R.style.PrimaryBlueTheme,
            0,
            R.color.colorBluePrimary,
            R.color.light_background_level_0,
            R.color.colorGreenAccent,
            R.color.color_control_highlight,
            false),
    WHITE_TEAL(3,
            R.style.PrimaryTealTheme,
            0,
            R.color.colorTealPrimary,
            R.color.light_background_level_0,
            R.color.colorPinkAccent,
            R.color.color_control_highlight,
            false),
    DARK(1,
            R.style.PrimaryDarkTheme,
            0,
            R.color.darkColorPrimary,
            R.color.dark_background_level_1,
            R.color.colorAccentDark,
            R.color.color_control_highlight_dark,
            true);

    private final int id;
    private final int themeResId;
    private final int descriptionId;
    private final int primaryColorId;
    private final int backgroundColorId;
    private final int accentColorId;
    private final int rippleColorId;
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
             int accentColorId, int rippleColorId, boolean isDark) {
        this.id = id;
        this.themeResId = themeResId;
        this.descriptionId = descriptionId;
        this.primaryColorId = primaryColorId;
        this.backgroundColorId = backgroundColorId;
        this.accentColorId = accentColorId;
        this.rippleColorId = rippleColorId;
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

    public int getAccentColorId() {
        return accentColorId;
    }

    public int getRippleColorId() {
        return rippleColorId;
    }

    public boolean isDark() {
        return isDark;
    }
}
