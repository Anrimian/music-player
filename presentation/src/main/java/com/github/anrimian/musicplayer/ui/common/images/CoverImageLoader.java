package com.github.anrimian.musicplayer.ui.common.images;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.utils.image.loader.SimpleImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

public class CoverImageLoader {

    private final int defaultPlaceholder = R.drawable.ic_music_placeholder_simple;
    private final int timeoutMillis = 5000;

    private final Context context;
    private final StorageAlbumsProvider storageAlbumsProvider;

    private final SimpleImageLoader<String, ImageMetaData> imageLoader;


    public CoverImageLoader(Context context, StorageAlbumsProvider storageAlbumsProvider) {
        this.context = context;
        this.storageAlbumsProvider = storageAlbumsProvider;

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
        Glide.with(imageView)
                .load(data)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(defaultPlaceholder)
                .timeout(timeoutMillis)
                .into(imageView);

//        imageLoader.displayImage(imageView, new CompositionImage(data.getId(), data.getFilePath()));
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Composition data,
                             @DrawableRes int errorPlaceholder) {
        Glide.with(imageView)
                .load(data)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(errorPlaceholder)
                .timeout(timeoutMillis)
                .into(imageView);

//        imageLoader.displayImage(imageView, new CompositionImage(data.getId(), data.getFilePath()), errorPlaceholder);
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Album album,
                             @DrawableRes int errorPlaceholder) {
        Glide.with(imageView)
                .load(album)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(errorPlaceholder)
                .timeout(timeoutMillis)
                .into(imageView);

//        imageLoader.displayImage(imageView, new AlbumImage(album), errorPlaceholder);
    }

    @Nullable
    public Bitmap getImage(@Nonnull Composition data, long timeoutMillis) {
        //not working properly
        try {
            return Glide.with(context)
                    .asBitmap()
                    .load(data)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .timeout((int) timeoutMillis)
                    .submit()
                    .get();
        } catch (Exception ignored) {
            return null;
        }

//        return imageLoader.getImage(new CompositionImage(data.getId(), data.getFilePath()), timeoutMillis);
    }

    public void loadImage(@Nonnull Composition data, Callback<Bitmap> onCompleted) {
        Glide.with(context)
                .asBitmap()
                .load(data)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .timeout(timeoutMillis)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        onCompleted.call(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        onCompleted.call(null);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
//        imageLoader.loadImage(new CompositionImage(data.getId(), data.getFilePath()), onCompleted);
    }

    public void displayImage(@NonNull RemoteViews widgetView,
                             @IdRes int viewId,
                             AppWidgetManager appWidgetManager,
                             int appWidgetId,
                             long compositionId,
                             String filePath,
                             @NonNull SimpleImageLoader.BitmapTransformer bitmapTransformer,
                             @DrawableRes int placeholder) {
        imageLoader.displayImage(widgetView, viewId, appWidgetManager, appWidgetId, new CompositionImage(compositionId, filePath), bitmapTransformer, placeholder);
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
        String filePath = composition.getFilePath();

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
