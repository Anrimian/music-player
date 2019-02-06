package com.github.anrimian.musicplayer.data.utils.rx.content_observer.volume;

import android.content.Context;
import android.media.AudioManager;

import com.github.anrimian.musicplayer.data.utils.rx.receivers.RxReceivers;

import io.reactivex.Observable;

public class VolumeObserver {

    public static Observable<Integer> getVolumeObservable(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return RxReceivers.from("android.media.VOLUME_CHANGED_ACTION", context)
                .map(o -> audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
                .distinctUntilChanged();
    }
}
