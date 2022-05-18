package com.github.anrimian.musicplayer.ui.common.mvp;

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.AddToEndSingle;

public interface ListMvpView<T> extends MvpView {
    
    String LIST_STATE = "list_state";

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showEmptySearchResult();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showLoadingError(ErrorCommand errorCommand);

    @AddToEndSingle
    void updateList(List<T> list);
}
