package com.github.anrimian.musicplayer.ui.playlist_screens.rename;

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface RenamePlayListView extends MvpView {

    String CHANGE_STATE = "change_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_STATE)
    void showProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_STATE)
    void showInputState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_STATE)
    void showError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showPlayListName(String initialName);
}
