package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.simplemusicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PAUSED_EXTERNALLY;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;
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

    private final BehaviorSubject<PlayerState> playerStateSubject = createDefault(IDLE);
    private final CompositeDisposable systemEventsDisposable = new CompositeDisposable();
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    public MusicPlayerInteractor(MusicPlayerController musicPlayerController,
                                 SettingsRepository settingsRepository,
                                 SystemMusicController systemMusicController,
                                 PlayQueueRepository playQueueRepository,
                                 MusicProviderRepository musicProviderRepository) {
        this.musicPlayerController = musicPlayerController;
        this.systemMusicController = systemMusicController;
        this.settingsRepository = settingsRepository;
        this.playQueueRepository = playQueueRepository;
        this.musicProviderRepository = musicProviderRepository;
    }

    public Completable startPlaying(List<Composition> compositions) {//TODO bug with stopping
        return playQueueRepository.setPlayQueue(compositions)
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .doOnError(t -> playerStateSubject.onNext(STOP))
                .doOnComplete(this::play);
    }

    public void play() {
        if (playerStateSubject.getValue() == PLAY) {
            return;
        }

        Observable<AudioFocusEvent> audioFocusObservable = systemMusicController.requestAudioFocus();
        if (audioFocusObservable != null) {
            playerStateSubject.onNext(PLAY);

            if (playerDisposable.size() == 0) {
                playerDisposable.add(playQueueRepository.getCurrentCompositionObservable()
                        .doOnNext(musicPlayerController::prepareToPlayIgnoreError)
                        .subscribe(this::onCompositionPrepared));
                playerDisposable.add(musicPlayerController.getEventsObservable()
                        .subscribe(this::onMusicPlayerEventReceived));
            } else {
                musicPlayerController.resume();
            }

            if (systemEventsDisposable.size() == 0) {
                systemEventsDisposable.add(audioFocusObservable
                        .subscribe(this::onAudioFocusChanged));

                systemEventsDisposable.add(systemMusicController.getAudioBecomingNoisyObservable()
                        .subscribe(this::onAudioBecomingNoisy));
            }
        }
    }

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
        playQueueRepository.skipToPrevious();
    }

    public void skipToNext() {
        playQueueRepository.skipToNext();
    }

    public void skipToPosition(int position) {
        playQueueRepository.skipToPosition(position);
    }

    public boolean isInfinitePlayingEnabled() {
        return settingsRepository.isInfinitePlayingEnabled();
    }

    public boolean isRandomPlayingEnabled() {
        return settingsRepository.isRandomPlayingEnabled();
    }

    public void setRandomPlayingEnabled(boolean enabled) {
        playQueueRepository.setRandomPlayingEnabled(enabled);
    }

    public void setInfinitePlayingEnabled(boolean enabled) {
        settingsRepository.setInfinitePlayingEnabled(enabled);
    }

    public Observable<Long> getTrackPositionObservable() {
        return musicPlayerController.getTrackPositionObservable();
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject.map(PlayerState::toBaseState)
                .distinctUntilChanged();
    }

    public Observable<CurrentComposition> getCurrentCompositionObservable() {
        return playQueueRepository.getCurrentCompositionObservable();
    }

    public Observable<List<Composition>> getPlayQueueObservable() {
        return playQueueRepository.getPlayQueueObservable();
    }

    private void onCompositionPrepared(CurrentComposition currentComposition) {
        if (playerStateSubject.getValue() == PLAY) {
            musicPlayerController.resume();
        }
    }

    private void onMusicPlayerEventReceived(PlayerEvent playerEvent) {
        if (playerEvent instanceof FinishedEvent) {
            onCompositionPlayFinished();
        } else if (playerEvent instanceof ErrorEvent) {
            writeErrorAboutCurrentComposition(((ErrorEvent) playerEvent).getThrowable());
            int currentPosition = playQueueRepository.skipToNext();
            if (currentPosition == 0) {
                stop();
            } else {
                musicPlayerController.resume();
            }
        }
    }

    private void writeErrorAboutCurrentComposition(Throwable throwable) {
        playQueueRepository.getCurrentComposition()
                .map(CurrentComposition::getComposition)
                .flatMapCompletable(composition ->
                        musicProviderRepository.onErrorWithComposition(throwable, composition))
                .subscribe();
    }

    private void onCompositionPlayFinished() {
        int currentPosition = playQueueRepository.skipToNext();
        if (currentPosition != 0 || settingsRepository.isInfinitePlayingEnabled() ) {
            musicPlayerController.resume();
        } else {
            stop();
        }
    }

    private void onAudioFocusChanged(AudioFocusEvent event) {
        switch (event) {
            case GAIN: {
                if (playerStateSubject.getValue() == PAUSED_EXTERNALLY) {
                    musicPlayerController.resume();
                    playerStateSubject.onNext(PLAY);
                }
                break;
            }
            case LOSS_SHORTLY:
            case LOSS: {
                musicPlayerController.pause();
                playerStateSubject.onNext(PAUSED_EXTERNALLY);
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    private void onAudioBecomingNoisy(Object o) {
        musicPlayerController.pause();
        playerStateSubject.onNext(PAUSE);
    }
}
