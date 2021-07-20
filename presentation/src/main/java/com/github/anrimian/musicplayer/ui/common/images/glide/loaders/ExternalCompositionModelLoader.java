package com.github.anrimian.musicplayer.ui.common.images.glide.loaders;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader;
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage;

import java.nio.ByteBuffer;

public class ExternalCompositionModelLoader extends AppModelLoader<UriCompositionImage, ByteBuffer> {

    @Override
    protected Object getModelKey(UriCompositionImage uriCompositionImage) {
        return uriCompositionImage;
    }

    @Override
    protected void loadData(UriCompositionImage uriCompositionImage,
                            @NonNull Priority priority,
                            @NonNull DataFetcher.DataCallback<? super ByteBuffer> callback) {
        try {
            UriCompositionSource source = uriCompositionImage.getSource();
            byte[] imageBytes = source.getImageBytes();
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
