package com.github.anrimian.musicplayer.ui.utils.image.loader;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SimpleImageLoader<K, T> {

    @DrawableRes
    private final int loadingPlaceholder;

    @DrawableRes
    private final int errorPlaceholder;

    private final int timeoutSeconds;

    private final ImageFetcher<T> imageFetcher;
    private final KeyFetcher<K, T> keyFetcher;

    private final ImageCache<K> imageCache;

    private final WeakHashMap<ImageView, Disposable> imageLoadingMap = new WeakHashMap<>();

    public SimpleImageLoader(int loadingPlaceholder,
                             int errorPlaceholder,
                             int timeoutSeconds,
                             int maxCacheSize,
                             ImageFetcher<T> imageFetcher,
                             KeyFetcher<K, T> keyFetcher) {
        this.loadingPlaceholder = loadingPlaceholder;
        this.errorPlaceholder = errorPlaceholder;
        this.timeoutSeconds = timeoutSeconds;
        this.imageFetcher = imageFetcher;
        this.keyFetcher = keyFetcher;

        imageCache = new ImageCache<>(maxCacheSize);
    }

    public void displayImage(@NonNull ImageView imageView, @NonNull T data) {
        displayImage(imageView, data, errorPlaceholder);
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull T data,
                             @DrawableRes int errorPlaceholder) {
        Bitmap cachedBitmap = imageCache.getBitmap(keyFetcher.getKey(data));
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        if (loadingPlaceholder == -1) {
            imageView.setImageBitmap(null);
        } else {
            imageView.setImageResource(loadingPlaceholder);
        }
        Disposable disposable = imageLoadingMap.get(imageView);
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Single.fromCallable(() -> getDataOrThrow(data))
                .timeout(timeoutSeconds, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageView::setImageBitmap,
                        t -> imageView.setImageResource(errorPlaceholder));
        imageLoadingMap.put(imageView, disposable);
    }

    @Nullable
    public Bitmap getImage(@Nonnull T data) {
        return getData(data);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
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
            widgetView.setImageViewResource(viewId, loadingPlaceholder);
        }
        Single.fromCallable(() -> getDataOrThrow(data))
                .timeout(timeoutSeconds, TimeUnit.SECONDS)
                .map(bitmapTransformer::transform)
                .subscribe(bitmap -> widgetView.setImageViewBitmap(viewId, bitmap),
                        t -> widgetView.setImageViewResource(viewId, placeholder));
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
