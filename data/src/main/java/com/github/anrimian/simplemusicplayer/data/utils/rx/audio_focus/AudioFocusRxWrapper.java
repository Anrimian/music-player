package com.github.anrimian.simplemusicplayer.data.utils.rx.audio_focus;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.github.anrimian.simplemusicplayer.domain.models.player.AudioFocusEvent;

import javax.annotation.Nullable;

import io.reactivex.Observable;

/**
 * Created on 21.04.2018.
 */
public class AudioFocusRxWrapper {

    private AudioManager audioManager;

    public AudioFocusRxWrapper(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Nullable
    public Observable<AudioFocusEvent> requestAudioFocus(int streamType, int durationHint) {
        AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {

            }
        };
        int audioFocusResult = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("KEK", "requestAudioFocus: true");
            return Observable.create(emitter -> {

            });
        }
        return null;
    }
}
