package com.github.anrimian.musicplayer.ui.player_screen;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

/**
 * Created on 02.11.2017.
 */

@InjectViewState
public class PlayerPresenter extends MvpPresenter<PlayerView> {

    private final MusicPlayerInteractor musicPlayerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable trackStateDisposable;
    private Disposable currentCompositionDisposable;

    private final List<PlayQueueItem> playQueue = new ArrayList<>();
    private PlayQueueItem currentItem;

    @Nullable
    private Composition compositionToAddToPlayList;

    public PlayerPresenter(MusicPlayerInteractor musicPlayerInteractor,
                           PlayListsInteractor playListsInteractor,
                           ErrorParser errorParser,
                           Scheduler uiScheduler) {
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.playListsInteractor = playListsInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().bindPlayList(playQueue);
        getViewState().showInfinitePlayingButton(musicPlayerInteractor.isInfinitePlayingEnabled());
        getViewState().showRandomPlayingButton(musicPlayerInteractor.isRandomPlayingEnabled());
    }

    void onStart() {
        subscribeOnPlayerStateChanges();
        subscribeOnPlayQueue();
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
        getViewState().showShareMusicDialog(currentItem.getComposition().getFilePath());
    }

    void onCompositionItemClicked(int position, PlayQueueItem item) {
        this.currentItem = item;
        musicPlayerInteractor.skipToPosition(position);

        onCurrentCompositionChanged(item, 0);
    }

    void onTrackRewoundTo(int progress) {
        long position = currentItem.getComposition().getDuration() * progress / 100;
        musicPlayerInteractor.seekTo(position);
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        deleteComposition(composition);
    }

    void onDeleteCurrentCompositionButtonClicked() {
        deleteComposition(currentItem.getComposition());
    }

    void onAddToPlayListButtonClicked(Composition composition) {
        compositionToAddToPlayList = composition;
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListToAddingSelected(PlayList playList) {
        addCompositionToPlayList(currentItem.getComposition(), playList);
    }

    void onPlayListForPlayQueueItemSelected(PlayList playList) {
        addCompositionToPlayList(compositionToAddToPlayList, playList);
    }

    void onPlayListForAddingCreated(PlayList playList) {
        List<Composition> compositionsToAdd = mapList(playQueue, PlayQueueItem::getComposition);
        playListsInteractor.addCompositionsToPlayList(compositionsToAdd, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> getViewState().showAddingToPlayListComplete(playList, compositionsToAdd),
                        this::onAddingToPlayListError);
    }

    private void addCompositionToPlayList(Composition composition, PlayList playList) {
        playListsInteractor.addCompositionToPlayList(composition, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList, composition),
                        this::onAddingToPlayListError);
    }

    private void deleteComposition(Composition composition) {
        musicPlayerInteractor.deleteComposition(composition)
                .observeOn(uiScheduler)
                .subscribe();//TODO displayError
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList, Composition composition) {
        getViewState().showAddingToPlayListComplete(playList, asList(composition));
    }

    public void onSeekStart() {
        musicPlayerInteractor.onSeekStarted();
    }

    public void onSeekStop(int progress) {
        long position = currentItem.getComposition().getDuration() * progress / 100;
        musicPlayerInteractor.onSeekFinished(position);
    }

    private void subscribeOnCurrentCompositionChanging() {
        currentCompositionDisposable = musicPlayerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayQueueEventReceived);
        presenterDisposable.add(currentCompositionDisposable);
    }

    private void onPlayQueueEventReceived(PlayQueueEvent playQueueEvent) {
        PlayQueueItem newItem = playQueueEvent.getPlayQueueItem();
        if (!Objects.equals(newItem, currentItem)) {
            onCurrentCompositionChanged(newItem, playQueueEvent.getTrackPosition());
        }
    }

    private void onCurrentCompositionChanged(PlayQueueItem newItem, long trackPosition) {
        this.currentItem = newItem;
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
            trackStateDisposable = null;
        }
        if (newItem != null) {
            Integer position = musicPlayerInteractor.getQueuePosition(newItem);
            if (position != null) {
                getViewState().showCurrentQueueItem(newItem, position);
            }
            getViewState().showTrackState(trackPosition, newItem.getComposition().getDuration());
            subscribeOnTrackPositionChanging();
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
            default: {
                getViewState().showStopState();
            }
        }
    }

    private void subscribeOnPlayQueue() {
        presenterDisposable.add(musicPlayerInteractor.getPlayQueueObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListChanged));
    }

    private void onPlayListChanged(List<PlayQueueItem> newPlayQueue) {
        if (currentCompositionDisposable != null) {
            currentCompositionDisposable.dispose();
            currentCompositionDisposable = null;
            currentItem = null;
        }
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
            trackStateDisposable = null;
        }

        List<PlayQueueItem> oldPlayList = new ArrayList<>(playQueue);
        playQueue.clear();
        playQueue.addAll(newPlayQueue);

        getViewState().updatePlayQueue(oldPlayList, playQueue);
        getViewState().showMusicControls(!playQueue.isEmpty());

        if (!playQueue.isEmpty()) {
            subscribeOnCurrentCompositionChanging();
        }
    }

    private void subscribeOnTrackPositionChanging() {
        trackStateDisposable = musicPlayerInteractor.getTrackPositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onTrackPositionChanged);
    }

    private void onTrackPositionChanged(Long currentPosition) {
        long duration = currentItem.getComposition().getDuration();
        getViewState().showTrackState(currentPosition, duration);
    }
}
