package com.github.anrimian.musicplayer.data.controllers.music.equalizer;

public interface AppEqualizer {

    void attachEqualizer(int audioSessionId);

    void detachEqualizer(int audioSessionId);
}
