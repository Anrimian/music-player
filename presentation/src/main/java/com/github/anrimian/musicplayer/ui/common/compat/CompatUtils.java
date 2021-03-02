package com.github.anrimian.musicplayer.ui.common.compat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.github.anrimian.musicplayer.R;
import com.google.android.material.slider.Slider;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public class CompatUtils {

    public static void setMainButtonStyle(ImageView imageView) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Context context = imageView.getContext();
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_enabled},
                    new int[]{android.R.attr.state_selected},
                    new int[]{}
            };

            int[] colors = new int[]{
                    ContextCompat.getColor(context, R.color.disabled_color),
                    getColorFromAttr(context, R.attr.colorAccent),
                    getColorFromAttr(context, R.attr.buttonColor)
            };

            ColorStateList tintColorList = new ColorStateList(states, colors);
            imageView.setImageTintList(tintColorList);
        }
    }

    public static void setSecondaryButtonStyle(ImageView imageView) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Context context = imageView.getContext();
            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_enabled},
                    new int[]{android.R.attr.state_selected},
                    new int[]{}
            };

            int[] colors = new int[]{
                    getColorFromAttr(context, R.attr.colorControlNormal),
                    getColorFromAttr(context, R.attr.colorAccent),
                    getColorFromAttr(context, R.attr.secondaryButtonColor)
            };

            ColorStateList tintColorList = new ColorStateList(states, colors);
            imageView.setImageTintList(tintColorList);
        }
    }

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
