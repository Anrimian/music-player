package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.interactors.player.PlayerType.EXTERNAL;

public class ExternalPlayerInteractor {

    private final PlayerCoordinatorInteractor playerCoordinatorInteractor;
    private final SettingsRepository settingsRepository;

    private final PublishSubject<Long> trackPositionSubject = PublishSubject.create();

    public ExternalPlayerInteractor(PlayerCoordinatorInteractor playerCoordinatorInteractor,
                                    SettingsRepository settingsRepository) {
        this.playerCoordinatorInteractor = playerCoordinatorInteractor;
        this.settingsRepository = settingsRepository;
    }

    public void startPlaying(CompositionSource source) {
        playerCoordinatorInteractor.startPlaying(source, EXTERNAL);
    }

    public void playOrPause() {
        playerCoordinatorInteractor.playOrPause(EXTERNAL);
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

    public Observable<Integer> getExternalPlayerRepeatModeObservable() {
        return settingsRepository.getExternalPlayerRepeatModeObservable();
    }

    public void setExternalPlayerKeepInBackground(boolean enabled) {
        settingsRepository.setExternalPlayerKeepInBackground(enabled);
    }

    public boolean isExternalPlayerKeepInBackground() {
        return settingsRepository.isExternalPlayerKeepInBackground();
    }

    public Observable<Long> getTrackPositionObservable() {
        return playerCoordinatorInteractor.getTrackPositionObservable(EXTERNAL)
                .mergeWith(trackPositionSubject);
    }

    public Observable<PlayerState> getPlayPauseObservable() {
        return playerCoordinatorInteractor.getPlayerStateObservable(EXTERNAL);
    }
}
