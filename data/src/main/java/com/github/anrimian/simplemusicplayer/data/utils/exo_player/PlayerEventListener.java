package com.github.anrimian.simplemusicplayer.data.utils.exo_player;

import com.github.anrimian.simplemusicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.PlayerEvent;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import io.reactivex.subjects.PublishSubject;

public class PlayerEventListener implements Player.EventListener {

    private final PublishSubject<PlayerEvent> subject;

    public PlayerEventListener(PublishSubject<PlayerEvent> subject) {
        this.subject = subject;
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
//                subject.onNext(BUFFERING);
                break;
            }
            case Player.STATE_IDLE: {
//                subject.onNext(IDLE);
                break;
            }
            case Player.STATE_READY: {
//                subject.onNext(READY);
                break;
            }
            case Player.STATE_ENDED: {
                subject.onNext(new FinishedEvent());
                break;
            }
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        subject.onNext(new ErrorEvent(error));
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
