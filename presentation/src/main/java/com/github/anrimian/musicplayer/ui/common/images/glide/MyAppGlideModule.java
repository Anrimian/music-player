package com.github.anrimian.musicplayer.ui.common.images.glide;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.images.glide.album.AlbumCoverLoaderFactory;
import com.github.anrimian.musicplayer.ui.common.images.glide.composition.CompositionCoverLoaderFactory;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setMemoryCache(new LruResourceCache(2 * 1024 * 1024));
    }

    @Override
    public void registerComponents(@NonNull Context context,
                                   @NonNull Glide glide,
                                   @NonNull Registry registry) {
        registry.prepend(Composition.class, Bitmap.class, new CompositionCoverLoaderFactory(context));
        registry.prepend(Album.class, Bitmap.class, new AlbumCoverLoaderFactory(context));
    }

}