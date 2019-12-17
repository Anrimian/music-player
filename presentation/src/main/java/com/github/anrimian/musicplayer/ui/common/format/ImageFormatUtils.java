package com.github.anrimian.musicplayer.ui.common.format;

import android.graphics.Bitmap;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class ImageFormatUtils {

    private static Bitmap defaultNotificationBitmap;

    public static Bitmap getNotificationImage(Composition composition) {
        Bitmap bitmap = Components.getAppComponent().imageLoader().getImage(composition);
        if (bitmap == null) {
            bitmap = getDefaultNotificationBitmap();
            int color = Components.getAppComponent().themeController().getPrimaryThemeColor();
            bitmap.eraseColor(color);
        }
        return bitmap;
    }

    private synchronized static Bitmap getDefaultNotificationBitmap() {
        if (defaultNotificationBitmap == null) {
            defaultNotificationBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
        }
        return defaultNotificationBitmap;
    }
}
