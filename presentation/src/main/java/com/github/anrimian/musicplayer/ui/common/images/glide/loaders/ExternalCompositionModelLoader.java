package com.github.anrimian.musicplayer.ui.common.images.glide.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
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
        callback.onDataReady(extractImageComposition(uriCompositionImage.getSource()));
    }

    @Nullable
    private Bitmap extractImageComposition(UriCompositionSource uriCompositionSource) {
        try {
            byte[] imageBytes = uriCompositionSource.getImageBytes();
            if (imageBytes == null) {
                return null;
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = getCoverSize();
            opt.outHeight = getCoverSize();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
            opt.inSampleSize = ImageUtils.calculateInSampleSize(opt, getCoverSize(), getCoverSize());
            opt.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int getCoverSize() {
        return context.getResources().getInteger(R.integer.icon_image_size);
    }
}
