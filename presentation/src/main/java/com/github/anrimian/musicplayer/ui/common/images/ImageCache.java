package com.github.anrimian.musicplayer.ui.common.images;

import android.graphics.Bitmap;

import javax.annotation.Nullable;

public class ImageCache {

    private static final int MAX_CACHE_SIZE = 1024*1024;

    private final BitmapLruCache<Long> bitmapCache = new BitmapLruCache<>(MAX_CACHE_SIZE);

    private static ImageCache imageCache;

    private ImageCache() {
    }

    public static ImageCache getInstance() {
        if (imageCache == null) {
            synchronized (ImageCache.class) {
                if (imageCache == null) {
                    imageCache = new ImageCache();
                }
            }
        }
        return imageCache;
    }

    @Nullable
    public Bitmap getBitmap(long key) {
        return bitmapCache.get(key);
    }

    public void putBitmap(long key, Bitmap bitmap) {
        bitmapCache.put(key, bitmap);
    }
}
