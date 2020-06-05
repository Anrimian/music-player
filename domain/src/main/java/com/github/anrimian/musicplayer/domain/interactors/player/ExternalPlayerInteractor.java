package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.interactors.player.PlayerType.EXTERNAL;

public class ExternalPlayerInteractor {

    private final PlayerCoordinatorInteractor playerCoordinatorInteractor;

    private final PublishSubject<Long> trackPositionSubject = PublishSubject.create();

    public ExternalPlayerInteractor(PlayerCoordinatorInteractor playerCoordinatorInteractor) {
        this.playerCoordinatorInteractor = playerCoordinatorInteractor;
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

    public Observable<Long> getTrackPositionObservable() {
        return playerCoordinatorInteractor.getTrackPositionObservable(EXTERNAL)
                .mergeWith(trackPositionSubject);
    }

    public Observable<PlayerState> getPlayPauseObservable() {
        return playerCoordinatorInteractor.getPlayerStateObservable(EXTERNAL);
    }
}
