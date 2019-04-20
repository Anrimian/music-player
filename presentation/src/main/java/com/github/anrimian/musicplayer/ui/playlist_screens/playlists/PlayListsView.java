package com.github.anrimian.musicplayer.ui.playlist_screens.playlists;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.AddToStartSingleStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;

public interface PlayListsView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(AddToStartSingleStrategy.class)
    void updateList(ListUpdate<PlayList> listUpdate);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showPlayListMenu(PlayList playList);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showConfirmDeletePlayListDialog(PlayList playListToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showPlayListDeleteSuccess(PlayList playListToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeletePlayListError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showEditPlayListNameDialog(PlayList playListInMenu);
}
