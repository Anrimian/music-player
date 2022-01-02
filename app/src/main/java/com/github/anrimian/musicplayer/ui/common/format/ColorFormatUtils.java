package com.github.anrimian.musicplayer.ui.common.format;

import android.content.Context;

import androidx.annotation.ColorInt;

import com.github.anrimian.musicplayer.R;

import static androidx.core.graphics.ColorUtils.setAlphaComponent;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public class ColorFormatUtils {

    @ColorInt
    public static int getPlayingCompositionColor(Context context, int alpha) {
        return setAlphaComponent(getColorFromAttr(context, R.attr.colorPrimaryVariant), alpha);
    }

    @ColorInt
    public static int getItemDragColor(Context context, int alpha) {
        return setAlphaComponent(getColorFromAttr(context, android.R.attr.colorControlNormal), alpha);
    }
}
