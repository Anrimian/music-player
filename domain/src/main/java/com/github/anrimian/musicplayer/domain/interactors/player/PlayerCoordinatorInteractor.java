package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class PlayerCoordinatorInteractor {

    private final PlayerInteractor playerInteractor;
    private final UiStateRepository uiStateRepository;

    private final HashMap<PlayerType, CompositionSource> preparedSourcesMap = new HashMap<>();
    private PlayerType activePlayerType = PlayerType.LIBRARY;

    private final BehaviorSubject<PlayerType> activePlayerTypeSubject = BehaviorSubject.createDefault(activePlayerType);

    public PlayerCoordinatorInteractor(PlayerInteractor playerInteractor,
                                       UiStateRepository uiStateRepository) {
        this.playerInteractor = playerInteractor;
        this.uiStateRepository = uiStateRepository;

        initializePlayerType(activePlayerType);
    }

    public void startPlaying(CompositionSource compositionSource, PlayerType playerType) {
        applyPlayerType(playerType);
        playerInteractor.startPlaying(compositionSource);
    }

    public void play(PlayerType playerType, int delay) {
        applyPlayerType(playerType);
        playerInteractor.play(delay);
    }

    public void setInLoadingState(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.setInLoadingState();
        }
    }

    public void updateSource(CompositionSource source, PlayerType playerType) {
        CompositionSource currentSource = preparedSourcesMap.get(playerType);
        if (source.equals(currentSource)) {
            preparedSourcesMap.put(playerType, source);
        }
        if (playerType == activePlayerType) {
            playerInteractor.updateSource(source);
        }
    }

    public void playOrPause(PlayerType playerType) {
        applyPlayerType(playerType);
        playerInteractor.playOrPause();
    }

    public void stop(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.stop();
        }
    }

    public void pause(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.pause();
        }
    }

    public void reset(PlayerType playerType) {
        preparedSourcesMap.remove(playerType);
        if (playerType == activePlayerType) {
            playerInteractor.reset();
        }
    }

    public void fastSeekForward(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.fastSeekForward();
        } //else update and save position
    }

    public void fastSeekBackward(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.fastSeekBackward();
        } //else update and save position
    }

    public void prepareToPlay(CompositionSource compositionSource, PlayerType playerType) {
        preparedSourcesMap.put(playerType, compositionSource);
        if (playerType == activePlayerType) {
            playerInteractor.prepareToPlay(compositionSource);
        }
    }

    public void onSeekStarted(PlayerType playerType) {
        if (activePlayerType == playerType) {
            playerInteractor.onSeekStarted();
        }
    }

    public boolean onSeekFinished(long position, PlayerType playerType) {
        if (activePlayerType == playerType) {
            playerInteractor.onSeekFinished(position);
            return true;
        } else {
            CompositionSource source = preparedSourcesMap.get(playerType);
            if (source != null) {
                applyPositionChange(source, position);
            }
            return false;
        }
    }

    public Single<Long> getActualTrackPosition(PlayerType playerType) {
        return isPlayerTypeActive(playerType)? playerInteractor.getTrackPosition(): Single.just(-1L);
    }

    public void setPlaybackSpeed(float speed, PlayerType playerType) {
        if (activePlayerType == playerType) {
            playerInteractor.setPlaybackSpeed(speed);
        }
    }

    public Observable<PlayerEvent> getPlayerEventsObservable(PlayerType playerType) {
        return playerInteractor.getPlayerEventsObservable()
                .filter(o -> isPlayerTypeActive(playerType));
    }

    public Observable<Long> getTrackPositionObservable(PlayerType playerType) {
        return playerInteractor.getTrackPositionObservable()
                .filter(o -> isPlayerTypeActive(playerType));
    }

    public Observable<PlayerState> getPlayerStateObservable(PlayerType playerType) {
        return playerInteractor.getPlayerStateObservable()
                .map(state -> isPlayerTypeActive(playerType)? state : PlayerState.PAUSE);
    }

    public boolean isPlayerTypeActive(PlayerType playerType) {
        return activePlayerType == playerType;
    }

    public PlayerState getPlayerState(PlayerType playerType) {
        return isPlayerTypeActive(playerType)? playerInteractor.getPlayerState() : PlayerState.PAUSE;
    }

    public Observable<Boolean> getSpeedChangeAvailableObservable() {
        return playerInteractor.getSpeedChangeAvailableObservable();
    }

    public Observable<PlayerType> getActivePlayerTypeObservable() {
        return activePlayerTypeSubject;
    }

    private void applyPlayerType(PlayerType playerType) {
        if (activePlayerType != playerType) {
            playerInteractor.pause();
            CompositionSource source = preparedSourcesMap.get(playerType);
            if (source != null) {
                playerInteractor.prepareToPlay(source);
            }
            CompositionSource oldSource = preparedSourcesMap.get(activePlayerType);
            if (oldSource != null) {
                //noinspection ResultOfMethodCallIgnored
                playerInteractor.getTrackPosition()
                        .subscribe(position -> applyPositionChange(oldSource, position));
            }

            //not only here
            initializePlayerType(playerType);
            activePlayerType = playerType;
            activePlayerTypeSubject.onNext(activePlayerType);
        }
    }

    private void initializePlayerType(PlayerType playerType) {
        switch (playerType) {
            case LIBRARY: {
                playerInteractor.setPlaybackSpeed(uiStateRepository.getCurrentPlaybackSpeed());
                break;
            }
            case EXTERNAL: {
                playerInteractor.setPlaybackSpeed(1f);

            }
        }
    }

    private void applyPositionChange(CompositionSource source, long position) {
        if (source instanceof LibraryCompositionSource) {
            ((LibraryCompositionSource) source).setTrackPosition(position);
        }
    }
}
