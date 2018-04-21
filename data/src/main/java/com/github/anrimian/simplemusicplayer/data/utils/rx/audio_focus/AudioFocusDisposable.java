package com.github.anrimian.simplemusicplayer.data.utils.rx.audio_focus;

import android.media.AudioManager;

import io.reactivex.disposables.Disposable;

/**
 * Created on 05.11.2017.
 */

class AudioFocusDisposable implements Disposable {

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    private boolean isDisposed = false;

    AudioFocusDisposable(AudioManager audioManager,
                                AudioManager.OnAudioFocusChangeListener audioFocusChangeListener) {
        this.audioManager = audioManager;
        this.audioFocusChangeListener = audioFocusChangeListener;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
            isDisposed = true;
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
}
