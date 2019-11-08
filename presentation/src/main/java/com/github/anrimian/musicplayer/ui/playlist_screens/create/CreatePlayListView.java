package com.github.anrimian.musicplayer.ui.playlist_screens.create;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import moxy.MvpView;
import moxy.viewstate.strategy.StateStrategyType;

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
