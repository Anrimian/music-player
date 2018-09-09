package com.github.anrimian.musicplayer.data.controllers.music;

import android.content.Context;
import android.media.AudioManager;

import com.github.anrimian.musicplayer.data.utils.rx.audio_focus.AudioFocusRxWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.receivers.RxReceivers;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.STREAM_MUSIC;

/**
 * Created on 10.12.2017.
 */

public class SystemMusicControllerImpl implements SystemMusicController {

    private final AudioFocusRxWrapper audioFocusRxWrapper;
    private final Context context;

    public SystemMusicControllerImpl(Context context) {
        this.context = context;
        audioFocusRxWrapper = new AudioFocusRxWrapper(context);
    }

    @Nullable
    @Override
    public Observable<AudioFocusEvent> requestAudioFocus() {
        return audioFocusRxWrapper.requestAudioFocus(STREAM_MUSIC, AUDIOFOCUS_GAIN);
    }

    @Override
    public Observable<Object> getAudioBecomingNoisyObservable() {
        return RxReceivers.from(AudioManager.ACTION_AUDIO_BECOMING_NOISY, context)
                .map(i -> new Object());
    }

}
