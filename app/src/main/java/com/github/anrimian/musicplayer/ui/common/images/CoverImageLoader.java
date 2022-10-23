package com.github.anrimian.musicplayer.ui.common.images;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.domain.utils.functions.Optional;
import com.github.anrimian.musicplayer.ui.common.images.glide.GlideApp;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.CustomAppWidgetTarget;
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage;
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;

import java.io.File;
import java.util.Date;

import javax.annotation.Nonnull;

import io.reactivex.rxjava3.core.Single;

public class CoverImageLoader {

    private static final int DEFAULT_PLACEHOLDER = R.drawable.ic_music_placeholder_simple;
    private static final int TIMEOUT_MILLIS = 5000;
    private static final int NOTIFICATION_IMAGE_TIMEOUT_MILLIS = 500;

    private final Context context;
    private final ThemeController themeController;

    private Bitmap defaultNotificationBitmap;

    public CoverImageLoader(Context context, ThemeController themeController) {
        this.context = context;
        this.themeController = themeController;
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Composition data,
                             Callback<Boolean> listener) {
        if (!isValidContextForGlide(imageView)) {
            return;
        }

        GlideApp.with(imageView)
                .asBitmap()
                .load(toImageRequest(data))
                .override(getCoverSize())
                .placeholder(DEFAULT_PLACEHOLDER)
                .error(DEFAULT_PLACEHOLDER)
                .timeout(TIMEOUT_MILLIS)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Bitmap> target,
                                                boolean isFirstResource) {
                        listener.call(false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource,
                                                   Object model,
                                                   Target<Bitmap> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        listener.call(true);
                        return false;
                    }
                })
                .into(imageView);
    }

    public void clearImage(@NonNull ImageView imageView) {
        if (!isValidContextForGlide(imageView)) {
            return;
        }
        GlideApp.with(imageView).clear(imageView);
    }

    public void displayImageInReusableTarget(@NonNull ImageView imageView,
                                             @NonNull ExternalCompositionSource data,
                                             @DrawableRes int errorPlaceholder) {
        displayImageInReusableTarget(imageView, new UriCompositionImage(data), null, errorPlaceholder);
    }

    public void displayImageInReusableTarget(@NonNull ImageView imageView,
                                             @NonNull FullComposition data,
                                             @DrawableRes int errorPlaceholder) {
        displayImageInReusableTarget(imageView,
                new CompositionImage(data.getId(), data.getDateModified(), data.getSize(), data.getStorageId() != null),
                null,
                errorPlaceholder);
    }

    public void displayImageInReusableTarget(@NonNull ImageView imageView,
                                             @NonNull Composition data,
                                             @Nullable Composition oldData,
                                             @DrawableRes int errorPlaceholder) {
        CompositionImage oldComposition = null;
        if (oldData != null) {
            oldComposition = toImageRequest(data);
        }
        displayImageInReusableTarget(imageView,
                toImageRequest(data),
                oldComposition,
                errorPlaceholder);
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Album album,
                             @DrawableRes int errorPlaceholder) {
        if (!isValidContextForGlide(imageView)) {
            return;
        }

        GlideApp.with(imageView)
                .asBitmap()
                .load(album)
                .override(getCoverSize())
                .placeholder(errorPlaceholder)
                .error(errorPlaceholder)
                .timeout(TIMEOUT_MILLIS)
                .into(imageView);
    }

    public Runnable loadNotificationImage(@Nonnull Composition data,
                                          Callback<Bitmap> onCompleted) {
        return loadNotificationImage(
                toImageRequest(data),
                onCompleted);
    }

    public Runnable loadNotificationImage(@Nonnull ExternalCompositionSource source,
                                          Callback<Bitmap> onCompleted) {
        return loadNotificationImage(
                new UriCompositionImage(source),
                onCompleted);
    }

    public Bitmap getDefaultNotificationBitmap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (defaultNotificationBitmap == null) {
                defaultNotificationBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
            }
            int color = themeController.getPrimaryThemeColor();
            defaultNotificationBitmap.eraseColor(color);
        } else {
            if (defaultNotificationBitmap == null) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                defaultNotificationBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_music_box, opt);
            }
        }
        return defaultNotificationBitmap;
    }

    public void loadImage(@Nonnull ExternalCompositionSource data, Callback<Bitmap> onCompleted) {
        loadImage(new UriCompositionImage(data), onCompleted);
    }

    public void loadImage(@Nonnull Composition data, Callback<Bitmap> onCompleted) {
        loadImage(toImageRequest(data), onCompleted);
    }

    public Single<Optional<Uri>> loadImageUri(@Nonnull Composition data) {
        return Single.create(emitter ->
                loadImageUri(data, uri -> emitter.onSuccess(new Optional<>(uri)))
        );
    }

    public void loadImageUri(@Nonnull Composition data, Callback<Uri> onCompleted) {
        CustomTarget<File> target = simpleTarget(file -> {
            if (file == null) {
                onCompleted.call(null);
                return;
            }
            Uri uri = new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(context.getString(R.string.covers_file_provider_authorities))
                    .path(file.getPath())
                    .build();
            onCompleted.call(uri);
        });
        CompositionImage imageData = toImageRequest(data);
        loadImage(imageData, bitmap ->
                GlideApp.with(context)
                        .download(imageData)
                        .onlyRetrieveFromCache(true)
                        .timeout(TIMEOUT_MILLIS)
                        .into(target)
        );
    }

    public void displayImage(@NonNull RemoteViews widgetView,
                             @IdRes int viewId,
                             int appWidgetId,
                             long compositionId,
                             long compositionUpdateTime,
                             long compositionSize,
                             boolean isFileExists,
                             @DrawableRes int placeholder) {
        CustomAppWidgetTarget widgetTarget = new CustomAppWidgetTarget(context,
                viewId,
                widgetView,
                placeholder,
                appWidgetId);

        GlideApp.with(context)
                .asBitmap()
                .load(new CompositionImage(
                        compositionId,
                        new Date(compositionUpdateTime),
                        compositionSize,
                        isFileExists)
                ).override(getCoverSize())
                .downsample(DownsampleStrategy.AT_MOST)
                .transform(new CircleCrop())
                .timeout(TIMEOUT_MILLIS)
                .into(widgetTarget);
    }

    private void displayImageInReusableTarget(@NonNull ImageView imageView,
                                              @NonNull Object data,
                                              @Nullable Object oldData,
                                              @DrawableRes int errorPlaceholder) {
        if (!isValidContextForGlide(imageView)) {
            return;
        }

        //here replacement with error placeholder flickers, don't know how to solve it
        GlideApp.with(imageView)
                .asBitmap()
                .load(data)
                .override(getCoverSize())
                .thumbnail(GlideApp.with(imageView)
                        .asBitmap()
                        .load(oldData)
                        .override(getCoverSize())
                        .timeout(TIMEOUT_MILLIS))
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Bitmap> target,
                                                boolean isFirstResource) {
                        imageView.setImageResource(errorPlaceholder);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource,
                                                   Object model,
                                                   Target<Bitmap> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        return false;
                    }
                })
                .error(errorPlaceholder)
                .timeout(TIMEOUT_MILLIS)
                .into(imageView);
    }

    private Runnable loadNotificationImage(Object compositionImage,
                                           Callback<Bitmap> onCompleted) {
        CustomTarget<Bitmap> target = simpleTarget(bitmap -> {
            if (bitmap != null) {
                //possible fix for RemoteServiceException crash
                //https://stackoverflow.com/questions/54948936/bad-notification-the-given-region-must-intersect-with-the-bitmaps-dimensions
                bitmap = bitmap.copy(Bitmap.Config.RGB_565, false);
            }
            onCompleted.call(bitmap);
        });

        GlideApp.with(context)
                .asBitmap()
                .load(compositionImage)
                .override(getCoverSize())
                .timeout(NOTIFICATION_IMAGE_TIMEOUT_MILLIS)
                .into(target);

        return () -> GlideApp.with(context).clear(target);
    }

    private void loadImage(@Nonnull Object data, Callback<Bitmap> onCompleted) {
        GlideApp.with(context)
                .asBitmap()
                .load(data)
                .override(getCoverSize())
                .timeout(TIMEOUT_MILLIS)
                .into(simpleTarget(onCompleted));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isValidContextForGlide(ImageView imageView) {
        return isValidContextForGlide(imageView.getContext());
    }

    private static boolean isValidContextForGlide(Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }

    private int getCoverSize() {
        return context.getResources().getInteger(R.integer.icon_image_size);
    }

    private <T> CustomTarget<T> simpleTarget(Callback<T> callback) {
        return new CustomTarget<>() {
            @Override
            public void onResourceReady(@NonNull T resource, @Nullable Transition<? super T> transition) {
                callback.call(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                callback.call(null);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                callback.call(null);
            }
        };
    }

    private CompositionImage toImageRequest(Composition composition) {
        return new CompositionImage(
                composition.getId(),
                composition.getDateModified(),
                composition.getSize(),
                composition.isFileExists()
        );
    }

}
