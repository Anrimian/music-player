package com.github.anrimian.musicplayer.domain.business.player;

import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
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
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSED_EXTERNALLY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.STOP;
import static io.reactivex.subjects.BehaviorSubject.createDefault;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractor {

    private final MusicPlayerController musicPlayerController;
    private final SystemMusicController systemMusicController;
    private final SettingsRepository settingsRepository;
    private final PlayQueueRepository playQueueRepository;
    private final MusicProviderRepository musicProviderRepository;
    private final Analytics analytics;
    private final PlayerErrorParser playerErrorParser;

    private final PublishSubject<Long> trackPositionSubject = PublishSubject.create();
    private final BehaviorSubject<PlayerState> playerStateSubject = createDefault(IDLE);

    private final CompositeDisposable systemEventsDisposable = new CompositeDisposable();
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    private Disposable skipDisposable;

    private PlayQueueItem currentItem;

    public MusicPlayerInteractor(MusicPlayerController musicPlayerController,
                                 SettingsRepository settingsRepository,
                                 SystemMusicController systemMusicController,
                                 PlayQueueRepository playQueueRepository,
                                 MusicProviderRepository musicProviderRepository,
                                 Analytics analytics,
                                 PlayerErrorParser playerErrorParser) {
        this.musicPlayerController = musicPlayerController;
        this.systemMusicController = systemMusicController;
        this.settingsRepository = settingsRepository;
        this.playQueueRepository = playQueueRepository;
        this.musicProviderRepository = musicProviderRepository;
        this.analytics = analytics;
        this.playerErrorParser = playerErrorParser;

        playerDisposable.add(playQueueRepository.getCurrentQueueItemObservable()
                .subscribe(this::onQueueItemChanged));
        playerDisposable.add(musicPlayerController.getEventsObservable()
                .subscribe(this::onMusicPlayerEventReceived));
        playerDisposable.add(systemMusicController.getVolumeObservable()
                .subscribe(this::onVolumeChanged));
    }

    public void startPlaying(List<Composition> compositions) {
        startPlaying(compositions, 0);
    }

    public void startPlaying(List<Composition> compositions, int firstPosition) {
        playQueueRepository.setPlayQueue(compositions, firstPosition)
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .doOnError(t -> playerStateSubject.onNext(STOP))
                .doOnComplete(this::play)
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

            if (systemEventsDisposable.size() == 0) {
                systemEventsDisposable.add(audioFocusObservable
                        .subscribe(this::onAudioFocusChanged));

                systemEventsDisposable.add(systemMusicController.getAudioBecomingNoisyObservable()
                        .subscribe(this::onAudioBecomingNoisy));
            }
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

        if (skipDisposable == null) {
            skipDisposable = playQueueRepository.skipToPrevious()
                    .doFinally(() -> skipDisposable = null)
                    .subscribe();
        }
    }

    public void skipToNext() {
        if (skipDisposable == null) {
            skipDisposable = playQueueRepository.skipToNext()
                    .doFinally(() -> skipDisposable = null)
                    .subscribe();
        }
    }

    public void skipToPosition(int position) {
        if (skipDisposable == null) {
            skipDisposable = playQueueRepository.skipToPosition(position)
                    .doFinally(() -> skipDisposable = null)
                    .subscribe();
        }
    }

    public Observable<Integer> getRepeatModeObservable() {
        return settingsRepository.getRepeatModeObservable();
    }

    public boolean isRandomPlayingEnabled() {
        return settingsRepository.isRandomPlayingEnabled();
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

    public Observable<Long> getTrackPositionObservable() {
        return musicPlayerController.getTrackPositionObservable()
                .mergeWith(trackPositionSubject);
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject.map(PlayerState::toBaseState)
                .distinctUntilChanged();
    }

    public Observable<PlayQueueEvent> getCurrentCompositionObservable() {
        return playQueueRepository.getCurrentQueueItemObservable();
    }

    public Observable<List<PlayQueueItem>> getPlayQueueObservable() {
        return playQueueRepository.getPlayQueueObservable();
    }

    public Completable deleteComposition(Composition composition) {
        return musicProviderRepository.deleteComposition(composition);
    }

    public Completable deleteCompositions(List<Composition> compositions) {
        return musicProviderRepository.deleteCompositions(compositions);
    }

    @Nullable
    public Integer getQueuePosition(PlayQueueItem item) {
        return playQueueRepository.getCompositionPosition(item);
    }

    public Completable removeQueueItem(PlayQueueItem item) {
        return playQueueRepository.removeQueueItem(item);
    }

    private void onQueueItemChanged(PlayQueueEvent compositionEvent) {
        this.currentItem = compositionEvent.getPlayQueueItem();
        if (currentItem == null) {
            stop();
        } else {
            musicPlayerController.prepareToPlay(currentItem.getComposition(), compositionEvent.getTrackPosition());
        }
    }

    private void onMusicPlayerEventReceived(PlayerEvent playerEvent) {
        if (playerEvent instanceof PreparedEvent) {
            onCompositionPrepared();
        } else if (playerEvent instanceof FinishedEvent) {
            onCompositionPlayFinished();
        } else if (playerEvent instanceof ErrorEvent) {
            ErrorEvent errorEvent = (ErrorEvent) playerEvent;
            handleErrorWithComposition(playerErrorParser.getErrorType(errorEvent.getThrowable()));
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

    private void handleErrorWithComposition(ErrorType errorType) {
        switch (errorType) {
            case DELETED: {
                musicProviderRepository.deleteComposition(currentItem.getComposition())
                        .doOnError(analytics::processNonFatalError)
                        .onErrorComplete()
                        .subscribe();
                break;
            }
            default: {
                musicProviderRepository.writeErrorAboutComposition(errorType, currentItem.getComposition())
                        .doOnError(analytics::processNonFatalError)
                        .onErrorComplete()
                        .andThen(playQueueRepository.skipToNext())
                        .doOnSuccess(currentPosition -> {
                            if (currentPosition == 0) {
                                stop();
                            }
                        })
                        .subscribe();
            }
        }
    }

    private void onCompositionPlayFinished() {
        if (settingsRepository.getRepeatMode() == RepeatMode.REPEAT_COMPOSITION) {
            musicPlayerController.seekTo(0);
            return;
        }

        if (skipDisposable == null) {
            skipDisposable = playQueueRepository.skipToNext()
                    .doFinally(() -> skipDisposable = null)
                    .subscribe(currentPosition -> {
                        if (currentPosition == 0 && !(settingsRepository.getRepeatMode() == RepeatMode.REPEAT_PLAY_LIST)) {
                            pause();
                        }
                    });
        }
    }

    private void onAudioFocusChanged(AudioFocusEvent event) {
        switch (event) {
            case GAIN: {
                musicPlayerController.setVolume(1f);
                if (playerStateSubject.getValue() == PAUSED_EXTERNALLY) {
                    musicPlayerController.resume();
                    playerStateSubject.onNext(PLAY);
                }
                break;
            }
            case LOSS_SHORTLY: {
                if (playerStateSubject.getValue() == PLAY) {
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
