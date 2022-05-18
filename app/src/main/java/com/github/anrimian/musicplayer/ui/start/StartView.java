package com.github.anrimian.musicplayer.ui.start;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.OneExecution;

/**
 * Created on 19.10.2017.
 */

public interface StartView extends MvpView {

    String SCREEN_STATE = "screen_state";

    @OneExecution
    void requestFilesPermissions();

    @OneExecution
    void goToMainScreen();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = SCREEN_STATE)
    void showDeniedPermissionMessage();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = SCREEN_STATE)
    void showStub();

    @OneExecution
    void startSystemServices();
}
