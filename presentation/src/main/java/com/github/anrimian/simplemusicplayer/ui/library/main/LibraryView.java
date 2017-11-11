package com.github.anrimian.simplemusicplayer.ui.library.main;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.simplemusicplayer.utils.moxy.SingleStateByTagStrategy;

/**
 * Created on 02.11.2017.
 */

public interface LibraryView extends MvpView {

    String PLAYER_STATE = "player_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showStopState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showPlayState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void hideMusicControls();
}
