package com.github.anrimian.simplemusicplayer.ui.playlist_screens.create;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.ui.utils.moxy.SingleStateByTagStrategy;

public interface CreatePlayListView extends MvpView {

    String CREATE_STATE = "create_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CREATE_STATE)
    void showProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CREATE_STATE)
    void showInputState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CREATE_STATE)
    void showError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CREATE_STATE)
    void onPlayListCreated(PlayList playList);
}
