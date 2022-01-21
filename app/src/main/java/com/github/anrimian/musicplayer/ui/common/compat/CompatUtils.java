package com.github.anrimian.musicplayer.ui.common.compat;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.github.anrimian.musicplayer.R;
import com.google.android.material.slider.Slider;

public class CompatUtils {

    public static void setOutlineButtonStyle(TextView button) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Context context = button.getContext();
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_enabled},
                    new int[]{}
            };

            int[] colors = new int[]{
                    getColorFromAttr(context, R.attr.disabledColor),
                    getColorFromAttr(context, R.attr.colorAccent)
            };

            ColorStateList textColorList = new ColorStateList(states, colors);
            button.setTextColor(textColorList);
            button.setBackgroundTintList(textColorList);
        }
    }

    public static void setOutlineTextButtonStyle(TextView button) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Context context = button.getContext();
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_enabled},
                    new int[]{}
            };

            int[] colors = new int[]{
                    getColorFromAttr(context, R.attr.disabledColor),
                    getColorFromAttr(context, android.R.attr.textColorSecondary)
            };

            ColorStateList textColorList = new ColorStateList(states, colors);
            button.setBackgroundTintList(textColorList);
        }
    }

    public static void setColorTextPrimaryColor(TextView button) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Context context = button.getContext();
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_enabled},
                    new int[]{}
            };

            int[] colors = new int[]{
                    getColorFromAttr(context, R.attr.disabledColor),
                    getColorFromAttr(context, android.R.attr.textColorPrimary)
            };

            ColorStateList textColorList = new ColorStateList(states, colors);
            button.setTextColor(textColorList);
        }
    }

    public static void setSliderStyle(Slider rangeSlider) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Context context = rangeSlider.getContext();
            int colorAccent = getColorFromAttr(context, R.attr.colorAccent);
            ColorStateList colorStateListAccent = ColorStateList.valueOf(colorAccent);
            rangeSlider.setThumbTintList(colorStateListAccent);
            rangeSlider.setTrackActiveTintList(colorStateListAccent);
            int inactiveTrackColor = ColorUtils.setAlphaComponent(colorAccent, 61);
            rangeSlider.setTrackInactiveTintList(ColorStateList.valueOf(inactiveTrackColor));
        }
    }
}
