package com.github.anrimian.simplemusicplayer.utils.exo_player;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.utils.exo_player.ExoPlayerState.BUFFERING;
import static com.github.anrimian.simplemusicplayer.utils.exo_player.ExoPlayerState.ENDED;
import static com.github.anrimian.simplemusicplayer.utils.exo_player.ExoPlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.utils.exo_player.ExoPlayerState.READY;

/**
 * Created on 08.11.2017.
 */

public class PlayerStateRxWrapper implements Player.EventListener {

    private BehaviorSubject<ExoPlayerState> subject = BehaviorSubject.create();

    public Observable<ExoPlayerState> getStateObservable() {
        return subject;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING: {
                subject.onNext(BUFFERING);
                break;
            }
            case Player.STATE_IDLE: {
                subject.onNext(IDLE);
                break;
            }
            case Player.STATE_READY: {
                subject.onNext(READY);
                break;
            }
            case Player.STATE_ENDED: {
                subject.onNext(ENDED);
                break;
            }
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
