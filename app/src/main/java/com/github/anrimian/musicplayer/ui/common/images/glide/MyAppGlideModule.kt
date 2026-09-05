package com.github.anrimian.musicplayer.ui.common.images.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.executor.GlideExecutor
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.ui.common.images.glide.loaders.AlbumModelLoader
import com.github.anrimian.musicplayer.ui.common.images.glide.loaders.CompositionModelLoader
import com.github.anrimian.musicplayer.ui.common.images.glide.loaders.ExternalCompositionModelLoader
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage
import java.io.InputStream

@GlideModule
class MyAppGlideModule : AppGlideModule() {

    companion object {
        const val IMAGE_CACHE_DIRECTORY = "image_cache"
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.ERROR)

        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))

        val threadTimeout = 1000L
        builder.setSourceExecutor(
            GlideExecutor.newSourceBuilder().setThreadTimeoutMillis(threadTimeout).build()
        )
        builder.setDiskCacheExecutor(
            GlideExecutor.newDiskCacheBuilder().setThreadTimeoutMillis(threadTimeout).build()
        )

        val memoryCacheSizeBytes = 6 * 1024 * 1024L // 6 MB
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes))
        builder.setBitmapPool(LruBitmapPool(memoryCacheSizeBytes))

        val diskCacheSizeBytes = 8 * 1024 * 1024L // 8 MB
        builder.setDiskCache(
            InternalCacheDiskCacheFactory(
                context,
                IMAGE_CACHE_DIRECTORY,
                diskCacheSizeBytes
            )
        )
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        AppModelLoader.addModelLoader(
            registry,
            CompositionImage::class.java,
            InputStream::class.java,
            CompositionModelLoader(context, Components.getAppComponent().storageSourceRepository())
        )
        AppModelLoader.addModelLoader(
            registry,
            UriCompositionImage::class.java,
            InputStream::class.java,
            ExternalCompositionModelLoader(context)
        )
        AppModelLoader.addModelLoader(
            registry,
            Album::class.java,
            InputStream::class.java,
            AlbumModelLoader(Components.getAppComponent().storageAlbumsProvider())
        )
    }
}
