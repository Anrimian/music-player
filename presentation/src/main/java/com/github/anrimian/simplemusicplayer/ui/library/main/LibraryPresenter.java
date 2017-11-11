package com.github.anrimian.simplemusicplayer.ui.library.main;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created on 02.11.2017.
 */

@InjectViewState
public class LibraryPresenter extends MvpPresenter<LibraryView> {

    private MusicPlayerInteractor musicPlayerInteractor;
    private Scheduler uiScheduler;

    private CompositeDisposable presenterDisposable = new CompositeDisposable();

    public LibraryPresenter(MusicPlayerInteractor musicPlayerInteractor,
                            Scheduler uiScheduler) {
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnPlayerStateChanges();
        subscribeOnCurrentCompositionChanging();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onPlayPauseButtonClicked() {
        musicPlayerInteractor.changePlayState();
    }

    void onSkipToPreviousButtonClicked() {
        musicPlayerInteractor.skipToPrevious();
    }

    void onSkipToNextButtonClicked() {
        musicPlayerInteractor.skipToNext();
    }

    private void subscribeOnCurrentCompositionChanging() {
        presenterDisposable.add(musicPlayerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionChanged));
    }

    private void onCurrentCompositionChanged(Composition composition) {
        getViewState().showCurrentComposition(composition);
    }

    private void subscribeOnPlayerStateChanges() {
        presenterDisposable.add(musicPlayerInteractor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateChanged));
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PLAYING: {
                getViewState().showPlayState();
                return;
            }
            case IDLE: {
                getViewState().hideMusicControls();
                return;
            }
            default: {
                getViewState().showStopState();
            }
        }
    }
}
