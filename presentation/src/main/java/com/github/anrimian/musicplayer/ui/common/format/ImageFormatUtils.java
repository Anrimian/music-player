package com.github.anrimian.musicplayer.ui.common.format;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.github.anrimian.musicplayer.ui.utils.ImageUtils.toCircleBitmap;

public class ImageFormatUtils {

    public static void displayImage(ImageView imageView, Composition composition) {
        Single.fromCallable(() -> getCompositionImage(composition))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageView::setImageBitmap,
                        t -> imageView.setImageResource(R.drawable.ic_music_placeholder));
    }

    private static Bitmap getCompositionImage(Composition composition) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(composition.getFilePath());
        byte[] imageBytes = mmr.getEmbeddedPicture();
        if (imageBytes == null) {
            throw new RuntimeException("not found");
        }
        Bitmap bitmap =  BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return toCircleBitmap(bitmap);
    }
}
