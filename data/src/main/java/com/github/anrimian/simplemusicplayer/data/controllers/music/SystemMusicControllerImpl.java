package com.github.anrimian.simplemusicplayer.data.controllers.music;

import android.content.Context;
import android.media.AudioManager;

import com.github.anrimian.simplemusicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.simplemusicplayer.domain.models.player.AudioFocusEvent;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created on 10.12.2017.
 */

public class SystemMusicControllerImpl implements SystemMusicController {

    private AudioManager audioManager;
    private AudioFocusChangeListener audioFocusChangeListener = new AudioFocusChangeListener();

    private PublishSubject<AudioFocusEvent> audioFocusSubject = PublishSubject.create();

    public SystemMusicControllerImpl(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public boolean requestAudioFocus() {
        int audioFocusResult = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        } else {
            return true;
        }
    }

    @Override
    public Observable<AudioFocusEvent> getAudioFocusObservable() {
        return audioFocusSubject;
    }

    @Override
    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }

    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN: {
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
