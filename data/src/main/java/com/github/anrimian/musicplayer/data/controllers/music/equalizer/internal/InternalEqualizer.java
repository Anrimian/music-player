package com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal;

import android.media.audiofx.Equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer;
import com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.domain.utils.functions.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository.NO_PRESET;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;

//two instances of eq are not allowed? - done
//release and nullify on detach? - done
//try to init twice?

//always call release to eq and android media player
// + 4/5 errors caused from fragment onFirstViewAttach()
//always attach session id(do not use audio session id = 0) for using correct audio session id
//attachEqualizer - what if session id was changed? - reinit

//calling before media player is prepared?(MediaPlayer.setOnCompletionListener)
//last resort: retry + handle errors

public class InternalEqualizer implements AppEqualizer {

    private final EqualizerStateRepository equalizerStateRepository;

    private final BehaviorSubject<EqualizerState> currentStateSubject = BehaviorSubject.create();

    private final EqualizerObjectHolder equalizerHolder = new EqualizerObjectHolder();

    public InternalEqualizer(EqualizerStateRepository equalizerStateRepository) {
        this.equalizerStateRepository = equalizerStateRepository;
    }

    @Override
    public void attachEqualizer(int audioSessionId) {
        if (audioSessionId != 0) {
            Equalizer equalizer = equalizerHolder.initEqualizer(audioSessionId, eq -> {

                EqualizerState equalizerState = equalizerStateRepository.loadEqualizerState();
                if (equalizerState != null) {
                    applyEqualizerState(eq, equalizerState);
                    currentStateSubject.onNext(equalizerState);
                } else {
                    currentStateSubject.onNext(extractEqualizerState(eq));
                }

            });

            equalizer.setEnabled(true);
        }
    }

    @Override
    public void detachEqualizer(int audioSessionId) {
        equalizerHolder.releaseEqualizer();
    }

    public Single<EqualizerConfig> getEqualizerConfig() {
        return Single.fromCallable(() -> equalizerHolder.useEqualizer(this::extractEqualizerInfo));
    }

    public Observable<EqualizerState> getEqualizerStateObservable() {
        return withDefaultValue(currentStateSubject, () -> {
            EqualizerState equalizerState = equalizerStateRepository.loadEqualizerState();
            if (equalizerState == null) {
                equalizerState = equalizerHolder.useEqualizer(this::extractEqualizerState);
            }
            return equalizerState;
        });
    }

    public void setBandLevel(short bandNumber, short level) {
        Equalizer equalizer = equalizerHolder.getEqualizer();
        if (equalizer != null) {
            equalizer.setBandLevel(bandNumber, level);
        }

        EqualizerState equalizerState = currentStateSubject.getValue();
        if (equalizerState == null) {
            equalizerState = new EqualizerState(NO_PRESET, new HashMap<>());
        }
        equalizerState.getBendLevels().put(bandNumber, level);
        equalizerState.setCurrentPreset(NO_PRESET);
        currentStateSubject.onNext(equalizerState);
    }

    public void saveBandLevel() {
        EqualizerState equalizerState = currentStateSubject.getValue();
        if (equalizerState != null) {
            equalizerStateRepository.saveEqualizerState(equalizerState);
        }
    }

    public void setPreset(Preset preset) {
        Equalizer equalizer = equalizerHolder.getEqualizer();
        if (equalizer == null) {
            EqualizerState equalizerState = equalizerHolder.useEqualizer(this::extractEqualizerState);

            equalizerStateRepository.saveEqualizerState(equalizerState);
            currentStateSubject.onNext(equalizerState);
        } else {
            if (preset.getNumber() != equalizer.getCurrentPreset()
                    && preset.getNumber() <= equalizer.getNumberOfPresets()) {
                equalizer.usePreset(preset.getNumber());
                EqualizerState equalizerState = extractEqualizerState(equalizer);

                equalizerStateRepository.saveEqualizerState(equalizerState);
                currentStateSubject.onNext(equalizerState);
            }
        }
    }

    private void applyEqualizerState(Equalizer equalizer, EqualizerState equalizerState) {
        for (Map.Entry<Short, Short> band: equalizerState.getBendLevels().entrySet()) {
            equalizer.setBandLevel(band.getKey(), band.getValue());
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
            bands.add(new Band(i, equalizer.getCenterFreq(i)));
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

    private static class EqualizerObjectHolder {

        private Equalizer mainEqualizer;

        private Equalizer initEqualizer(int audioSessionId, Callback<Equalizer> initFunc) {
            synchronized (this) {
                if (mainEqualizer == null) {
                    mainEqualizer = new Equalizer(1000, audioSessionId);
                    initFunc.call(mainEqualizer);
                }
                return mainEqualizer;
            }
        }

        @Nullable
        private Equalizer getEqualizer() {
            return mainEqualizer;
        }

        private void releaseEqualizer() {
            synchronized (this) {
                if (mainEqualizer != null) {
                    mainEqualizer.setEnabled(false);
                    mainEqualizer.release();
                    mainEqualizer = null;
                }
            }
        }

        private <T> T useEqualizer(Mapper<Equalizer, T> func) {
            synchronized (this) {
                Equalizer equalizer;
                if (mainEqualizer == null) {
                    equalizer = new Equalizer(0, 1);
                } else {
                    equalizer = mainEqualizer;
                }
                T result = func.map(equalizer);
                if (mainEqualizer == null) {
                    equalizer.release();
                }
                return result;
            }
        }

    }
}
