package com.github.anrimian.musicplayer.data.utils.exo_player;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

public class PlayerEventListener implements Player.EventListener {

    private final Runnable onEnded;
    private final Callback<ExoPlaybackException> errorCallback;

    public PlayerEventListener(Runnable onEnded, Callback<ExoPlaybackException> errorCallback) {
        this.onEnded = onEnded;
        this.errorCallback = errorCallback;
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
                onEnded.run();
                break;
            }
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        errorCallback.call(error);
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
