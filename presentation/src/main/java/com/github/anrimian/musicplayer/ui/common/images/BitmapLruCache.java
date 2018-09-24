package com.github.anrimian.musicplayer.ui.common.images;

import android.graphics.Bitmap;
import android.support.v4.graphics.BitmapCompat;
import android.util.LruCache;

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
