package com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal;

import android.media.audiofx.Equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer;

public class InternalEqualizer implements AppEqualizer {

    private Equalizer equalizer;

    @Override
    public void attachEqualizer(int audioSessionId) {
        if (audioSessionId != 0) {
            equalizer = new Equalizer(1000, audioSessionId);
            equalizer.setEnabled(true);
        }
    }

    @Override
    public void detachEqualizer(int audioSessionId) {
        if (equalizer != null) {
            equalizer.setEnabled(false);
        }
    }

}
