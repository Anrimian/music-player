package com.github.anrimian.musicplayer.data.controllers.music.equalizer;

import android.app.Activity;
import android.content.Context;

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

public class EqualizerController {

    private final SettingsRepository settingsRepository;

    private int audioSessionId;
    //add description - ok, + margin top
    //add launch button - ok
    //disable if no equalizer was present - ok
    //android media player
    public EqualizerController(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void attachEqualizer(Context context, int audioSessionId) {
        this.audioSessionId = audioSessionId;
        switch (settingsRepository.getSelectedEqualizerType()) {
            case EqualizerTypes.EXTERNAL: {
                ExternalEqualizer.attachExternalEqualizer(context, audioSessionId);
                break;
            }
        }
    }

    public void detachEqualizer(Context context) {
        ExternalEqualizer.detachExternalEqualizer(context, this.audioSessionId);
    }

    public void disableExternalEqualizer(Context context) {
        int currentEqualizer = settingsRepository.getSelectedEqualizerType();
        if (currentEqualizer == EqualizerTypes.EXTERNAL) {
            ExternalEqualizer.detachExternalEqualizer(context, this.audioSessionId);
        }
        settingsRepository.setSelectedEqualizerType(EqualizerTypes.NONE);
    }

    public void enableExternalEqualizer(Context context, int equalizerType) {
        if (equalizerType == EqualizerTypes.EXTERNAL) {
            settingsRepository.setSelectedEqualizerType(EqualizerTypes.EXTERNAL);
            ExternalEqualizer.attachExternalEqualizer(context, audioSessionId);
        }
    }

    public void launchExternalEqualizerSetup(Activity activity, int equalizerType) {
        if (equalizerType == EqualizerTypes.EXTERNAL) {
            settingsRepository.setSelectedEqualizerType(EqualizerTypes.EXTERNAL);
            ExternalEqualizer.launchExternalEqualizerSetup(activity, this.audioSessionId);
        }
    }

    public int getSelectedEqualizerType() {
        return settingsRepository.getSelectedEqualizerType();
    }

}
