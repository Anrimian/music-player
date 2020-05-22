package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.musicplayer.domain.Constants.NO_POSITION;
import static com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper.hasSourceChanges;

public class LibraryPlayerInteractor {

    private final PlayerInteractor musicPlayerInteractor;
    private final SettingsRepository settingsRepository;
    private final PlayQueueRepository playQueueRepository;
    private final LibraryRepository musicProviderRepository;
    private final Analytics analytics;

    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    @Nullable
    private PlayQueueItem currentItem;

    private final Observable<CurrentComposition> currentCompositionObservable;

    public LibraryPlayerInteractor(PlayerInteractor musicPlayerInteractor,
                                   SettingsRepository settingsRepository,
                                   PlayQueueRepository playQueueRepository,
                                   LibraryRepository musicProviderRepository,
                                   Analytics analytics) {
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.settingsRepository = settingsRepository;
        this.playQueueRepository = playQueueRepository;
        this.musicProviderRepository = musicProviderRepository;
        this.analytics = analytics;

        currentCompositionObservable = getCurrentQueueItemObservable()
                .distinctUntilChanged()
                .switchMap(event -> getPlayerStateObservable()
                        .map(state -> state == PlayerState.PLAY)
                        .distinctUntilChanged()
                        .map(state -> new CurrentComposition(event, state)))
                .distinctUntilChanged()
                .replay(1)
                .refCount();

        playerDisposable.add(playQueueRepository.getCurrentQueueItemObservable()
                .subscribe(this::onQueueItemChanged));

        playerDisposable.add(musicPlayerInteractor.getPlayerEventsObservable()
                .subscribe(this::onMusicPlayerEventReceived));
    }

    public void startPlaying(List<Composition> compositions) {
        startPlaying(compositions, NO_POSITION);
    }

    public void startPlaying(List<Composition> compositions, int firstPosition) {
        playQueueRepository.setPlayQueue(compositions, firstPosition)
                .doOnComplete(musicPlayerInteractor::play)
                .doOnSubscribe(o -> musicPlayerInteractor.setInLoadingState())//fixes music gap and state blinking
                .doOnError(analytics::processNonFatalError)
                .onErrorComplete()
                .subscribe();
    }

    public void play() {
        musicPlayerInteractor.play();
    }

    public void playOrPause() {
        musicPlayerInteractor.playOrPause();
    }

    public void stop() {
        musicPlayerInteractor.stop();
    }

    public void pause() {
        musicPlayerInteractor.pause();
    }

    public void skipToPrevious() {
        if (musicPlayerInteractor.getTrackPosition() > settingsRepository.getSkipConstraintMillis()) {
            musicPlayerInteractor.seekTo(0);
            return;
        }
        playQueueRepository.skipToPrevious();
    }

    public void skipToNext() {
        playQueueRepository.skipToNext().subscribe();
    }

    public void skipToItem(PlayQueueItem item) {
        playQueueRepository.skipToItem(item);
    }

    public Observable<Integer> getRepeatModeObservable() {
        return settingsRepository.getRepeatModeObservable();
    }

    public int getRepeatMode() {
        return settingsRepository.getRepeatMode();
    }

    public boolean isRandomPlayingEnabled() {
        return settingsRepository.isRandomPlayingEnabled();
    }

    public Observable<Boolean> getRandomPlayingObservable() {
        return settingsRepository.getRandomPlayingObservable();
    }

    public void setRandomPlayingEnabled(boolean enabled) {
        playQueueRepository.setRandomPlayingEnabled(enabled);
    }

    public void onSeekStarted() {
        musicPlayerInteractor.onSeekStarted();
    }

    public void seekTo(long position) {
        musicPlayerInteractor.seekTo(position);
    }

    public void onSeekFinished(long position) {
        musicPlayerInteractor.onSeekFinished(position);
    }

    public void setRepeatMode(int mode) {
        settingsRepository.setRepeatMode(mode);
    }

    public void changeRepeatMode() {
        switch (settingsRepository.getRepeatMode()) {
            case RepeatMode.NONE: {
                settingsRepository.setRepeatMode(RepeatMode.REPEAT_PLAY_LIST);
                break;
            }
            case RepeatMode.REPEAT_PLAY_LIST: {
                settingsRepository.setRepeatMode(RepeatMode.REPEAT_COMPOSITION);
                break;
            }
            case RepeatMode.REPEAT_COMPOSITION: {
                settingsRepository.setRepeatMode(RepeatMode.NONE);
                break;
            }
        }
    }

    public Observable<Long> getTrackPositionObservable() {
        return musicPlayerInteractor.getTrackPositionObservable();
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return musicPlayerInteractor.getPlayerStateObservable();
    }

    public PlayerState getPlayerState() {
        return musicPlayerInteractor.getPlayerState();
    }

    public Observable<PlayQueueEvent> getCurrentQueueItemObservable() {
        return playQueueRepository.getCurrentQueueItemObservable();
    }

