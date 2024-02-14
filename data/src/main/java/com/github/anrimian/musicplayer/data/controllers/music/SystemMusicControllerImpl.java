package com.github.anrimian.musicplayer.data.controllers.music;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.STREAM_MUSIC;
import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;

import android.content.Context;
import android.media.AudioManager;

import com.github.anrimian.musicplayer.data.utils.rx.audio_focus.RxAudioFocus;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.volume.VolumeObserver;
import com.github.anrimian.musicplayer.data.utils.rx.receivers.RxReceivers;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created on 10.12.2017.
 */

public class SystemMusicControllerImpl implements SystemMusicController {

    private final Context context;
    private final AudioManager audioManager;

    private VolumeState volumeState;

    public SystemMusicControllerImpl(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Nullable
    @Override
    public Observable<AudioFocusEvent> requestAudioFocus() {
        return RxAudioFocus.requestAudioFocus(audioManager, STREAM_MUSIC, AUDIOFOCUS_GAIN);
    }

    @Override
    public Observable<Object> getAudioBecomingNoisyObservable() {
        return RxReceivers.from(AudioManager.ACTION_AUDIO_BECOMING_NOISY, context)
                .map(i -> TRIGGER);
    }

    @Override
    public Observable<Integer> getVolumeObservable() {
        return VolumeObserver.getVolumeObservable(context, audioManager);
    }

    @Override
    public Observable<VolumeState> getVolumeStateObservable() {
        return VolumeObserver.getVolumeStateObservable(context, audioManager)
                .doOnNext(volumeState -> this.volumeState = volumeState);
    }

    @Override
    public void setVolume(int volume) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (volume <= maxVolume && volume >= 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }
    }

    @Override
    public void changeVolumeBy(int volume) {
        if (volumeState != null) {
            setVolume(volumeState.getVolume() + volume);
        }
    }

}
