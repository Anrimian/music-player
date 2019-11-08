package com.github.anrimian.musicplayer.ui.utils.image.loader;

import android.graphics.Bitmap;

import javax.annotation.Nullable;

class ImageCache<T> {

    private final BitmapLruCache<T> bitmapCache;

    ImageCache(int maxCacheSize) {
        bitmapCache = new BitmapLruCache<>(maxCacheSize);
    }

    @Nullable
    Bitmap getBitmap(T key) {
        return bitmapCache.get(key);
    }

    void putBitmap(T key, Bitmap bitmap) {
        bitmapCache.put(key, bitmap);
    }
}
