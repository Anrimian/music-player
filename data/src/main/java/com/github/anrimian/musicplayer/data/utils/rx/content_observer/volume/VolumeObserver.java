package com.github.anrimian.musicplayer.data.utils.rx.content_observer.volume;

import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;

import android.content.Context;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

import com.github.anrimian.musicplayer.data.utils.rx.receivers.RxReceivers;
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class VolumeObserver {

    /**
     * Emits volume in absolute values. Has no start event
     */
    public static Observable<Integer> getVolumeObservable(Context context, AudioManager audioManager) {
        return RxReceivers.from("android.media.VOLUME_CHANGED_ACTION", context)
                .flatMapSingle(o -> safeGetStreamVolume(audioManager))
                .distinctUntilChanged();
    }

    /**
     * Emits volume in state model. Has start event
     */
    public static Observable<VolumeState> getVolumeStateObservable(Context context, AudioManager audioManager) {
        PublishSubject<Object> outputChangeSubject = PublishSubject.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.registerAudioDeviceCallback(new AudioDeviceCallback() {
                @Override
                public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                    super.onAudioDevicesAdded(addedDevices);
                    outputChangeSubject.onNext(TRIGGER);
                }

                @Override
                public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                    super.onAudioDevicesRemoved(removedDevices);
                    outputChangeSubject.onNext(TRIGGER);
                }
            }, null);
        }
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        VolumeState volumeState = new VolumeState(maxVolume);
        return outputChangeSubject.startWithItem(TRIGGER)
                .switchMap(t -> RxReceivers.from("android.media.VOLUME_CHANGED_ACTION", context)
                        .flatMapSingle(o -> safeGetStreamVolume(audioManager))
                        .startWith(safeGetStreamVolume(audioManager))
                        .map(volumeState::setVolume));
    }

    private static Single<Integer> safeGetStreamVolume(AudioManager audioManager) {
        return Single.create(emitter -> {
            try {
                emitter.onSuccess(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            } catch (Exception ignored) {}
        });
    }
}
