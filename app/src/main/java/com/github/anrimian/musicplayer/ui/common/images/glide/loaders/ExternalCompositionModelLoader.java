package com.github.anrimian.musicplayer.ui.common.images.glide.loaders;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader;
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage;
import com.github.anrimian.musicplayer.ui.utils.ImageUtils;

public class ExternalCompositionModelLoader extends AppModelLoader<UriCompositionImage, Bitmap> {

    private final Context context;

    public ExternalCompositionModelLoader(Context context) {
        this.context = context;
    }

    @Override
    protected Object getModelKey(UriCompositionImage uriCompositionImage) {
        return uriCompositionImage;
    }

    @Override
    protected void loadData(UriCompositionImage uriCompositionImage,
                            @NonNull Priority priority,
                            @NonNull DataFetcher.DataCallback<? super Bitmap> callback) {
        try {
            ExternalCompositionSource source = uriCompositionImage.getSource();
            byte[] imageBytes = source.getImageBytes();
            Bitmap bitmap = null;
            if (imageBytes != null) {
                int coverSize = context.getResources().getInteger(R.integer.icon_image_full_size);
                bitmap = ImageUtils.decodeBitmap(imageBytes, coverSize);
            }
            callback.onDataReady(bitmap);
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

}
