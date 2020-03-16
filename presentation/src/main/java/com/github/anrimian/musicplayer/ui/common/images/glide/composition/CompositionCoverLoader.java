package com.github.anrimian.musicplayer.ui.common.images.glide.composition;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public final class CompositionCoverLoader implements ModelLoader<Composition, Bitmap> {

    private final Context context;

    CompositionCoverLoader(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader.LoadData<Bitmap> buildLoadData(Composition model,
                                                      int width,
                                                      int height,
                                                      @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model.getId()), new CompositionCoverFetcher(model, context));
    }

    @Override
    public boolean handles(@NonNull Composition model) {
        return true;
    }

}