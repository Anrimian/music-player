package com.github.anrimian.musicplayer.ui.player_screen;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.player.PlayerScreenInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

/**
 * Created on 02.11.2017.
 */

@InjectViewState
public class PlayerPresenter extends MvpPresenter<PlayerView> {

    private final MusicPlayerInteractor musicPlayerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final PlayerScreenInteractor playerScreenInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable trackStateDisposable;

    private final List<PlayQueueItem> playQueue = new ArrayList<>();
    private PlayQueueItem currentItem;

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    public PlayerPresenter(MusicPlayerInteractor musicPlayerInteractor,
                           PlayListsInteractor playListsInteractor,
                           PlayerScreenInteractor playerScreenInteractor,
                           ErrorParser errorParser,
                           Scheduler uiScheduler) {
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.playListsInteractor = playListsInteractor;
        this.playerScreenInteractor = playerScreenInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().bindPlayList(playQueue);
        getViewState().showInfinitePlayingButton(musicPlayerInteractor.isInfinitePlayingEnabled());
        getViewState().showRandomPlayingButton(musicPlayerInteractor.isRandomPlayingEnabled());
        if (playerScreenInteractor.isPlayerPanelOpen()) {
            getViewState().expandBottomPanel();
        } else {
            getViewState().collapseBottomPanel();
        }
        getViewState().showDrawerScreen(playerScreenInteractor.getSelectedDrawerScreen());
    }

    void onStart() {//TODO unnecessary scroll to position
        subscribeOnPlayerStateChanges();
        subscribeOnPlayQueue();
        subscribeOnCurrentCompositionChanging();
    }

    void onStop() {
        presenterDisposable.clear();
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
        }
    }

    void onBottomPanelExpanded() {
        playerScreenInteractor.setPlayerPanelOpen(true);
        getViewState().expandBottomPanel();
    }

    void onBottomPanelCollapsed() {
        playerScreenInteractor.setPlayerPanelOpen(false);
        getViewState().collapseBottomPanel();
    }

    void onDrawerScreenSelected(int screenId) {
        playerScreenInteractor.setSelectedDrawerScreen(screenId);
        getViewState().showDrawerScreen(screenId);
    }

    void onLibraryScreenSelected() {
        getViewState().showLibraryScreen(playerScreenInteractor.getSelectedLibraryScreen());
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

    void onInfiniteButtonClicked(boolean enable) {
        musicPlayerInteractor.setInfinitePlayingEnabled(enable);
        getViewState().showInfinitePlayingButton(enable);
    }

    void onRandomPlayingButtonClicked(boolean enable) {
        musicPlayerInteractor.setRandomPlayingEnabled(enable);
        getViewState().showRandomPlayingButton(enable);
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
        compositionsToDelete.clear();
        compositionsToDelete.add(composition);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onDeleteCurrentCompositionButtonClicked() {
        compositionsToDelete.clear();
        compositionsToDelete.add(currentItem.getComposition());
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onAddQueueItemToPlayListButtonClicked(Composition composition) {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(composition);
        getViewState().showSelectPlayListDialog();
    }

    void onAddCurrentCompositionToPlayListButtonClicked() {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(currentItem.getComposition());
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListForAddingSelected(PlayList playList) {
        addPreparedCompositionsToPlayList(playList);
    }

    void onPlayListForAddingCreated(PlayList playList) {
        List<Composition> compositionsToAdd = mapList(playQueue, PlayQueueItem::getComposition);
        playListsInteractor.addCompositionsToPlayList(compositionsToAdd, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> getViewState().showAddingToPlayListComplete(playList, compositionsToAdd),
                        this::onAddingToPlayListError);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    void onSeekStart() {
        musicPlayerInteractor.onSeekStarted();
    }

    void onSeekStop(int progress) {
        long position = currentItem.getComposition().getDuration() * progress / 100;
        musicPlayerInteractor.onSeekFinished(position);
    }

    private void addPreparedCompositionsToPlayList(PlayList playList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList),
                        this::onAddingToPlayListError);
    }

    private void deletePreparedCompositions() {
        musicPlayerInteractor.deleteCompositions(compositionsToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionError);
    }

    private void onDeleteCompositionsSuccess() {
        getViewState().showDeleteCompositionMessage(compositionsToDelete);
        compositionsToDelete.clear();
    }

    private void onDeleteCompositionError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositionsForPlayList);
        compositionsForPlayList.clear();
    }

    private void subscribeOnCurrentCompositionChanging() {
        presenterDisposable.add(musicPlayerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayQueueEventReceived));
    }

    private void onPlayQueueEventReceived(PlayQueueEvent playQueueEvent) {
        PlayQueueItem newItem = playQueueEvent.getPlayQueueItem();
        if (currentItem == null || !currentItem.equals(newItem)) {
            onCurrentCompositionChanged(newItem, playQueueEvent.getTrackPosition());
        }
    }

    private void onCurrentCompositionChanged(PlayQueueItem newItem, long trackPosition) {
        this.currentItem = newItem;
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
            trackStateDisposable = null;
        }
        getViewState().showMusicControls(currentItem != null);
        if (newItem != null) {
            getViewState().showCurrentQueueItem(newItem);
            getViewState().showTrackState(trackPosition, newItem.getComposition().getDuration());
            subscribeOnTrackPositionChanging();

            Integer position = musicPlayerInteractor.getQueuePosition(newItem);
            if (position != null) {
                getViewState().scrollQueueToPosition(position);
            }
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
        getViewState().showPlayQueueSubtitle(newPlayQueue.size());

        List<PlayQueueItem> oldPlayList = new ArrayList<>(playQueue);
        playQueue.clear();
        playQueue.addAll(newPlayQueue);

        getViewState().updatePlayQueue(oldPlayList, playQueue);

        Integer position = musicPlayerInteractor.getQueuePosition(currentItem);
        if (position != null) {
            getViewState().scrollQueueToPosition(position);
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
