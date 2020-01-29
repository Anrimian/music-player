package com.github.anrimian.musicplayer.ui.utils.image.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;

import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public abstract class SimpleImageLoader<K, T> {

    @DrawableRes
    private final int loadingPlaceholderId;

    @DrawableRes
    private final int errorPlaceholder;

    private final int timeoutSeconds;

    private final ImageFetcher<T> imageFetcher;
    private final KeyFetcher<K, T> keyFetcher;

    private final ImageCache<K> imageCache;

    private final WeakHashMap<ImageView, K> imageLoadingMap = new WeakHashMap<>();

    private Drawable loadingPlaceholder;

    public SimpleImageLoader(int loadingPlaceholderId,
                             int errorPlaceholder,
                             int timeoutSeconds,
                             int maxCacheSize,
                             KeyFetcher<K, T> keyFetcher) {
        this.loadingPlaceholderId = loadingPlaceholderId;
        this.errorPlaceholder = errorPlaceholder;
        this.timeoutSeconds = timeoutSeconds;
        this.keyFetcher = keyFetcher;

        imageFetcher = getImageFetcher();
        imageCache = new ImageCache<>(maxCacheSize);
    }

    public void displayImage(@NonNull ImageView imageView, @NonNull T data) {
        displayImage(imageView, data, errorPlaceholder);
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull T data,
                             @DrawableRes int errorPlaceholder) {
        K key = keyFetcher.getKey(data);
        Bitmap cachedBitmap = imageCache.getBitmap(key);
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        imageView.setImageDrawable(getPlaceholder(imageView.getContext()));
        Maybe.fromCallable(() -> getDataOrThrow(data))
                .timeout(timeoutSeconds, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(bitmap -> onImageLoaded(bitmap, imageView, key))
                .doOnError(t -> imageView.setImageResource(errorPlaceholder))
                .onErrorComplete()
                .subscribe();
        imageLoadingMap.put(imageView, key);
    }

    @Nullable
    public Bitmap getImage(@Nonnull T data, long timeoutSeconds) {
        return Maybe.fromCallable(() -> getDataOrThrow(data))
                .timeout(timeoutSeconds, TimeUnit.SECONDS)
                .onErrorComplete()
                .blockingGet();
    }

    public void loadImage(@Nonnull T data, Callback<Bitmap> onCompleted) {
        Maybe.fromCallable(() -> getDataOrThrow(data))
                .timeout(timeoutSeconds, TimeUnit.SECONDS)
                .doOnSuccess(onCompleted::call)
                .doOnError(t -> onCompleted.call(null))
                .onErrorComplete()
                .subscribe();
    }

    public void displayImage(@NonNull RemoteViews widgetView,
                             @IdRes int viewId,
                             @NonNull T data,
                             @NonNull BitmapTransformer bitmapTransformer,
                             @DrawableRes int placeholder) {
        Bitmap cachedBitmap = imageCache.getBitmap(keyFetcher.getKey(data));
        if (cachedBitmap != null) {
            widgetView.setImageViewBitmap(viewId, bitmapTransformer.transform(cachedBitmap));
            return;
        }

        if (placeholder == -1) {
            widgetView.setImageViewBitmap(viewId, null);
        } else {
            widgetView.setImageViewResource(viewId, loadingPlaceholderId);
        }
        Maybe.fromCallable(() -> getDataOrThrow(data))
                .timeout(timeoutSeconds, TimeUnit.SECONDS)
                .map(bitmapTransformer::transform)
                .doOnSuccess(bitmap -> widgetView.setImageViewBitmap(viewId, bitmap))
                .doOnError(t -> widgetView.setImageViewResource(viewId, placeholder))
                .onErrorComplete()
                .subscribe();
    }

    protected abstract ImageFetcher<T> getImageFetcher();

    private Drawable getPlaceholder(Context context) {
        if (loadingPlaceholder == null && loadingPlaceholderId != -1) {
            loadingPlaceholder = ContextCompat.getDrawable(context, loadingPlaceholderId);
        }
        return loadingPlaceholder;
    }

    private void onImageLoaded(Bitmap bitmap, ImageView imageView, K key) {
        //if task is actual
        if (imageLoadingMap.get(imageView) == key) {
            imageView.setImageBitmap(bitmap);
        }
    }

    @Nonnull
    private Bitmap getDataOrThrow(T data) {
        if (data == null) {
            throw new RuntimeException("data is null");
        }
        Bitmap bitmap = getData(data);
        if (bitmap == null) {
            throw new RuntimeException("bitmap is null");
        }
        return bitmap;
    }

    @Nullable
    private Bitmap getData(T data) {
        K key = keyFetcher.getKey(data);
        Bitmap bitmap = imageCache.getBitmap(key);
        if (bitmap == null) {
            synchronized (this) {
                bitmap = imageCache.getBitmap(key);
                if (bitmap == null) {
                    bitmap = imageFetcher.loadImage(data);
                    if (bitmap != null) {
                        imageCache.putBitmap(key, bitmap);
                    }
                }
            }
        }
        return bitmap;
    }
    public interface ImageFetcher<T> {
        Bitmap loadImage(T data);
    }

    public interface KeyFetcher<K, T> {
        K getKey(T data);
    }

    public interface BitmapTransformer {
        Bitmap transform(Bitmap bitmap);
    }
}
