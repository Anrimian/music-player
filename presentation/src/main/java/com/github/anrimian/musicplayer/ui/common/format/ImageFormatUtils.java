package com.github.anrimian.musicplayer.ui.common.format;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.images.ImageCache;
import com.github.anrimian.musicplayer.ui.utils.ImageUtils;

import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ImageFormatUtils {

    private static final WeakHashMap<ImageView, Disposable> imageLoadingMap = new WeakHashMap<>();

    public static void displayImage(@NonNull ImageView imageView,
                                    @NonNull Composition composition) {
        imageView.setImageResource(R.drawable.ic_music_placeholder);
        Disposable disposable = imageLoadingMap.get(imageView);
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Single.fromCallable(() -> getCompositionImageOrThrow(composition.getFilePath(), composition.getId()))
                .timeout(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageView::setImageBitmap,
                        t -> imageView.setImageResource(R.drawable.ic_music_placeholder));
        imageLoadingMap.put(imageView, disposable);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public static void displayImage(@NonNull RemoteViews widgetView,
                                    @IdRes int viewId,
                                    @NonNull String compositionFile,
                                    long compositionId) {
        widgetView.setImageViewResource(viewId, R.drawable.ic_music_placeholder);
        Single.fromCallable(() -> getCompositionImageOrThrow(compositionFile, compositionId))
                .timeout(5, TimeUnit.SECONDS)
                .map(ImageUtils::toCircleBitmap)
                .subscribe(bitmap -> widgetView.setImageViewBitmap(viewId, bitmap),
                        t -> widgetView.setImageViewResource(viewId, R.drawable.ic_music_placeholder));
    }
    
    @Nonnull
    private static Bitmap getCompositionImageOrThrow(String compositionFile, long compositionId) {
        Bitmap bitmap = getCompositionImage(compositionFile, compositionId);
        if (bitmap == null) {
            throw new RuntimeException("composition image not found");
        }
        return bitmap;
    }

    @Nullable
    public static Bitmap getCompositionImage(Composition composition) {
        return getCompositionImage(composition.getFilePath(), composition.getId());
    }

    @Nullable
    public static Bitmap getCompositionImage(String compositionFile, long compositionId) {
        ImageCache imageCache = ImageCache.getInstance();
        Bitmap bitmap = imageCache.getBitmap(compositionId);
        if (bitmap == null) {
            synchronized (ImageCache.class) {
                bitmap = imageCache.getBitmap(compositionId);
                if (bitmap == null) {
                    bitmap = extractImageComposition(compositionFile);
                    if (bitmap != null) {
                        imageCache.putBitmap(compositionId, bitmap);
                    }
                }
            }
        }
        return bitmap;
    }

    @Nullable
    private static Bitmap extractImageComposition(String filePath) {
        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filePath);
            byte[] imageBytes = mmr.getEmbeddedPicture();
            mmr.release();
            if (imageBytes == null) {
                return null;
            }
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
    }
}
