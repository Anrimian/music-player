package com.github.anrimian.musicplayer.domain.business.player;

import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.Constants.NO_POSITION;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSED_EXTERNALLY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.STOP;
import static com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper.hasSourceChanges;
import static io.reactivex.subjects.BehaviorSubject.createDefault;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractor {

    private final MusicPlayerController musicPlayerController;
    private final SystemMusicController systemMusicController;
    private final SystemServiceController systemServiceController;
    private final SettingsRepository settingsRepository;
    private final PlayQueueRepository playQueueRepository;
    private final MusicProviderRepository musicProviderRepository;
    private final Analytics analytics;

    private final PublishSubject<Long> trackPositionSubject = PublishSubject.create();
    private final BehaviorSubject<PlayerState> playerStateSubject = createDefault(IDLE);

    private final CompositeDisposable systemEventsDisposable = new CompositeDisposable();
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    @Nullable
    private PlayQueueItem currentItem;

    public MusicPlayerInteractor(MusicPlayerController musicPlayerController,
                                 SettingsRepository settingsRepository,
                                 SystemMusicController systemMusicController,
                                 SystemServiceController systemServiceController,
                                 PlayQueueRepository playQueueRepository,
                                 MusicProviderRepository musicProviderRepository,
                                 Analytics analytics) {
        this.musicPlayerController = musicPlayerController;
        this.systemMusicController = systemMusicController;
        this.settingsRepository = settingsRepository;
        this.systemServiceController = systemServiceController;
        this.playQueueRepository = playQueueRepository;
        this.musicProviderRepository = musicProviderRepository;
        this.analytics = analytics;

        playerDisposable.add(playQueueRepository.getCurrentQueueItemObservable()
                .subscribe(this::onQueueItemChanged));
        playerDisposable.add(musicPlayerController.getEventsObservable()
                .subscribe(this::onMusicPlayerEventReceived));
        playerDisposable.add(systemMusicController.getVolumeObservable()
                .subscribe(this::onVolumeChanged));
    }

    public void startPlaying(List<Composition> compositions) {
        startPlaying(compositions, NO_POSITION);
    }

    public void startPlaying(List<Composition> compositions, int firstPosition) {
        playQueueRepository.setPlayQueue(compositions, firstPosition)
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .doOnError(t -> playerStateSubject.onNext(STOP))
                .doOnComplete(this::play)
                .doOnError(analytics::processNonFatalError)
                .onErrorComplete()
                .subscribe();
    }

    public void play() {
        if (playerStateSubject.getValue() == PLAY || playerStateSubject.getValue() == STOP) {
            return;
        }

        Observable<AudioFocusEvent> audioFocusObservable = systemMusicController.requestAudioFocus();
        if (audioFocusObservable != null) {
            playerStateSubject.onNext(PLAY);
            musicPlayerController.resume();
            systemServiceController.startMusicService();

            systemEventsDisposable.clear();
            systemEventsDisposable.add(audioFocusObservable.subscribe(this::onAudioFocusChanged));
            systemEventsDisposable.add(systemMusicController.getAudioBecomingNoisyObservable()
                    .subscribe(this::onAudioBecomingNoisy));
        }
    }

    public void playOrPause() {
        if (playerStateSubject.getValue() == PLAY) {
            pause();
        } else {
            play();
        }
    }

    /**
     * Use it only with empty play queue as final result
     */
    public void stop() {
        musicPlayerController.stop();
        playerStateSubject.onNext(STOP);
        systemEventsDisposable.clear();
    }

    public void pause() {
        musicPlayerController.pause();
        playerStateSubject.onNext(PAUSE);
        systemEventsDisposable.clear();
    }

    public void skipToPrevious() {
        if (musicPlayerController.getTrackPosition() > settingsRepository.getSkipConstraintMillis()) {
            musicPlayerController.seekTo(0);
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
        if (playerStateSubject.getValue() == PLAY) {
            musicPlayerController.pause();
        }
    }

    public void seekTo(long position) {
        trackPositionSubject.onNext(position);
    }

    public void onSeekFinished(long position) {
        if (playerStateSubject.getValue() == PLAY) {
            musicPlayerController.resume();
        }
        musicPlayerController.seekTo(position);
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
        return musicPlayerController.getTrackPositionObservable()
                .mergeWith(trackPositionSubject);
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject.map(PlayerState::toBaseState)
                .distinctUntilChanged();
    }

    public PlayerState getPlayerState() {
        return playerStateSubject.getValue();
    }

    public Observable<PlayQueueEvent> getCurrentCompositionObservable() {
        return playQueueRepository.getCurrentQueueItemObservable();
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

    public void swapItems(PlayQueueItem firstItem,
                          int firstPosition,
                          PlayQueueItem secondItem,
                          int secondPosition) {
        playQueueRepository.swapItems(firstItem, firstPosition, secondItem, secondPosition)
                .subscribe();
    }

    public Completable addCompositionsToPlayNext(List<Composition> compositions) {
        return playQueueRepository.addCompositionsToPlayNext(compositions);
    }

    public Completable addCompositionsToEnd(List<Composition> compositions) {
        return playQueueRepository.addCompositionsToEnd(compositions);
    }

    private void onQueueItemChanged(PlayQueueEvent compositionEvent) {
        PlayQueueItem previousItem = currentItem;
        this.currentItem = compositionEvent.getPlayQueueItem();
        if (currentItem == null) {
            pause();
        } else {
            long trackPosition = compositionEvent.getTrackPosition();

            //if items are equal and content changed -> restart play
            if (previousItem != null && previousItem.equals(currentItem)) {
                if (!hasSourceChanges(previousItem, currentItem)) {
                    return;
                }
                trackPosition = musicPlayerController.getTrackPosition();
            }

            musicPlayerController.prepareToPlay(currentItem.getComposition(), trackPosition);
        }
    }

    private void onMusicPlayerEventReceived(PlayerEvent playerEvent) {
        if (playerEvent instanceof PreparedEvent) {
            onCompositionPrepared();
        } else if (playerEvent instanceof FinishedEvent) {
            FinishedEvent finishedEvent = (FinishedEvent) playerEvent;
            onCompositionPlayFinished();
            Composition composition = finishedEvent.getComposition();
            if (composition.getCorruptionType() != null) {
                musicProviderRepository.writeErrorAboutComposition(null, composition)
                        .doOnError(analytics::processNonFatalError)
                        .onErrorComplete()
                        .subscribe();
            }
        } else if (playerEvent instanceof ErrorEvent) {
            ErrorEvent errorEvent = (ErrorEvent) playerEvent;
            handleErrorWithComposition(errorEvent.getErrorType(), errorEvent.getComposition());
        }
    }

    private void onVolumeChanged(int volume) {
        if (playerStateSubject.getValue() == PLAY && volume == 0) {
            pause();
        }
    }

    private void onCompositionPrepared() {
        if (playerStateSubject.getValue() == PLAY) {
            musicPlayerController.resume();
        }
    }

    private void handleErrorWithComposition(ErrorType errorType, Composition composition) {
        CorruptionType corruptionType = toCorruptionType(errorType);
        musicProviderRepository.writeErrorAboutComposition(corruptionType, composition)
                .doOnError(analytics::processNonFatalError)
                .onErrorComplete()
                .doOnComplete(() -> {
                    if (playQueueRepository.getCurrentPosition() >= playQueueRepository.getQueueSize() - 1) {//mm, no!
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
            musicPlayerController.seekTo(0);
            return;
        }
        playQueueRepository.skipToNext()
                .doOnSuccess(this::onAutoSkipNextFinished)
                .subscribe();
    }

    private void onAutoSkipNextFinished(int currentPosition) {
        if (currentPosition == 0 && !(settingsRepository.getRepeatMode() == RepeatMode.REPEAT_PLAY_LIST)) {
            pause();
        }
    }

    private void onAudioFocusChanged(AudioFocusEvent event) {
        switch (event) {
            case GAIN: {
                musicPlayerController.setVolume(1f);
                if (playerStateSubject.getValue() == PAUSED_EXTERNALLY) {
                    playerStateSubject.onNext(PLAY);
                    musicPlayerController.resume();
                    systemServiceController.startMusicService();
                }
                break;
            }
            case LOSS_SHORTLY: {
                if (playerStateSubject.getValue() == PLAY
                        && settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()) {
                    musicPlayerController.setVolume(0.5f);
                }
                break;
            }
            case LOSS: {
                if (playerStateSubject.getValue() == PLAY) {
                    musicPlayerController.pause();
                    playerStateSubject.onNext(PAUSED_EXTERNALLY);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void onAudioBecomingNoisy(Object o) {
        musicPlayerController.pause();
        playerStateSubject.onNext(PAUSE);
    }
}
