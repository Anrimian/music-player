package com.github.anrimian.musicplayer.ui.common.images.glide.loaders;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader;
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage;

import java.nio.ByteBuffer;

public class CompositionModelLoader extends AppModelLoader<CompositionImage, ByteBuffer> {

    private final CompositionSourceProvider compositionSourceProvider;

    public CompositionModelLoader(CompositionSourceProvider compositionSourceProvider) {
        this.compositionSourceProvider = compositionSourceProvider;
    }

    @Override
    protected Object getModelKey(CompositionImage compositionImage) {
        return compositionImage;
    }

    @Override
    protected void loadData(CompositionImage compositionImage,
                            @NonNull Priority priority,
                            @NonNull DataFetcher.DataCallback<? super ByteBuffer> callback) {
        try {
            long id = compositionImage.getId();
            byte[] imageBytes = compositionSourceProvider.getCompositionArtworkBinaryData(id)
                    .blockingGet();
            ByteBuffer result = null;
            if (imageBytes != null) {
                result = ByteBuffer.wrap(imageBytes);
            }
            callback.onDataReady(result);
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

}
