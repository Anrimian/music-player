package com.github.anrimian.musicplayer.ui.equalizer.app;

import com.github.anrimian.musicplayer.domain.interactors.player.EqualizerInteractor;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import io.reactivex.rxjava3.core.Scheduler;
import moxy.MvpPresenter;

public class EqualizerPresenter extends MvpPresenter<EqualizerView> {

    private final EqualizerInteractor interactor;
    private final Scheduler scheduler;
    private final ErrorParser errorParser;

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
        loadEqualizerInfo();
    }

    public void onBandLevelChanged(Band band, short value) {
        interactor.setBandLevel(band.getBandNumber(), value);
    }

    public void onPresetSelected(Preset preset) {
        interactor.setPreset(preset);
    }

    private void loadEqualizerInfo() {
        interactor.getEqualizerInfo()
                .observeOn(scheduler)
                .subscribe(getViewState()::displayEqualizerInfo, this::onDefaultError);
    }

    private void onDefaultError(Throwable throwable) {
        getViewState().showErrorMessage(errorParser.parseError(throwable));
    }
}
