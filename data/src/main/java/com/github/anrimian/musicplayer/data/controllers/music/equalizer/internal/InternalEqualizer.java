package com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal;

import static com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository.NO_PRESET;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;

import android.media.audiofx.Equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer;
import com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
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

//two instances of eq are not allowed? - done, doesn't help
//release and nullify on detach? - done, doesn't help
//attachEqualizer - what if session id was changed? - reinit - done
//always call release to eq and android media player - done in player, always call on pause

//try to init twice?
// + 4/5 errors caused from fragment onFirstViewAttach()
//always attach session id(do not use audio session id = 0) for using correct audio session id(can't do with android media player) - skip

//calling before media player is prepared?(MediaPlayer.setOnCompletionListener)
//last resort: retry(done) + handle errors
//implement error state
public class InternalEqualizer implements AppEqualizer {

    private final EqualizerStateRepository equalizerStateRepository;

    private final BehaviorSubject<EqualizerState> currentStateSubject = BehaviorSubject.create();

    private final EqualizerObjectHolder equalizerHolder;

    public InternalEqualizer(EqualizerStateRepository equalizerStateRepository, Analytics analytics) {
        this.equalizerStateRepository = equalizerStateRepository;

        equalizerHolder = new EqualizerObjectHolder(analytics::processNonFatalError);
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

            if (equalizer != null) {
                equalizer.setEnabled(true);
            }
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

    public Observable<EqInitializationState> getEqInitializationState() {
        return equalizerHolder.getEqInitializationState();
    }

    public void tryToRelaunchEqualizer() {
        equalizerHolder.resurrectEqualizer();
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

    public void release() {
        equalizerHolder.releaseEqualizer();
    }

    private void applyEqualizerState(Equalizer equalizer, EqualizerState equalizerState) {
        for (Map.Entry<Short, Short> band: equalizerState.getBendLevels().entrySet()) {
            equalizer.setBandLevel(band.getKey(), band.getValue());
        }
    }

    private EqualizerState extractEqualizerState(Equalizer equalizer) {
        Map<Short, Short> maps = new HashMap<>();
        for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
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

        private static final int DEFAULT_AUDIO_SESSION_ID = 1;
        private static final int EQ_PRIORITY = 1000;

        private final BehaviorSubject<EqInitializationState> stateSubject = BehaviorSubject.createDefault(EqInitializationState.IDLE);

        private final Callback<Throwable> onInitializationError;

        private Equalizer mainEqualizer;
        private int currentAudioSessionId = DEFAULT_AUDIO_SESSION_ID;

        @Nullable
        private Callback<Equalizer> deferredInitFunc;

        private EqualizerObjectHolder(Callback<Throwable> onInitializationError) {
            this.onInitializationError = onInitializationError;
        }

        @Nullable
        private Equalizer initEqualizer(int audioSessionId, Callback<Equalizer> initFunc) {
            synchronized (this) {
                if (currentAudioSessionId != audioSessionId) {
                    releaseEqualizer();
                }

                if (mainEqualizer == null) {
                    currentAudioSessionId = audioSessionId;
                    mainEqualizer = newEqualizer(EQ_PRIORITY, audioSessionId);
                    if (mainEqualizer == null) {
                        stateSubject.onNext(EqInitializationState.INITIALIZATION_ERROR);
                        deferredInitFunc = initFunc;
                    } else {
                        stateSubject.onNext(EqInitializationState.INITIALIZED);
                        initFunc.call(mainEqualizer);
                    }
                }
                return mainEqualizer;
            }
        }

        private void resurrectEqualizer() {
            if (mainEqualizer == null
                    && currentAudioSessionId != DEFAULT_AUDIO_SESSION_ID
                    && deferredInitFunc != null
                    && stateSubject.getValue() == EqInitializationState.INITIALIZATION_ERROR) {
                mainEqualizer = newEqualizer(EQ_PRIORITY, currentAudioSessionId);
                if (mainEqualizer != null) {
                    deferredInitFunc.call(mainEqualizer);
                    deferredInitFunc = null;
                    mainEqualizer.setEnabled(true);
                    stateSubject.onNext(EqInitializationState.INITIALIZED);
                }
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
                    currentAudioSessionId = DEFAULT_AUDIO_SESSION_ID;
                    stateSubject.onNext(EqInitializationState.IDLE);
                }
            }
        }

        private <T> T useEqualizer(Mapper<Equalizer, T> func) {
            synchronized (this) {
                Equalizer equalizer;
                if (mainEqualizer == null) {
                    equalizer = newEqualizer(0, currentAudioSessionId);
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

        private Observable<EqInitializationState> getEqInitializationState() {
            return stateSubject;
        }

        @Nullable
        private Equalizer newEqualizer(int priority, int audioSession) {
            RuntimeException ex = null;
            for (int i = 0; i < 3; i++) {
                try {
                    return new Equalizer(priority, audioSession);
                } catch (RuntimeException e) {
                    ex = e;
                }
            }
            onInitializationError.call(ex);
            return null;
        }

    }
}
