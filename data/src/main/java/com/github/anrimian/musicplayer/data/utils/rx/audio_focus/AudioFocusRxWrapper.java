package com.github.anrimian.musicplayer.data.utils.rx.audio_focus;

import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.GAIN;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS_SHORTLY;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS_TRANSIENT;

import android.content.Context;
import android.media.AudioManager;

import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * Created on 21.04.2018.
 */
public class AudioFocusRxWrapper {

    private final AudioManager audioManager;

    public AudioFocusRxWrapper(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Nullable
    public Observable<AudioFocusEvent> requestAudioFocus(int streamType, int durationHint) {
        AudioFocusObservable audioFocusObservable = new AudioFocusObservable();
        //W/AudioManager: Use of stream types is deprecated for operations other than volume control
        //W/AudioManager: See the documentation of requestAudioFocus() for what to use instead with android.media.AudioAttributes to qualify your playback use case
        int audioFocusResult = audioManager.requestAudioFocus(audioFocusObservable, streamType,
                durationHint);
        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return audioFocusObservable.getObservable()
                    .doOnDispose(() -> audioManager.abandonAudioFocus(audioFocusObservable));
        }
        return null;
    }

    private static class AudioFocusObservable implements AudioManager.OnAudioFocusChangeListener {

        private final PublishSubject<AudioFocusEvent> subject = PublishSubject.create();

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN: {
                    subject.onNext(GAIN);
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                    subject.onNext(LOSS_TRANSIENT);
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
