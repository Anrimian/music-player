package com.github.anrimian.musicplayer.ui.common.images.glide.loaders;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader;
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage;

import java.nio.ByteBuffer;

public class CompositionModelLoader extends AppModelLoader<CompositionImage, ByteBuffer> {

    private final Context context;
    private final CompositionSourceProvider compositionSourceProvider;

    public CompositionModelLoader(Context context, CompositionSourceProvider compositionSourceProvider) {
        this.context = context;
        this.compositionSourceProvider = compositionSourceProvider;
    }

    @Override
    protected Object getModelKey(CompositionImage compositionImage) {
        return compositionImage;
    }

    @Override
    protected void loadData(CompositionImage compositionImage, @NonNull Priority priority, @NonNull DataFetcher.DataCallback<? super ByteBuffer> callback) {
        callback.onDataReady(extractImageComposition(compositionImage));
    }

    @Nullable
    private ByteBuffer extractImageComposition(CompositionImage composition) {
        long id = composition.getId();

        try {
            byte[] imageBytes = compositionSourceProvider.getCompositionArtworkBinaryData(id)
                    .blockingGet();
            if (imageBytes == null) {
                return null;
            }
            return ByteBuffer.wrap(imageBytes);
//            BitmapFactory.Options opt = new BitmapFactory.Options();
//            opt.outWidth = getCoverSize();
//            opt.outHeight = getCoverSize();
//            opt.inJustDecodeBounds = true;
//            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
//            opt.inSampleSize = ImageUtils.calculateInSampleSize(opt, getCoverSize(), getCoverSize());
//            opt.inJustDecodeBounds = false;
//            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int getCoverSize() {
        return context.getResources().getInteger(R.integer.icon_image_size);
    }
}
