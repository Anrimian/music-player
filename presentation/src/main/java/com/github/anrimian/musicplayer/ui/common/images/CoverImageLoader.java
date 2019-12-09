package com.github.anrimian.musicplayer.ui.common.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.utils.image.loader.SimpleImageLoader;

public class CoverImageLoader extends SimpleImageLoader<Long, Composition> {

    private static CoverImageLoader coverImageLoader;

    private CoverImageLoader() {
        super(R.drawable.ic_music_placeholder_simple,
                R.drawable.ic_music_placeholder_simple,
                5,
                2*1024*1024,
                CoverImageLoader::extractImageComposition,
                Composition::getId);
    }

    public static CoverImageLoader getInstance() {
        if (coverImageLoader == null) {
            synchronized (CoverImageLoader.class) {
                if (coverImageLoader == null) {
                    coverImageLoader = new CoverImageLoader();
                }
            }
        }
        return coverImageLoader;
    }

    @Nullable
    private static Bitmap extractImageComposition(Composition composition) {
        String filePath = composition.getFilePath();

        //noinspection ConstantConditions
        if (filePath == null) {
            return null;
        }

        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filePath);
            byte[] imageBytes = mmr.getEmbeddedPicture();
            mmr.release();
            if (imageBytes == null) {
                return null;
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = 300;
            opt.outHeight = 300;
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
    }
}
