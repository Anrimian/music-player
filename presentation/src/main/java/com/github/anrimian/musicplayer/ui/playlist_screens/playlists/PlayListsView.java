package com.github.anrimian.musicplayer.ui.playlist_screens.playlists;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.ListStateStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.OneExecution;

public interface PlayListsView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(ListStateStrategy.class)
    void updateList(List<PlayList> lists);

    @OneExecution
    void showPlayListMenu(PlayList playList);

    @OneExecution
    void showConfirmDeletePlayListDialog(PlayList playList);

    @OneExecution
    void showPlayListDeleteSuccess(PlayList playListToDelete);

    @OneExecution
    void showDeletePlayListError(ErrorCommand errorCommand);

    @OneExecution
    void showEditPlayListNameDialog(PlayList playListInMenu);

    @OneExecution
    void restoreListPosition(ListPosition listPosition);
}
