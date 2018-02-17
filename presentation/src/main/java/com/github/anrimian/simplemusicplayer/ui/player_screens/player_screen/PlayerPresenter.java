package com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen;

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
public class PlayerPresenter extends MvpPresenter<PlayerView> {

    private final MusicPlayerInteractor musicPlayerInteractor;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable trackStateDisposable;
    private Disposable currentCompositionDisposable;

    private final List<Composition> currentPlayList = new ArrayList<>();
    private Composition currentComposition;

    public PlayerPresenter(MusicPlayerInteractor musicPlayerInteractor,
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

    void onStart() {
        subscribeOnPlayerStateChanges();
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

    void onEnableInfinitePlayingButtonClicked() {
        musicPlayerInteractor.setInfinitePlayingEnabled(true);
        getViewState().showInfinitePlayingButton(true);
    }

    void onDisableInfinitePlayingButtonClicked() {
        musicPlayerInteractor.setInfinitePlayingEnabled(false);
        getViewState().showInfinitePlayingButton(false);
    }

    void onEnableRandomPlayingButtonClicked() {
        musicPlayerInteractor.setRandomPlayingEnabled(true);
        getViewState().showRandomPlayingButton(true);
    }

    void onDisableRandomPlayingButtonClicked() {
        musicPlayerInteractor.setRandomPlayingEnabled(false);
        getViewState().showRandomPlayingButton(false);
    }

    void onShareCompositionButtonClicked() {
        getViewState().showShareMusicDialog(currentComposition.getFilePath());
    }

    void onCompositionItemClicked(Composition composition) {
        musicPlayerInteractor.moveToComposition(composition);
    }

    private void subscribeOnCurrentCompositionChanging() {
        currentCompositionDisposable = musicPlayerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionChanged);
        presenterDisposable.add(currentCompositionDisposable);
    }

    private void onCurrentCompositionChanged(Composition composition) {
        currentComposition = composition;
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
            trackStateDisposable = null;
        }
        int position = currentPlayList.indexOf(composition);
        if (position != -1) {
            getViewState().showCurrentComposition(composition, position);
            getViewState().showTrackState(0, composition.getDuration());
            subscribeOnTrackPositionChanging();
        } else {
            throw new IllegalStateException("can not find position in queue: " + composition);
        }
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
        if (currentCompositionDisposable != null) {
            currentCompositionDisposable.dispose();
            currentCompositionDisposable = null;
            currentComposition = null;
        }
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
            trackStateDisposable = null;
        }

        List<Composition> oldPlayList = new ArrayList<>(currentPlayList);
        currentPlayList.clear();
        currentPlayList.addAll(newPlayList);
        getViewState().updatePlayList(oldPlayList, currentPlayList);

        subscribeOnCurrentCompositionChanging();
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
