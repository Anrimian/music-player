package com.github.anrimian.musicplayer.ui.library.folders.root;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

public interface FolderRootView extends MvpView {

    String LOADING_STATE = "loading_state";

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showFolderScreens(List<String> paths);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LOADING_STATE)
    void showProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LOADING_STATE)
    void showError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LOADING_STATE)
    void showIdle();

}
