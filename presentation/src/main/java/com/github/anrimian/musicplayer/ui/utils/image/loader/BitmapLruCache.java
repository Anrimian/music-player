package com.github.anrimian.musicplayer.ui.utils.image.loader;

import android.graphics.Bitmap;
import android.util.LruCache;

import androidx.core.graphics.BitmapCompat;

public class BitmapLruCache<K> extends LruCache<K, Bitmap> {

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(K key, Bitmap value) {
        return BitmapCompat.getAllocationByteCount(value);
    }
}
