package com.github.anrimian.musicplayer.ui.common.format;

import android.graphics.Bitmap;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;

public class ImageFormatUtils {

    public static Bitmap getNotificationImage(Composition composition) {
        Bitmap bitmap = CoverImageLoader.getInstance().getImage(composition);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);

            int color = Components.getAppComponent().themeController().getPrimaryThemeColor();
            bitmap.eraseColor(color);
        }
        return bitmap;
    }
}
