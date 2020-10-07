package com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal;

import android.media.audiofx.Equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer;
import com.github.anrimian.musicplayer.data.utils.rx.RxUtils;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerInfo;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class InternalEqualizer implements AppEqualizer {

    private final BehaviorSubject<EqualizerInfo> currentInfoSubject = BehaviorSubject.create();

    private Equalizer equalizer;

    @Override
    public void attachEqualizer(int audioSessionId) {
        if (audioSessionId != 0) {
            equalizer = new Equalizer(1000, audioSessionId);
            equalizer.setEnabled(true);

            System.out.println("KEK bands level range: " + Arrays.toString(equalizer.getBandLevelRange()));
            for(short i = 0; i < equalizer.getNumberOfBands(); i++) {
                System.out.println("KEK band i: " + i);
                System.out.println("KEK band freq range: " + Arrays.toString(equalizer.getBandFreqRange(i)));
                System.out.println("KEK band level: " + equalizer.getBandLevel(i));
            }
        }
    }

    @Override
    public void detachEqualizer(int audioSessionId) {
        if (equalizer != null) {
            equalizer.setEnabled(false);
        }
    }

    public Observable<EqualizerInfo> getEqualizerInfoObservable() {
        return RxUtils.withDefaultValue(
                currentInfoSubject,
                () -> extractEqualizerInfo(new Equalizer(0, 1))
        );
    }

    public void setBandLevel(short bandNumber, short level) {
        if (equalizer != null) {
            equalizer.setBandLevel(bandNumber, level);
        }
    }

    public void setPreset(Preset preset) {
        if (equalizer != null) {
            equalizer.usePreset(preset.getNumber());
            currentInfoSubject.onNext(extractEqualizerInfo(equalizer));
        }
    }

    private EqualizerInfo extractEqualizerInfo(Equalizer equalizer) {
        short[] bandLevelRange = equalizer.getBandLevelRange();

        List<Band> bands = new ArrayList<>();
        for(short i = 0; i < equalizer.getNumberOfBands(); i++) {
            bands.add(new Band(
                    i,
                    equalizer.getBandFreqRange(i),
                    equalizer.getCenterFreq(i),
                    equalizer.getBandLevel(i))
            );
        }

        List<Preset> presets = new ArrayList<>();
        short currentPresetNumber = equalizer.getCurrentPreset();
        Preset currentPreset = null;
        for(short i = 0; i < equalizer.getNumberOfPresets(); i++) {
            Preset preset = new Preset(
                    i,
                    equalizer.getPresetName(i)
            );
            if (i == currentPresetNumber) {
                currentPreset = preset;
            }
            presets.add(preset);
        }

        equalizer.release();

        return new EqualizerInfo(bandLevelRange, bands, presets, currentPreset);
    }

}
