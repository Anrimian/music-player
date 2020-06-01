package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;

import java.util.HashMap;

import io.reactivex.Observable;

public class PlayerCoordinatorInteractor {

    private final PlayerInteractor playerInteractor;

    private PlayerType activePlayerType = PlayerType.LIBRARY;
    private HashMap<PlayerType, CompositionSource> preparedSourcesMap = new HashMap<>();

    //set and unset active state - ok
    //filter player state - ok
    //filter seekbar state - ok
    //play from inactive player - ok

    //"skip to from" inactive player - ok, but position is blinking
    //"seek to" inactive player - ok, and save position

    //save position only for library
    //filter player events
    //sound blinks
    //skip to previous - processed wrong position
    public PlayerCoordinatorInteractor(PlayerInteractor playerInteractor) {
        this.playerInteractor = playerInteractor;
    }

    public void startPlaying(CompositionSource compositionSource, PlayerType playerType) {
        if (activePlayerType != playerType) {
            playerInteractor.pause();
        }
        activePlayerType = playerType;
        playerInteractor.startPlaying(compositionSource);
    }

    public void play(PlayerType playerType) {
        if (activePlayerType != playerType) {
            playerInteractor.pause();
            CompositionSource source = preparedSourcesMap.get(playerType);
            if (source != null) {
                playerInteractor.prepareToPlay(source);
            }
        }
        activePlayerType = playerType;
        playerInteractor.play();
    }

    public void playOrPause(PlayerType playerType) {
        if (activePlayerType != playerType) {
            playerInteractor.pause();
            CompositionSource source = preparedSourcesMap.get(playerType);
            if (source != null) {
                playerInteractor.prepareToPlay(source);
            }
        }
        activePlayerType = playerType;
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

    //blink sound from previous composition
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

    public void onSeekFinished(long position, PlayerType playerType) {
        if (activePlayerType == playerType) {
            playerInteractor.onSeekFinished(position);
        } else {
            CompositionSource source = preparedSourcesMap.get(playerType);
            if (source != null) {
                applyPositionChange(source, position);
            }
        }
    }

    public boolean processPreviousCommand(PlayerType playerType) {
        return isPlayerTypeActive(playerType) && playerInteractor.processPreviousCommand();
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

    private void applyPositionChange(CompositionSource source, long position) {
        if (source instanceof LibraryCompositionSource) {
            ((LibraryCompositionSource) source).setTrackPosition(position);
        }
    }
}
