package com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal;

import android.media.audiofx.Equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class InternalEqualizer implements AppEqualizer {

    private final BehaviorSubject<EqualizerState> currentStateSubject = BehaviorSubject.create();

    private Equalizer equalizer;

    @Override
    public void attachEqualizer(int audioSessionId) {
        if (audioSessionId != 0) {
            equalizer = new Equalizer(1000, audioSessionId);
            equalizer.setEnabled(true);

            currentStateSubject.onNext(extractEqualizerState(equalizer));
        }
    }

    @Override
    public void detachEqualizer(int audioSessionId) {
        if (equalizer != null) {
            equalizer.setEnabled(false);
        }
    }

    public Single<EqualizerConfig> getEqualizerConfig() {
        return Single.fromCallable(() -> {
            Equalizer tempEqualizer = new Equalizer(0, 1);
            EqualizerConfig config = extractEqualizerInfo(tempEqualizer);
            tempEqualizer.release();
            return config;
        });
    }

    public Observable<EqualizerState> getEqualizerStateObservable() {
        return currentStateSubject;
    }

    public void setBandLevel(short bandNumber, short level) {
        if (equalizer != null) {
            equalizer.setBandLevel(bandNumber, level);
        }
    }

    public void setPreset(Preset preset) {
        if (equalizer != null
                && preset.getNumber() != equalizer.getCurrentPreset()
                && preset.getNumber() <= equalizer.getNumberOfPresets()) {
            equalizer.usePreset(preset.getNumber());
            currentStateSubject.onNext(extractEqualizerState(equalizer));
        }
    }

    private EqualizerState extractEqualizerState(Equalizer equalizer) {
        Map<Short, Short> maps = new HashMap<>();
        for(short i = 0; i < equalizer.getNumberOfBands(); i++) {
            maps.put(i, equalizer.getBandLevel(i));
        }
        return new EqualizerState(
                equalizer.getCurrentPreset(),
                maps
        );
    }

    private EqualizerConfig extractEqualizerInfo(Equalizer equalizer) {
        short[] bandLevelRange = equalizer.getBandLevelRange();

        short lowestRange = bandLevelRange[0];
        short highestRange = bandLevelRange[1];

        List<Band> bands = new ArrayList<>();
        for(short i = 0; i < equalizer.getNumberOfBands(); i++) {
            bands.add(new Band(
                    i,
                    equalizer.getBandFreqRange(i),
                    equalizer.getCenterFreq(i))
            );
        }

        List<Preset> presets = new ArrayList<>();
        for(short i = 0; i < equalizer.getNumberOfPresets(); i++) {
            Preset preset = new Preset(
                    i,
                    equalizer.getPresetName(i)
            );
            presets.add(preset);
        }

        return new EqualizerConfig(lowestRange, highestRange, bands, presets);
    }

}
