package com.github.anrimian.musicplayer.ui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth,
                                            int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static byte[] downscaleImageBytes(byte[] imageBytes, int requestedSize) throws IOException {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.outWidth = requestedSize;
        opt.outHeight = requestedSize;
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
        opt.inSampleSize = ImageUtils.calculateInSampleSize(opt, requestedSize, requestedSize);
        opt.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] compressedImageBytes = bos.toByteArray();
            bitmap.recycle();
            return compressedImageBytes;
        }
    }

}
