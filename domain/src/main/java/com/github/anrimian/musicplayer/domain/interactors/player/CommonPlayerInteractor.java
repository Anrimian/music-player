package com.github.anrimian.musicplayer.domain.interactors.player;

import static com.github.anrimian.musicplayer.domain.interactors.player.PlayerType.EXTERNAL;
import static com.github.anrimian.musicplayer.domain.interactors.player.PlayerType.LIBRARY;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class CommonPlayerInteractor {

    private final PlayerCoordinatorInteractor playerCoordinatorInteractor;
    private final LibraryPlayerInteractor libraryPlayerInteractor;
    private final ExternalPlayerInteractor externalPlayerInteractor;
    private final PlayerInteractor playerInteractor;

    public CommonPlayerInteractor(PlayerCoordinatorInteractor playerCoordinatorInteractor,
                                  LibraryPlayerInteractor libraryPlayerInteractor,
                                  ExternalPlayerInteractor externalPlayerInteractor,
                                  PlayerInteractor playerInteractor) {
        this.playerCoordinatorInteractor = playerCoordinatorInteractor;
        this.libraryPlayerInteractor = libraryPlayerInteractor;
        this.externalPlayerInteractor = externalPlayerInteractor;
        this.playerInteractor = playerInteractor;
    }

    public void prepare() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(EXTERNAL)) {
            return;
        }
        libraryPlayerInteractor.prepare();
    }

    public void play(long delay, @Nullable PlayerType forcePlayerType) {
        if (playerCoordinatorInteractor.isPlayerTypeActive(EXTERNAL)
                && forcePlayerType != LIBRARY) {
            externalPlayerInteractor.play(delay);
        } else {
            libraryPlayerInteractor.play(delay);
        }
    }
    public void skipToNext() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.skipToNext();
        }
    }

    public void skipToPrevious() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.skipToPrevious();
        }
    }

    public void changeRandomMode() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.changeRandomMode();
        }
    }

    public void setRepeatMode(int appRepeatMode) {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.setRepeatMode(appRepeatMode);
        } else {
            externalPlayerInteractor.setExternalPlayerRepeatMode(appRepeatMode);
        }
    }

    public void changeRepeatMode() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.changeRepeatMode();
        } else {
            externalPlayerInteractor.changeExternalPlayerRepeatMode();
        }
    }

    public void setRandomPlayingEnabled(boolean isEnabled) {
        libraryPlayerInteractor.setRandomPlayingEnabled(isEnabled);
    }

    public void setPlaybackSpeed(float speed) {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.setPlaybackSpeed(speed);
            return;
        }
        if (playerCoordinatorInteractor.isPlayerTypeActive(EXTERNAL)) {
            externalPlayerInteractor.setPlaybackSpeed(speed);
        }
    }

    public void seekTo(long position) {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.onSeekFinished(position);
            return;
        }
        if (playerCoordinatorInteractor.isPlayerTypeActive(EXTERNAL)) {
            externalPlayerInteractor.onSeekFinished(position);
        }
    }

    public void fastSeekBackward() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.fastSeekBackward();
            return;
        }
        if (playerCoordinatorInteractor.isPlayerTypeActive(EXTERNAL)) {
            externalPlayerInteractor.fastSeekBackward();
        }
    }

    public void fastSeekForward() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(LIBRARY)) {
            libraryPlayerInteractor.fastSeekForward();
            return;
        }
        if (playerCoordinatorInteractor.isPlayerTypeActive(EXTERNAL)) {
            externalPlayerInteractor.fastSeekForward();
        }
    }

    public void reset() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(EXTERNAL)) {
            externalPlayerInteractor.reset();
        }
    }

    public Observable<Long> getTrackPositionChangeObservable() {
        return Observable.merge(
                playerInteractor.getIsPlayingStateObservable(),
                playerInteractor.getCurrentSourceObservable(),
                playerInteractor.getTrackPositionChangeObservable()
        ).flatMapSingle(o -> getTrackPosition());
    }

    public Single<Long> getTrackPosition() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(EXTERNAL)) {
            return externalPlayerInteractor.getTrackPositionObservable().first(0L);
        }
        return libraryPlayerInteractor.getTrackPositionObservable().first(0L);
    }

    public Observable<Float> getPlaybackSpeedObservable() {
        return playerCoordinatorInteractor.getActivePlayerTypeObservable()
                .switchMap(playerType -> {
                    switch (playerType) {
                        case LIBRARY: {
                            return libraryPlayerInteractor.getPlaybackSpeedObservable();
                        }
                        case EXTERNAL: {
                            return externalPlayerInteractor.getPlaybackSpeedObservable();
                        }
                        default: throw new IllegalStateException();
                    }
                });
    }

    public Observable<Integer> getRepeatModeObservable() {
        return playerCoordinatorInteractor.getActivePlayerTypeObservable()
                .switchMap(playerType -> {
                    switch (playerType) {
                        case LIBRARY: {
                            return libraryPlayerInteractor.getRepeatModeObservable();
                        }
                        case EXTERNAL: {
                            return externalPlayerInteractor.getExternalPlayerRepeatModeObservable();
                        }
                        default: throw new IllegalStateException();
                    }
                });
    }

    public Observable<Boolean> getRandomModeObservable() {
        return playerCoordinatorInteractor.getActivePlayerTypeObservable()
                .switchMap(playerType -> {
                    switch (playerType) {
                        case LIBRARY: {
                            return libraryPlayerInteractor.getRandomPlayingObservable();
                        }
                        case EXTERNAL: {
                            return Observable.fromCallable(() -> false);
                        }
                        default: throw new IllegalStateException();
                    }
                });
    }

}
