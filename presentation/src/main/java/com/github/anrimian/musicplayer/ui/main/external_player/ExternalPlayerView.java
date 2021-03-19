package com.github.anrimian.musicplayer.ui.main.external_player;

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import javax.annotation.Nullable;

import moxy.MvpView;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.AddToEndSingle;
public interface ExternalPlayerView extends MvpView {

    String PLAYER_STATE = "player_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showStopState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showPlayState();

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
