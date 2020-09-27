package com.github.anrimian.musicplayer.data.controllers.music.equalizer;

import android.app.Activity;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.external.ExternalEqualizer;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.InternalEqualizer;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

public class EqualizerController {

    private final SettingsRepository settingsRepository;
    private final ExternalEqualizer externalEqualizer;
    private final InternalEqualizer internalEqualizer;

    private int audioSessionId;

    private AppEqualizer currentEqualizer;

    public EqualizerController(SettingsRepository settingsRepository,
                               ExternalEqualizer externalEqualizer,
                               InternalEqualizer internalEqualizer) {
        this.settingsRepository = settingsRepository;
        this.externalEqualizer = externalEqualizer;
        this.internalEqualizer = internalEqualizer;
    }

    public void attachEqualizer(int audioSessionId) {
        this.audioSessionId = audioSessionId;
        int type = settingsRepository.getSelectedEqualizerType();
        if (type == EqualizerType.NONE) {
            return;
        }
        currentEqualizer = selectEqualizerByType(type);
        currentEqualizer.attachEqualizer(audioSessionId);
    }

    public void detachEqualizer() {
        if (currentEqualizer != null) {
            currentEqualizer.detachEqualizer(this.audioSessionId);
        }
    }

    public void disableEqualizer() {
        if (currentEqualizer != null) {
            currentEqualizer.detachEqualizer(this.audioSessionId);
        }
        settingsRepository.setSelectedEqualizerType(EqualizerType.NONE);
    }

    public void enableEqualizer(int type) {
        settingsRepository.setSelectedEqualizerType(type);
        currentEqualizer = selectEqualizerByType(type);
        currentEqualizer.attachEqualizer(audioSessionId);
    }

    public void launchExternalEqualizerSetup(Activity activity) {
        settingsRepository.setSelectedEqualizerType(EqualizerType.EXTERNAL);
        currentEqualizer = externalEqualizer;
        externalEqualizer.launchExternalEqualizerSetup(activity, this.audioSessionId);
    }

    public int getSelectedEqualizerType() {
        return settingsRepository.getSelectedEqualizerType();
    }

    private AppEqualizer selectEqualizerByType(int type) {
        switch (type) {
            case EqualizerType.EXTERNAL: {
                return externalEqualizer;
            }
            case EqualizerType.APP: {
                return internalEqualizer;
            }
            default: throw new IllegalStateException("unknown equalizer type: " + type);
        }
    }

}
