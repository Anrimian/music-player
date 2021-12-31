package com.github.anrimian.musicplayer.ui.common.images.glide.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader;
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage;
import com.github.anrimian.musicplayer.ui.utils.ImageUtils;

public class CompositionModelLoader extends AppModelLoader<CompositionImage, Bitmap> {

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
    protected void loadData(CompositionImage compositionImage,
                            @NonNull Priority priority,
                            @NonNull DataFetcher.DataCallback<? super Bitmap> callback) {
        MediaMetadataRetriever mmr = null;
        try {
            long id = compositionImage.getId();
            byte[] imageBytes = compositionSourceProvider.getCompositionArtworkBinaryData(id)
                    .blockingGet();

            if (imageBytes == null) {
                mmr = new MediaMetadataRetriever();
                mmr.setDataSource(compositionSourceProvider.getCompositionFileDescriptor(id));
                imageBytes = mmr.getEmbeddedPicture();
            }

            Bitmap bitmap = null;
            if (imageBytes != null) {
                int coverSize = context.getResources().getInteger(R.integer.icon_image_full_size);
                bitmap = ImageUtils.decodeBitmap(imageBytes, coverSize);
            }
            callback.onDataReady(bitmap);
        } catch (Exception e) {
            callback.onLoadFailed(e);
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
    }

}
