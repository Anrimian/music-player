package com.github.anrimian.simplemusicplayer.ui.drawer;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

/**
 * Created on 02.11.2017.
 */

public interface PlayerView extends MvpView {

    String PLAYER_STATE = "player_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showStopState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showPlayState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void hideMusicControls();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCurrentComposition(Composition composition);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void bindPlayList(List<Composition> currentPlayList);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void updatePlayList();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showInfinitePlayingButton(boolean active);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showRandomPlayingButton(boolean active);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showTrackState(long currentPosition, long duration);
}
