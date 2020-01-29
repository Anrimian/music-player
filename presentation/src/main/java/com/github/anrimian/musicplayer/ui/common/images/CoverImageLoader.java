package com.github.anrimian.musicplayer.ui.common.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.utils.image.loader.SimpleImageLoader;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

public class CoverImageLoader extends SimpleImageLoader<String, ImageMetaData> {

    private static final int COVER_SIZE = 300;

    private final StorageAlbumsProvider storageAlbumsProvider;

    public CoverImageLoader(StorageAlbumsProvider storageAlbumsProvider) {
        super(R.drawable.ic_music_placeholder_simple,
                R.drawable.ic_music_placeholder_simple,
                5000,
                2*1024*1024,
                ImageMetaData::getKey);

        this.storageAlbumsProvider = storageAlbumsProvider;
    }

    @Override
    protected ImageFetcher<ImageMetaData> getImageFetcher() {
        return this::getImage;
    }

    public void displayImage(@NonNull ImageView imageView, @NonNull Composition composition) {
        displayImage(imageView, new CompositionImage(composition));
    }

    public void displayImage(@NonNull ImageView imageView, @NonNull Album album) {
        displayImage(imageView, new AlbumImage(album));
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Composition data,
                             @DrawableRes int errorPlaceholder) {
        displayImage(imageView, new CompositionImage(data), errorPlaceholder);
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Album album,
                             @DrawableRes int errorPlaceholder) {
        displayImage(imageView, new AlbumImage(album), errorPlaceholder);
    }

    @Nullable
    public Bitmap getImage(@Nonnull Composition data, long timeoutMillis) {
        return getImage(new CompositionImage(data), timeoutMillis);
    }

    public void loadImage(@Nonnull Composition data, Callback<Bitmap> onCompleted) {
        loadImage(new CompositionImage(data), onCompleted);
    }

    public void displayImage(@NonNull RemoteViews widgetView,
                             @IdRes int viewId,
                             @NonNull Composition data,
                             @NonNull BitmapTransformer bitmapTransformer,
                             @DrawableRes int placeholder) {
        displayImage(widgetView, viewId, new CompositionImage(data), bitmapTransformer, placeholder);
    }

    private Bitmap getImage(ImageMetaData metaData) {
        if (metaData instanceof CompositionImage) {
            return extractImageComposition(((CompositionImage) metaData).getComposition());
        }
        if (metaData instanceof AlbumImage) {
            return extractAlbumCover(((AlbumImage) metaData).getAlbum());
        }
        throw new IllegalStateException("unexpected image metaData: " + metaData);
    }

    private Bitmap extractAlbumCover(Album album) {
        try (InputStream in = storageAlbumsProvider.getAlbumCoverStream(album.getName())) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = COVER_SIZE;
            opt.outHeight = COVER_SIZE;
            return BitmapFactory.decodeStream(in, null, opt);
        } catch (IOException ignores) {
            return null;
        }
    }

    @Nullable
    //TODO get from media store by album id instead
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
            opt.outWidth = COVER_SIZE;
            opt.outHeight = COVER_SIZE;
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
