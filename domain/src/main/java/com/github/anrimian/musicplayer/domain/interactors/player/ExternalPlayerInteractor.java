package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.interactors.player.PlayerType.EXTERNAL;

public class ExternalPlayerInteractor {

    private final PlayerCoordinatorInteractor playerCoordinatorInteractor;
    private final SettingsRepository settingsRepository;

    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    private final PublishSubject<Long> trackPositionSubject = PublishSubject.create();
    private final PublishSubject<ErrorType> playErrorSubject = PublishSubject.create();
    private final BehaviorSubject<Float> playbackSpeedSubject = BehaviorSubject.createDefault(1f);

    @Nullable
    private CompositionSource currentSource;

    private boolean onPlayPrepareAgain = false;

    public ExternalPlayerInteractor(PlayerCoordinatorInteractor playerCoordinatorInteractor,
                                    SettingsRepository settingsRepository) {
        this.playerCoordinatorInteractor = playerCoordinatorInteractor;
        this.settingsRepository = settingsRepository;

        playerDisposable.add(playerCoordinatorInteractor.getPlayerEventsObservable(EXTERNAL)
                .subscribe(this::onMusicPlayerEventReceived));
    }

    public void startPlaying(CompositionSource source) {
        this.currentSource = source;
        onPlayPrepareAgain = false;
        setPlaybackSpeed(1f);
        playerCoordinatorInteractor.startPlaying(source, EXTERNAL);
    }

    public void playOrPause() {
        if (onPlayPrepareAgain && currentSource != null) {
            startPlaying(currentSource);
        } else {
            playerCoordinatorInteractor.playOrPause(EXTERNAL);
        }
    }

    public void stop() {
        playerCoordinatorInteractor.stop(EXTERNAL);
    }

    public void onSeekStarted() {
        playerCoordinatorInteractor.onSeekStarted(EXTERNAL);
    }

    public void seekTo(long position) {
        trackPositionSubject.onNext(position);
    }

    public void onSeekFinished(long position) {
        boolean processed = playerCoordinatorInteractor.onSeekFinished(position, EXTERNAL);
        if (!processed) {
            trackPositionSubject.onNext(position);
        }
    }

    public void changeExternalPlayerRepeatMode() {
        if (settingsRepository.getExternalPlayerRepeatMode() == RepeatMode.NONE) {
            settingsRepository.setExternalPlayerRepeatMode(RepeatMode.REPEAT_COMPOSITION);
        } else {
            settingsRepository.setExternalPlayerRepeatMode(RepeatMode.NONE);
        }
    }

    public void setExternalPlayerRepeatMode(int mode) {
        //not supported
        if (mode == RepeatMode.REPEAT_PLAY_LIST) {
            return;
        }
        settingsRepository.setExternalPlayerRepeatMode(mode);
    }

    public void fastSeekForward() {
        playerCoordinatorInteractor.fastSeekForward(EXTERNAL);
    }

    public void fastSeekBackward() {
        playerCoordinatorInteractor.fastSeekBackward(EXTERNAL);
    }

    public Observable<Integer> getExternalPlayerRepeatModeObservable() {
        return settingsRepository.getExternalPlayerRepeatModeObservable();
    }

    public void setExternalPlayerKeepInBackground(boolean enabled) {
        settingsRepository.setExternalPlayerKeepInBackground(enabled);
    }

    public boolean isExternalPlayerKeepInBackground() {
        return settingsRepository.isExternalPlayerKeepInBackground();
    }

    public void setPlaybackSpeed(float speed) {
        playerCoordinatorInteractor.setPlaybackSpeed(speed, EXTERNAL);
        playbackSpeedSubject.onNext(speed);
    }

    public Observable<Float> getPlaybackSpeedObservable() {
        return playbackSpeedSubject;
    }

    public Observable<Long> getTrackPositionObservable() {
        return playerCoordinatorInteractor.getTrackPositionObservable(EXTERNAL)
                .mergeWith(trackPositionSubject);
    }

    public Observable<Boolean> getSpeedChangeAvailableObservable() {
        return playerCoordinatorInteractor.getSpeedChangeAvailableObservable();
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return playerCoordinatorInteractor.getPlayerStateObservable(EXTERNAL);
    }

    public Observable<ErrorType> getErrorEventsObservable() {
        return playErrorSubject;
    }

    private void onMusicPlayerEventReceived(PlayerEvent playerEvent) {
        if (playerEvent instanceof FinishedEvent) {
            onSeekFinished(0);
            if (settingsRepository.getExternalPlayerRepeatMode() != RepeatMode.REPEAT_COMPOSITION) {
                playerCoordinatorInteractor.pause(EXTERNAL);
            }
        } else if (playerEvent instanceof ErrorEvent) {
            ErrorEvent errorEvent = (ErrorEvent) playerEvent;
            ErrorType errorType = errorEvent.getErrorType();

            onPlayPrepareAgain = true;
            stop();
            playErrorSubject.onNext(errorType);
        }
    }


}
