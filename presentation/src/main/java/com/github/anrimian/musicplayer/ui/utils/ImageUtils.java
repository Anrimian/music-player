package com.github.anrimian.musicplayer.ui.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;

public class ImageUtils {

    public static Bitmap toCircleBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Path path = new Path();
        path.addCircle(
                (float)(width / 2),
                (float)(height / 2),
                (float) Math.min(width, (height / 2)),
                Path.Direction.CCW);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

}
