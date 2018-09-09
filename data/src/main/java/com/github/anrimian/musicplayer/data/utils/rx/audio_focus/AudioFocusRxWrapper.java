package com.github.anrimian.musicplayer.data.utils.rx.audio_focus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;

import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.GAIN;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS_SHORTLY;

/**
 * Created on 21.04.2018.
 */
public class AudioFocusRxWrapper {

    private final AudioManager audioManager;
    private final AudioFocusObservable audioFocusChangeListener;

    public AudioFocusRxWrapper(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioFocusChangeListener = new AudioFocusObservable(audioManager);
    }

    @SuppressLint("CheckResult")
    @Nullable
    public Observable<AudioFocusEvent> requestAudioFocus(int streamType, int durationHint) {
        int audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener, streamType,
                durationHint);
        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return audioFocusChangeListener.getObservable();
        }
        return null;
    }

    private class AudioFocusObservable implements AudioManager.OnAudioFocusChangeListener {

        private final PublishSubject<AudioFocusEvent> subject = PublishSubject.create();

        @SuppressLint("CheckResult")
        private AudioFocusObservable(AudioManager audioManager) {
            subject.doOnDispose(() -> audioManager.abandonAudioFocus(this));
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN: {
                    subject.onNext(GAIN);
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                    subject.onNext(LOSS_SHORTLY);
                    break;
                }
                default: {
                    subject.onNext(LOSS);
                    break;
                }
            }
        }

        private Observable<AudioFocusEvent> getObservable() {
            return subject;
        }
    }
}
