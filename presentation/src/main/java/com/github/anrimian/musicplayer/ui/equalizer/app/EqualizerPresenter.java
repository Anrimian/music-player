package com.github.anrimian.musicplayer.ui.equalizer.app;

import com.github.anrimian.musicplayer.domain.interactors.player.EqualizerInteractor;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
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
        loadBands();
    }

    private void loadBands() {
        interactor.getBands()
                .observeOn(scheduler)
                .subscribe(getViewState()::displayBands, this::onDefaultError);
    }

    private void onDefaultError(Throwable throwable) {
        getViewState().showErrorMessage(errorParser.parseError(throwable));
    }

    public void onBandLevelChanged(Band band, short value) {
        interactor.setBandLevel(band.getBandNumber(), value);
    }
}
