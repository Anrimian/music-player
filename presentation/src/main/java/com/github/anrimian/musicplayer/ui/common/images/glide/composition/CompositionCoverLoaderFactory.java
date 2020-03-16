package com.github.anrimian.musicplayer.ui.common.images.glide.composition;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import javax.annotation.Nonnull;

public class CompositionCoverLoaderFactory implements ModelLoaderFactory<Composition, Bitmap> {

    private final Context context;

    public CompositionCoverLoaderFactory(Context context) {
        this.context = context;
    }

    @Nonnull
    @Override
    public ModelLoader<Composition, Bitmap> build(@Nonnull MultiModelLoaderFactory unused) {
        return new CompositionCoverLoader(context);
    }

    @Override
    public void teardown() {
        // Do nothing.
    }

}