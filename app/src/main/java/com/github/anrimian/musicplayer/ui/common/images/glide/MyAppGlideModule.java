package com.github.anrimian.musicplayer.ui.common.images.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.images.glide.loaders.AlbumModelLoader;
import com.github.anrimian.musicplayer.ui.common.images.glide.loaders.CompositionModelLoader;
import com.github.anrimian.musicplayer.ui.common.images.glide.loaders.ExternalCompositionModelLoader;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader;
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage;
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage;

import java.io.InputStream;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    public static final String IMAGE_CACHE_DIRECTORY = "image_cache";

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setLogLevel(Log.ERROR);

        builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565));

        //check how it will affect OOM errors statistic. Profiler shows >2x reducing RAM consumption
        long threadTimeout = 1000;
        builder.setSourceExecutor(GlideExecutor.newSourceBuilder().setThreadTimeoutMillis(threadTimeout).build());
        builder.setDiskCacheExecutor(GlideExecutor.newDiskCacheBuilder().setThreadTimeoutMillis(threadTimeout).build());

        int memoryCacheSizeBytes = 6 * 1024 * 1024;//6 MB
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        builder.setBitmapPool(new LruBitmapPool(memoryCacheSizeBytes));

        int diskCacheSizeBytes = 8 * 1024 * 1024; //8 MB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, IMAGE_CACHE_DIRECTORY, diskCacheSizeBytes));
    }

    @Override
    public void registerComponents(@NonNull Context context,
                                   @NonNull Glide glide,
                                   @NonNull Registry registry) {
        AppModelLoader.addModelLoader(registry, CompositionImage.class, Bitmap.class, new CompositionModelLoader(context, Components.getAppComponent().storageSourceRepository()));
        AppModelLoader.addModelLoader(registry, UriCompositionImage.class, Bitmap.class, new ExternalCompositionModelLoader(context));
        AppModelLoader.addModelLoader(registry, Album.class, InputStream.class, new AlbumModelLoader(Components.getAppComponent().storageAlbumsProvider()));
    }

}