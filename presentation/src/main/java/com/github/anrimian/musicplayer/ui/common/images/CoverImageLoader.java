package com.github.anrimian.musicplayer.ui.common.images;

import android.appwidget.AppWidgetManager;
import android.content.Context;
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
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.utils.image.loader.SimpleImageLoader;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

public class CoverImageLoader {

    private final Context context;
    private final StorageAlbumsProvider storageAlbumsProvider;
    private final CompositionSourceProvider compositionSourceProvider;

    private final SimpleImageLoader<String, ImageMetaData> imageLoader;

    public CoverImageLoader(Context context,
                            StorageAlbumsProvider storageAlbumsProvider,
                            CompositionSourceProvider compositionSourceProvider) {
        this.context = context;
        this.storageAlbumsProvider = storageAlbumsProvider;
        this.compositionSourceProvider = compositionSourceProvider;

        imageLoader = new SimpleImageLoader<>(
                R.drawable.ic_music_placeholder_simple,
                R.drawable.ic_music_placeholder_simple,
                5000,
                2 * 1024 * 1024,
                ImageMetaData::getKey,
                this::getImage
        );
    }

    public void displayImage(@NonNull ImageView imageView, @NonNull Composition data) {
        imageLoader.displayImage(imageView, new CompositionImage(data.getId()));
    }

    public void displayImage(@NonNull ImageView imageView, @NonNull Album album) {
        imageLoader.displayImage(imageView, new AlbumImage(album));
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Composition data,
                             @DrawableRes int errorPlaceholder) {
        imageLoader.displayImage(imageView, new CompositionImage(data.getId()), errorPlaceholder);
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Album album,
                             @DrawableRes int errorPlaceholder) {
        imageLoader.displayImage(imageView, new AlbumImage(album), errorPlaceholder);
    }

    @Nullable
    public Bitmap getImage(@Nonnull Composition data, long timeoutMillis) {
        return imageLoader.getImage(new CompositionImage(data.getId()), timeoutMillis);
    }

    public void loadImage(@Nonnull Composition data, Callback<Bitmap> onCompleted) {
        imageLoader.loadImage(new CompositionImage(data.getId()), onCompleted);
    }

    public void displayImage(@NonNull RemoteViews widgetView,
                             @IdRes int viewId,
                             AppWidgetManager appWidgetManager,
                             int appWidgetId,
                             long compositionId,
                             @NonNull SimpleImageLoader.BitmapTransformer bitmapTransformer,
                             @DrawableRes int placeholder) {
        imageLoader.displayImage(widgetView, viewId, appWidgetManager, appWidgetId, new CompositionImage(compositionId), bitmapTransformer, placeholder);
    }

    private Bitmap getImage(ImageMetaData metaData) {
        if (metaData instanceof CompositionImage) {
            return extractImageComposition(((CompositionImage) metaData));
        }
        if (metaData instanceof AlbumImage) {
            return extractAlbumCover(((AlbumImage) metaData).getAlbum());
        }
        throw new IllegalStateException("unexpected image metaData: " + metaData);
    }

    private Bitmap extractAlbumCover(Album album) {
        try (InputStream in = storageAlbumsProvider.getAlbumCoverStream(album.getName())) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = getCoverSize();
            opt.outHeight = getCoverSize();
            return BitmapFactory.decodeStream(in, null, opt);
        } catch (IOException ignores) {
            return null;
        }
    }

    @Nullable
    private Bitmap extractImageComposition(CompositionImage composition) {
        long id = composition.getId();

        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(compositionSourceProvider.getCompositionFileDescriptor(id));
            byte[] imageBytes = mmr.getEmbeddedPicture();
            mmr.release();
            if (imageBytes == null) {
                return null;
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = getCoverSize();
            opt.outHeight = getCoverSize();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
    }

    private int getCoverSize() {
        return context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_size);
    }
}
