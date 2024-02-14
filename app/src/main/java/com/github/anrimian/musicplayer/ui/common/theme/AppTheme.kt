package com.github.anrimian.musicplayer.ui.common.theme

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.anrimian.musicplayer.R

class AppTheme(
    val id: Int,
    val themeResId: Int,
    val primaryColorId: Int,
    val backgroundColorId: Int,
    val accentColorId: Int,
    val rippleColorId: Int,
    val isDark: Boolean
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppTheme

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }


    companion object {
        val WHITE_PURPLE_TEAL = AppTheme(
            0,
            R.style.PrimaryPurpleTheme,
            R.color.color_purple_primary,
            R.color.light_background_level_0,
            R.color.colorAccent,
            R.color.color_control_highlight,
            false
        )
        val DARK = AppTheme(
            1,
            R.style.PrimaryDarkTheme,
            R.color.darkColorPrimary,
            R.color.dark_background_level_1,
            R.color.colorAccentDark,
            R.color.color_control_highlight_dark,
            true
        )
        val WHITE_INDIGO_GREEN = AppTheme(
            2,
            R.style.PrimaryIndigoTheme,
            R.color.color_indigo_primary,
            R.color.light_background_level_0,
            R.color.colorGreenAccent,
            R.color.color_control_highlight,
            false
        )
        val DARK_ORANGE = AppTheme(
            4,
            R.style.DarkOrangeTheme,
            R.color.darkColorPrimary,
            R.color.dark_background_level_1,
            R.color.color_orange_dark_accent,
            R.color.color_control_highlight_dark,
            true
        )
        val WHITE_TEAL_PINK = AppTheme(
            3,
            R.style.PrimaryTealTheme,
            R.color.colorTealPrimary,
            R.color.light_background_level_0,
            R.color.colorPinkAccent,
            R.color.color_control_highlight,
            false
        )
        val DARK_GREEN = AppTheme(
            5,
            R.style.DarkGreenTheme,
            R.color.darkColorPrimary,
            R.color.dark_background_level_1,
            R.color.colorGreenAccent,
            R.color.color_control_highlight_dark,
            true
        )
        val COMPLETELY_WHITE = AppTheme(
            6,
            R.style.CompletelyWhiteTheme,
            android.R.color.white,
            android.R.color.white,
            R.color.colorAccentBlue,
            R.color.color_control_highlight,
            false
        )
        val COMPLETELY_BLACK = AppTheme(
            7,
            R.style.CompletelyBlackTheme,
            android.R.color.black,
            android.R.color.black,
            R.color.colorAccentDark,
            R.color.color_control_highlight_dark,
            true
        )
        val WHITE_RED = AppTheme(
            8,
            R.style.PrimaryRedTheme,
            R.color.colorRedPrimary,
            R.color.light_background_level_0,
            R.color.colorRedPrimary,
            R.color.color_control_highlight,
            false
        )
        val WHITE_ORANGE = AppTheme(
            9,
            R.style.PrimaryOrangeTheme,
            R.color.colorOrangePrimary,
            R.color.light_background_level_0,
            R.color.colorOrangePrimary,
            R.color.color_control_highlight,
            false
        )
        val WHITE_PURPLE_PINK = AppTheme(
            10,
            R.style.PrimaryPurplePinkTheme,
            R.color.color_purple_primary,
            R.color.light_background_level_0,
            R.color.colorPinkAccent,
            R.color.color_control_highlight,
            false
        )
        val WHITE_BLUE_ORANGE = AppTheme(
            11,
            R.style.PrimaryBlueOrangeTheme,
            R.color.color_blue_primary,
            R.color.light_background_level_0,
            R.color.color_orange_accent,
            R.color.color_control_highlight,
            false
        )

        @RequiresApi(Build.VERSION_CODES.S)
        fun getSystemWhiteTheme() = AppTheme(
            -1,
            R.style.SystemLightTheme,
            android.R.color.system_accent1_500,
            android.R.color.system_accent1_100,
            android.R.color.system_accent3_400,
            R.color.color_control_highlight,
            false
        )

        @RequiresApi(Build.VERSION_CODES.S)
        fun getSystemDarkTheme() = AppTheme(
            -2,
            R.style.SystemDarkTheme,
            android.R.color.system_accent1_500,
            android.R.color.system_accent1_900,
            android.R.color.system_accent3_400,
            R.color.color_control_highlight_dark,
            true
        )

        fun getTheme(id: Int): AppTheme {
            for (type in appThemes()) {
                if (type.id == id) {
                    return type
                }
            }
            return WHITE_PURPLE_TEAL
        }

        fun appThemes() = arrayOf(
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
        )
    }

}