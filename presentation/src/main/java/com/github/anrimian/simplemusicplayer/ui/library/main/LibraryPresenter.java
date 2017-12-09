package com.github.anrimian.simplemusicplayer.ui.library.main;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created on 02.11.2017.
 */

@InjectViewState
public class LibraryPresenter extends MvpPresenter<LibraryView> {

    private MusicPlayerInteractor musicPlayerInteractor;
    private Scheduler uiScheduler;

    private CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable trackStateDisposable;

    private List<Composition> currentPlayList = new ArrayList<>();
    private Composition currentComposition;

    public LibraryPresenter(MusicPlayerInteractor musicPlayerInteractor,
                            Scheduler uiScheduler) {
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().bindPlayList(currentPlayList);
        getViewState().showInfinitePlayingButton(musicPlayerInteractor.isInfinitePlayingEnabled());
        getViewState().showRandomPlayingButton(musicPlayerInteractor.isRandomPlayingEnabled());


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    void onStart() {
        subscribeOnPlayerStateChanges();
        subscribeOnCurrentCompositionChanging();
        subscribeOnCurrentPlaylistChanging();
    }

    void onStop() {
        presenterDisposable.clear();
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
        }
    }

    void onPlayButtonClicked() {
        musicPlayerInteractor.play();
    }

    void onStopButtonClicked() {
        musicPlayerInteractor.pause();
    }

    void onSkipToPreviousButtonClicked() {
        musicPlayerInteractor.skipToPrevious();
    }

    void onSkipToNextButtonClicked() {
        musicPlayerInteractor.skipToNext();
    }

    void onInfinitePlayingButtonClicked(boolean enabled) {
        musicPlayerInteractor.setInfinitePlayingEnabled(enabled);
        getViewState().showInfinitePlayingButton(enabled);
    }

    void onRandomPlayingButtonClicked(boolean enabled) {
        musicPlayerInteractor.setRandomPlayingEnabled(enabled);
        getViewState().showRandomPlayingButton(enabled);
    }

    private void subscribeOnCurrentCompositionChanging() {
        presenterDisposable.add(musicPlayerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionChanged));
    }

    private void onCurrentCompositionChanged(Composition composition) {
        currentComposition = composition;
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
            trackStateDisposable = null;
        }
        getViewState().showCurrentComposition(composition);
        getViewState().showTrackState(0, composition.getDuration());
        subscribeOnTrackPositionChanging();
    }

    private void subscribeOnPlayerStateChanges() {
        presenterDisposable.add(musicPlayerInteractor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateChanged));
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PLAY: {
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

    private void subscribeOnCurrentPlaylistChanging() {
        presenterDisposable.add(musicPlayerInteractor.getCurrentPlayListObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListChanged));
    }

    private void onPlayListChanged(List<Composition> newPlayList) {
        currentPlayList.clear();
        currentPlayList.addAll(newPlayList);
        getViewState().updatePlayList();
    }

    private void subscribeOnTrackPositionChanging() {
        trackStateDisposable = musicPlayerInteractor.getTrackPositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onTrackPositionChanged);
    }

    private void onTrackPositionChanged(Long currentPosition) {
        long duration = currentComposition.getDuration();
        getViewState().showTrackState(currentPosition, duration);
    }
}
