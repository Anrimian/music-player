package com.github.anrimian.musicplayer.ui.common.images.glide.composition;

import android.content.Context;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

public class CompositionCoverLoaderFactory implements ModelLoaderFactory<CompositionImage, ByteBuffer> {

    private final Context context;
    private final CompositionSourceProvider compositionSourceProvider;

    public CompositionCoverLoaderFactory(Context context, CompositionSourceProvider compositionSourceProvider) {
        this.context = context;
        this.compositionSourceProvider = compositionSourceProvider;
    }

    @Nonnull
    @Override
    public ModelLoader<CompositionImage, ByteBuffer> build(@Nonnull MultiModelLoaderFactory unused) {
        return new CompositionCoverLoader(context, compositionSourceProvider);
    }

    @Override
    public void teardown() {
        // Do nothing.
    }

}