package com.github.anrimian.simplemusicplayer.data.utils.exo_player.callback;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerCallback;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import javax.annotation.Nullable;

/**
 * Created on 17.04.2018.
 */
public class CallbackPlayerListener implements Player.EventListener {

    @Nullable
    private MusicPlayerCallback musicPlayerCallback;

    public void setMusicPlayerCallback(@Nullable MusicPlayerCallback musicPlayerCallback) {
        this.musicPlayerCallback = musicPlayerCallback;
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
        if (musicPlayerCallback != null) {
            switch (playbackState) {
                case Player.STATE_BUFFERING: {

                    break;
                }
                case Player.STATE_IDLE: {

                    break;
                }
                case Player.STATE_READY: {

                    break;
                }
                case Player.STATE_ENDED: {
                    musicPlayerCallback.onFinished();
                    break;
                }
            }
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (musicPlayerCallback != null) {
            musicPlayerCallback.onError(error);
        }
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
