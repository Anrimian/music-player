package com.github.anrimian.musicplayer.ui.start;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

/**
 * Created on 19.10.2017.
 */

interface StartView extends MvpView {

    String SCREEN_STATE = "screen_state";

    @StateStrategyType(OneExecutionStateStrategy.class)
    void requestFilesPermissions();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void goToMainScreen();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = SCREEN_STATE)
    void showDeniedPermissionMessage();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = SCREEN_STATE)
    void showStub();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void startSystemUi();
}