    public Observable<CurrentComposition> getCurrentCompositionObservable() {
        return currentCompositionObservable;
    }

    public Flowable<Integer> getCurrentItemPositionObservable() {
        return playQueueRepository.getCurrentItemPositionObservable();
    }

    public Flowable<List<PlayQueueItem>> getPlayQueueObservable() {
        return playQueueRepository.getPlayQueueObservable();
    }

    public Completable deleteComposition(Composition composition) {
        return musicProviderRepository.deleteComposition(composition);
    }

    public Completable deleteCompositions(List<Composition> compositions) {
        return musicProviderRepository.deleteCompositions(compositions);
    }

    public Completable removeQueueItem(PlayQueueItem item) {
        return playQueueRepository.removeQueueItem(item);
    }

    public Completable restoreDeletedItem() {
        return playQueueRepository.restoreDeletedItem();
    }

    public void swapItems(PlayQueueItem firstItem,
                          PlayQueueItem secondItem) {
        playQueueRepository.swapItems(firstItem, secondItem).subscribe();
    }

    public Single<List<Composition>> addCompositionsToPlayNext(List<Composition> compositions) {
        return playQueueRepository.addCompositionsToPlayNext(compositions)
                .toSingleDefault(compositions);
    }

    public Single<List<Composition>> addCompositionsToEnd(List<Composition> compositions) {
        return playQueueRepository.addCompositionsToEnd(compositions)
                .toSingleDefault(compositions);
    }

    public void clearPlayQueue() {
        playQueueRepository.clearPlayQueue();
    }

    private void onQueueItemChanged(PlayQueueEvent compositionEvent) {
        PlayQueueItem previousItem = currentItem;
        this.currentItem = compositionEvent.getPlayQueueItem();
        if (currentItem == null) {
            if (previousItem != null) {
                stop();
            }
        } else {
            long trackPosition = compositionEvent.getTrackPosition();

            //if items are equal and content changed -> restart play
            if (previousItem != null && previousItem.equals(currentItem)) {
                if (!hasSourceChanges(previousItem, currentItem)) {
                    return;
                }
                trackPosition = musicPlayerInteractor.getTrackPosition();
            }

            musicPlayerInteractor.prepareToPlay(
                    new LibraryCompositionSource(currentItem.getComposition()),
                    trackPosition
            );
        }
    }

    private void onMusicPlayerEventReceived(PlayerEvent playerEvent) {
        if (playerEvent instanceof FinishedEvent) {
            FinishedEvent finishedEvent = (FinishedEvent) playerEvent;
            onCompositionPlayFinished();
            CompositionSource compositionSource = finishedEvent.getComposition();
            if (compositionSource instanceof LibraryCompositionSource) {
                Composition composition = ((LibraryCompositionSource) compositionSource).getComposition();
                if (composition.getCorruptionType() != null) {
                    musicProviderRepository.writeErrorAboutComposition(null, composition)
                            .doOnError(analytics::processNonFatalError)
                            .onErrorComplete()
                            .subscribe();
                }
            }
        } else if (playerEvent instanceof ErrorEvent) {
            ErrorEvent errorEvent = (ErrorEvent) playerEvent;
            handleErrorWithComposition(errorEvent.getErrorType(), errorEvent.getComposition());
        }
    }

    private void handleErrorWithComposition(ErrorType errorType, CompositionSource compositionSource) {
        if (!(compositionSource instanceof LibraryCompositionSource) || errorType == ErrorType.IGNORED) {
            return;
        }
        Composition composition = ((LibraryCompositionSource) compositionSource).getComposition();
        CorruptionType corruptionType = toCorruptionType(errorType);
        musicProviderRepository.writeErrorAboutComposition(corruptionType, composition)
                .doOnError(analytics::processNonFatalError)
                .onErrorComplete()
                .andThen(playQueueRepository.isCurrentCompositionAtEndOfQueue())
                .doOnSuccess(isLast -> {
                    if (isLast) {
                        stop();
                    } else {
                        playQueueRepository.skipToNext().subscribe();
                    }
                })
                .subscribe();
    }

    private CorruptionType toCorruptionType(ErrorType errorType) {
        switch (errorType) {
            case UNSUPPORTED: return CorruptionType.UNSUPPORTED;
            case NOT_FOUND: return CorruptionType.NOT_FOUND;
            case UNKNOWN: return CorruptionType.UNKNOWN;
            default: return null;
        }
    }

    private void onCompositionPlayFinished() {
        if (settingsRepository.getRepeatMode() == RepeatMode.REPEAT_COMPOSITION) {
            musicPlayerInteractor.onSeekFinished(0);
            return;
        }
        playQueueRepository.skipToNext()
                .doOnSuccess(this::onAutoSkipNextFinished)
                .subscribe();
    }

    private void onAutoSkipNextFinished(int currentPosition) {
        if (currentPosition == 0 && !(settingsRepository.getRepeatMode() == RepeatMode.REPEAT_PLAY_LIST)) {
            stop();
        }
    }
}
