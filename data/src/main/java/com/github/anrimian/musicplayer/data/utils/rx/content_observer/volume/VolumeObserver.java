package com.github.anrimian.musicplayer.data.utils.rx.content_observer.volume;

import android.content.Context;
import android.media.AudioManager;

import com.github.anrimian.musicplayer.data.utils.rx.receivers.RxReceivers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class VolumeObserver {

    public static Observable<Integer> getVolumeObservable(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return RxReceivers.from("android.media.VOLUME_CHANGED_ACTION", context)
                .flatMapSingle(o -> safeGetStreamVolume(audioManager))
                .distinctUntilChanged();
    }

    private static Single<Integer> safeGetStreamVolume(AudioManager audioManager) {
        return Single.create(emitter -> {
            try {
                emitter.onSuccess(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            } catch (Exception ignored) {}
        });
    }
}
