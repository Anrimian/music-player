package com.github.anrimian.musicplayer.ui.common.theme;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.github.anrimian.musicplayer.R;

public class AppTheme {
    public static final AppTheme WHITE_PURPLE_TEAL = new AppTheme(0,
            R.style.PrimaryPurpleTheme,
            0,
            R.color.color_purple_primary,
            R.color.light_background_level_0,
            R.color.colorAccent,
            R.color.color_control_highlight,
            false);
    public static final AppTheme DARK = new AppTheme(1,
            R.style.PrimaryDarkTheme,
            0,
            R.color.darkColorPrimary,
            R.color.dark_background_level_1,
            R.color.colorAccentDark,
            R.color.color_control_highlight_dark,
            true);
    public static final AppTheme WHITE_INDIGO_GREEN = new AppTheme(2,
            R.style.PrimaryIndigoTheme,
            0,
            R.color.color_indigo_primary,
            R.color.light_background_level_0,
            R.color.colorGreenAccent,
            R.color.color_control_highlight,
            false);
    public static final AppTheme DARK_ORANGE = new AppTheme(4,
            R.style.DarkOrangeTheme,
            0,
            R.color.darkColorPrimary,
            R.color.dark_background_level_1,
            R.color.color_orange_dark_accent,
            R.color.color_control_highlight_dark,
            true);
    public static final AppTheme WHITE_TEAL_PINK = new AppTheme(3,
            R.style.PrimaryTealTheme,
            0,
            R.color.colorTealPrimary,
            R.color.light_background_level_0,
            R.color.colorPinkAccent,
            R.color.color_control_highlight,
            false);
    public static final AppTheme DARK_GREEN = new AppTheme(5,
            R.style.DarkGreenTheme,
            0,
            R.color.darkColorPrimary,
            R.color.dark_background_level_1,
            R.color.colorGreenAccent,
            R.color.color_control_highlight_dark,
            true);
    public static final AppTheme COMPLETELY_WHITE = new AppTheme(6,
            R.style.CompletelyWhiteTheme,
            0,
            android.R.color.white,
            android.R.color.white,
            R.color.colorAccentBlue,
            R.color.color_control_highlight,
            false);
    public static final AppTheme COMPLETELY_BLACK = new AppTheme(7,
            R.style.CompletelyBlackTheme,
            0,
            android.R.color.black,
            android.R.color.black,
            R.color.colorAccentDark,
            R.color.color_control_highlight_dark,
            true);
    public static final AppTheme WHITE_RED = new AppTheme(8,
            R.style.PrimaryRedTheme,
            0,
            R.color.colorRedPrimary,
            R.color.light_background_level_0,
            R.color.colorRedPrimary,
            R.color.color_control_highlight,
            false);
    public static final AppTheme WHITE_ORANGE = new AppTheme(9,
            R.style.PrimaryOrangeTheme,
            0,
            R.color.colorOrangePrimary,
            R.color.light_background_level_0,
            R.color.colorOrangePrimary,
            R.color.color_control_highlight,
            false);
    public static final AppTheme WHITE_PURPLE_PINK = new AppTheme(10,
            R.style.PrimaryPurplePinkTheme,
            0,
            R.color.color_purple_primary,
            R.color.light_background_level_0,
            R.color.colorPinkAccent,
            R.color.color_control_highlight,
            false);
    public static final AppTheme WHITE_BLUE_ORANGE = new AppTheme(11,
            R.style.PrimaryBlueOrangeTheme,
            0,
            R.color.color_blue_primary,
            R.color.light_background_level_0,
            R.color.color_orange_accent,
            R.color.color_control_highlight,
            false);

    @RequiresApi(Build.VERSION_CODES.S)
    public static AppTheme getSystemWhiteTheme() {
        return new AppTheme(-1,
                R.style.SystemLightTheme,
                0,
                android.R.color.system_accent1_500,
                android.R.color.system_accent1_100,
                android.R.color.system_accent3_400,
                R.color.color_control_highlight,
                false);
    }

    @RequiresApi(Build.VERSION_CODES.S)
    public static AppTheme getSystemDarkTheme() {
        return new AppTheme(-2,
                R.style.SystemDarkTheme,
                0,
                android.R.color.system_accent1_500,
                android.R.color.system_accent1_900,
                android.R.color.system_accent3_400,
                R.color.color_control_highlight_dark,
                true);
    }

    @NonNull
    public static AppTheme getTheme(int id) {
        for (AppTheme type : appThemes()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return WHITE_PURPLE_TEAL;
    }

    public static AppTheme[] appThemes() {
        return new AppTheme[] {
                WHITE_PURPLE_TEAL,
                DARK,
                WHITE_INDIGO_GREEN,
                DARK_ORANGE,
                WHITE_TEAL_PINK,
                DARK_GREEN,
                COMPLETELY_WHITE,
                COMPLETELY_BLACK,
                WHITE_RED,
                WHITE_ORANGE,
                WHITE_PURPLE_PINK,
                WHITE_BLUE_ORANGE
        };
    }

    private final int id;
    private final int themeResId;
    private final int descriptionId;
    private final int primaryColorId;
    private final int backgroundColorId;
    private final int accentColorId;
    private final int rippleColorId;
    private final boolean isDark;

    AppTheme(int id,
             int themeResId,
             int descriptionId,
             int primaryColorId,
             int backgroundColorId,
             int accentColorId,
             int rippleColorId,
             boolean isDark) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppTheme appTheme = (AppTheme) o;

        return id == appTheme.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
