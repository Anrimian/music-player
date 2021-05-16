package com.github.anrimian.musicplayer.ui.main.external_player;

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;

import javax.annotation.Nullable;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;
public interface ExternalPlayerView extends MvpView {

    @AddToEndSingle
    void showPlayerState(PlayerState state);

    @AddToEndSingle
    void displayComposition(UriCompositionSource source);

    @AddToEndSingle
    void showTrackState(long currentPosition, long duration);

    @AddToEndSingle
    void showRepeatMode(int mode);

    @AddToEndSingle
    void showPlayErrorEvent(@Nullable ErrorType errorType);

    @AddToEndSingle
    void showKeepPlayerInBackground(boolean externalPlayerKeepInBackground);

    @AddToEndSingle
    void displayPlaybackSpeed(float speed);

    @AddToEndSingle
    void showSpeedChangeFeatureVisible(boolean visible);
}
