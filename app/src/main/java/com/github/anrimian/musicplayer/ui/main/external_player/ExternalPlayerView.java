package com.github.anrimian.musicplayer.ui.main.external_player;

import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import javax.annotation.Nullable;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;
public interface ExternalPlayerView extends MvpView {

    @AddToEndSingle
    void showPlayerState(boolean isPlaying);

    @AddToEndSingle
    void displayComposition(ExternalCompositionSource source);

    @AddToEndSingle
    void showTrackState(long currentPosition, long duration);

    @AddToEndSingle
    void showRepeatMode(int mode);

    @AddToEndSingle
    void showPlayErrorState(@Nullable ErrorCommand errorCommand);

    @AddToEndSingle
    void showKeepPlayerInBackground(boolean externalPlayerKeepInBackground);

    @AddToEndSingle
    void displayPlaybackSpeed(float speed);

    @AddToEndSingle
    void showSpeedChangeFeatureVisible(boolean visible);
}
