package com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

public interface PlayListsView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void bindList(List<PlayList> playLists);

    @StateStrategyType(SkipStrategy.class)
    void updateList(List<PlayList> oldList, List<PlayList> newList);
}
