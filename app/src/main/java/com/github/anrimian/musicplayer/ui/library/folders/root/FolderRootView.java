package com.github.anrimian.musicplayer.ui.library.folders.root;

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.OneExecution;

public interface FolderRootView extends MvpView {

    String LOADING_STATE = "loading_state";

    @OneExecution
    void showFolderScreens(List<Long> ids);

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LOADING_STATE)
    void showProgress();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LOADING_STATE)
    void showError(ErrorCommand errorCommand);

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LOADING_STATE)
    void showIdle();

}
