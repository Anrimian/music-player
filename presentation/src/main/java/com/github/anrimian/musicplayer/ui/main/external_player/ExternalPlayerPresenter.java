package com.github.anrimian.musicplayer.ui.main.external_player;

import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import moxy.MvpPresenter;

public class ExternalPlayerPresenter extends MvpPresenter<ExternalPlayerView> {

    private final CompositionSource compositionSource;
    private final PlayerInteractor interactor;
    private final Scheduler uiScheduler;
    private final ErrorParser errorParser;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    public ExternalPlayerPresenter(CompositionSource compositionSource,
                                   PlayerInteractor interactor,
                                   Scheduler uiScheduler,
                                   ErrorParser errorParser) {
        this.compositionSource = compositionSource;
        this.interactor = interactor;
        this.uiScheduler = uiScheduler;
        this.errorParser = errorParser;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        interactor.startPlaying(compositionSource);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

}
