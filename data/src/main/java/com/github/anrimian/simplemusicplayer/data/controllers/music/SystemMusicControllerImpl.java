package com.github.anrimian.simplemusicplayer.data.controllers.music;

import android.content.Context;
import android.media.AudioManager;

import com.github.anrimian.simplemusicplayer.data.utils.rx.audio_focus.AudioFocusRxWrapper;
import com.github.anrimian.simplemusicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.simplemusicplayer.domain.models.player.AudioFocusEvent;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.STREAM_MUSIC;

/**
 * Created on 10.12.2017.
 */

public class SystemMusicControllerImpl implements SystemMusicController {

    private AudioManager audioManager;
    private AudioFocusChangeListener audioFocusChangeListener = new AudioFocusChangeListener();

    private PublishSubject<AudioFocusEvent> audioFocusSubject = PublishSubject.create();

    private final AudioFocusRxWrapper audioFocusRxWrapper;

    public SystemMusicControllerImpl(Context context) {
        audioFocusRxWrapper = new AudioFocusRxWrapper(context);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    @Deprecated
    public boolean requestAudioFocusOld() {
        int audioFocusResult = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                STREAM_MUSIC,
                AUDIOFOCUS_GAIN);
        return audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Nullable
    @Override
    @Deprecated
    public Observable<AudioFocusEvent> requestAudioFocus() {
        return audioFocusRxWrapper.requestAudioFocus(STREAM_MUSIC, AUDIOFOCUS_GAIN);
    }

    @Override
    @Deprecated
    public Observable<AudioFocusEvent> getAudioFocusObservable() {
        return audioFocusSubject;
    }

    @Override
    @Deprecated
    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }

    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AUDIOFOCUS_GAIN: {
                    audioFocusSubject.onNext(AudioFocusEvent.GAIN);
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                    audioFocusSubject.onNext(AudioFocusEvent.LOSS_SHORTLY);
                    break;
                }
                default: {
                    audioFocusSubject.onNext(AudioFocusEvent.LOSS);
                    break;
                }
            }
        }
    }
}
