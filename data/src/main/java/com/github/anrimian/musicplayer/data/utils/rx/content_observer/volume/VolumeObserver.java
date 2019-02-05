package com.github.anrimian.musicplayer.data.utils.rx.content_observer.volume;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;

import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;

import androidx.annotation.NonNull;
import io.reactivex.Observable;

import static android.provider.Settings.System.CONTENT_URI;

public class VolumeObserver {

    public static Observable<Integer> getVolumeObservable(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return RxContentObserver.getObservable(context.getContentResolver(), CONTENT_URI)
                .map(o -> audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }
}
