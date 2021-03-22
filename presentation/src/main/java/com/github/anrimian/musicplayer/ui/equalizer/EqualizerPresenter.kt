package com.github.anrimian.musicplayer.ui.equalizer;

import com.github.anrimian.musicplayer.domain.interactors.player.EqualizerInteractor;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import moxy.MvpPresenter;

public class EqualizerPresenter extends MvpPresenter<EqualizerView> {

    private final EqualizerInteractor interactor;
    private final Scheduler scheduler;
    private final ErrorParser errorParser;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    public EqualizerPresenter(EqualizerInteractor interactor,
                              Scheduler scheduler,
                              ErrorParser errorParser) {
        this.interactor = interactor;
        this.scheduler = scheduler;
        this.errorParser = errorParser;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        loadEqualizerConfig();
    }

    private void loadEqualizerConfig() {
        interactor.getEqualizerConfig()
                .observeOn(scheduler)
                .subscribe(this::onEqualizerConfigReceived);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    public void onBandLevelChanged(Band band, short value) {
        interactor.setBandLevel(band.getBandNumber(), value);
    }

    public void onBandLevelDragStopped() {
        interactor.saveBandLevel();
    }

    public void onPresetSelected(Preset preset) {
        interactor.setPreset(preset);
    }

    private void onEqualizerConfigReceived(EqualizerConfig config) {
        getViewState().displayEqualizerConfig(config);
        subscribeOnEqualizerState(config);
    }

    private void subscribeOnEqualizerState(EqualizerConfig config) {
        presenterDisposable.add(interactor.getEqualizerStateObservable()
                .observeOn(scheduler)
                .subscribe(
                        equalizerState -> getViewState().displayEqualizerState(equalizerState, config),
                        this::onDefaultError)
        );
    }

    private void onDefaultError(Throwable throwable) {
        getViewState().showErrorMessage(errorParser.parseError(throwable));
    }
}
