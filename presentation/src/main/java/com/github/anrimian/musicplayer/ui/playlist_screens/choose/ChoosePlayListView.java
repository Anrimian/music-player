package com.github.anrimian.musicplayer.ui.playlist_screens.choose;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;

public interface ChoosePlayListView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @AddToEndSingle
    void updateList(List<PlayList> list);

    @AddToEndSingle
    void showBottomSheetSlided(float slideOffset);

    @OneExecution
    void showPlayListMenu(PlayList playList);

    @OneExecution
    void showConfirmDeletePlayListDialog(PlayList playListToDelete);

    @OneExecution
    void showPlayListDeleteSuccess(PlayList playListToDelete);

    @OneExecution
    void showDeletePlayListError(ErrorCommand errorCommand);

    @OneExecution
    void showEditPlayListNameDialog(PlayList playListInMenu);
}
