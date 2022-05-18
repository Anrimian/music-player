package com.github.anrimian.musicplayer.ui.playlist_screens.create;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface CreatePlayListView extends MvpView {

    String CREATE_STATE = "create_state";

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = CREATE_STATE)
    void showProgress();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = CREATE_STATE)
    void showInputState();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = CREATE_STATE)
    void showError(ErrorCommand errorCommand);

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = CREATE_STATE)
    void onPlayListCreated(PlayList playList);
}
