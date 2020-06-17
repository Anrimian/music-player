package com.github.anrimian.musicplayer.ui.main.external_player;

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.domain.interactors.player.ExternalPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import moxy.MvpPresenter;

public class ExternalPlayerPresenter extends MvpPresenter<ExternalPlayerView> {

    private final ExternalPlayerInteractor interactor;
    private final Scheduler uiScheduler;
    private final ErrorParser errorParser;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    private UriCompositionSource compositionSource;

    public ExternalPlayerPresenter(ExternalPlayerInteractor interactor,
                                   Scheduler uiScheduler,
                                   ErrorParser errorParser) {
        this.interactor = interactor;
        this.uiScheduler = uiScheduler;
        this.errorParser = errorParser;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        subscribeOnPlayerStateChanges();
        subscribeOnTrackPositionChanging();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onSourceForPlayingReceived(UriCompositionSource source) {
        compositionSource = source;
        interactor.startPlaying(compositionSource);
        getViewState().displayComposition(source);
    }

    void onPlayPauseClicked() {
        interactor.playOrPause();
    }

    void onTrackRewoundTo(int progress) {
        interactor.seekTo(progress);
    }

    void onSeekStart() {
        interactor.onSeekStarted();
    }

    void onSeekStop(int progress) {
        interactor.onSeekFinished(progress);
    }

    private void subscribeOnTrackPositionChanging() {
        presenterDisposable.add(interactor.getTrackPositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onTrackPositionChanged));
    }

    private void onTrackPositionChanged(Long currentPosition) {
        if (compositionSource != null) {
            long duration = compositionSource.getDuration();
            getViewState().showTrackState(currentPosition, duration);
        }
    }

    private void subscribeOnPlayerStateChanges() {
        presenterDisposable.add(interactor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateChanged));
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PLAY: {
                getViewState().showPlayState();
                return;
            }
            default: {
                getViewState().showStopState();
            }
        }
    }
}
