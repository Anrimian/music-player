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
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.domain.utils.java.Processor;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.CustomAppWidgetTarget;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;
import com.github.anrimian.musicplayer.ui.utils.image.loader.SimpleImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

public class CoverImageLoader {

    private static final int DEFAULT_PLACEHOLDER = R.drawable.ic_music_placeholder_simple;
    private static final int TIMEOUT_MILLIS = 5000;
    private static final int NOTIFICATION_IMAGE_TIMEOUT_MILLIS = 250;

    private final Context context;
    private final StorageAlbumsProvider storageAlbumsProvider;
    private final CompositionSourceProvider compositionSourceProvider;
    private final ThemeController themeController;

    private Bitmap defaultNotificationBitmap;

    private final SimpleImageLoader<String, ImageMetaData> imageLoader;

    public CoverImageLoader(Context context,
                            StorageAlbumsProvider storageAlbumsProvider,
                            CompositionSourceProvider compositionSourceProvider,
                            ThemeController themeController) {
        this.context = context;
        this.storageAlbumsProvider = storageAlbumsProvider;
        this.compositionSourceProvider = compositionSourceProvider;
        this.themeController = themeController;

        imageLoader = new SimpleImageLoader<>(
                R.drawable.ic_music_placeholder_simple,
                R.drawable.ic_music_placeholder_simple,
                5000,
                2 * 1024 * 1024,
                ImageMetaData::getKey,
                this::getImage
        );
    }

    //blinking
    public void displayImage(@NonNull ImageView imageView, @NonNull Composition data) {
        Glide.with(imageView)
                .load(new CompositionImage(data.getId()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(DEFAULT_PLACEHOLDER)
                .error(DEFAULT_PLACEHOLDER)
                .timeout(TIMEOUT_MILLIS)
                .into(imageView);

//        imageLoader.displayImage(imageView, new CompositionImage(data.getId(), data.getFilePath()));
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Composition data,
                             @DrawableRes int errorPlaceholder) {
        Glide.with(imageView)
                .load(new CompositionImage(data.getId()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(errorPlaceholder)
                .timeout(TIMEOUT_MILLIS)
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
                .error(errorPlaceholder)
                .timeout(TIMEOUT_MILLIS)
                .into(imageView);

//        imageLoader.displayImage(imageView, new AlbumImage(album), errorPlaceholder);
    }

    public Runnable loadNotificationImage(@Nonnull Composition data,
                                          Callback<Bitmap> onCompleted) {
        CustomTarget<Bitmap> target = simpleTarget(bitmap -> {
            if (bitmap == null) {
                bitmap = getDefaultNotificationBitmap();
            }
            onCompleted.call(bitmap);
        });

        Glide.with(context)
                .asBitmap()
                .load(new CompositionImage(data.getId()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .timeout(NOTIFICATION_IMAGE_TIMEOUT_MILLIS)
                .into(target);

        return () -> Glide.with(context).clear(target);
//        imageLoader.loadImage(new CompositionImage(data.getId(), data.getFilePath()), onCompleted);
    }

    public Bitmap getDefaultNotificationBitmap() {
        if (defaultNotificationBitmap == null) {
            defaultNotificationBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
        }
        int color = themeController.getPrimaryThemeColor();
        defaultNotificationBitmap.eraseColor(color);
        return defaultNotificationBitmap;
    }

    public void loadImage(@Nonnull Composition data, Callback<Bitmap> onCompleted) {
        Glide.with(context)
                .asBitmap()
                .load(new CompositionImage(data.getId()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .timeout(TIMEOUT_MILLIS)
                .into(simpleTarget(onCompleted));
//        imageLoader.loadImage(new CompositionImage(data.getId(), data.getFilePath()), onCompleted);
    }

    public void displayImage(@NonNull RemoteViews widgetView,
                             @IdRes int viewId,
                             int appWidgetId,
                             long compositionId,
                             @DrawableRes int placeholder) {
        CustomAppWidgetTarget widgetTarget = new CustomAppWidgetTarget(context,
                viewId,
                widgetView,
                placeholder,
                appWidgetId);

        Glide.with(context)
                .asBitmap()
                .load(new CompositionImage(compositionId))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(new CircleCrop())
                .timeout(TIMEOUT_MILLIS)
                .into(widgetTarget);
//        imageLoader.displayImage(widgetView, viewId, appWidgetManager, appWidgetId, new CompositionImage(compositionId), bitmapTransformer, placeholder);
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

    private <T> CustomTarget<T> simpleTarget(Callback<T> callback) {
        return new CustomTarget<T>() {
            @Override
            public void onResourceReady(@NonNull T resource, @Nullable Transition<? super T> transition) {
                callback.call(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                callback.call(null);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {}
        };
    }
}
