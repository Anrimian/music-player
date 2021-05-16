package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.ListStateStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;
import moxy.viewstate.strategy.alias.Skip;

public interface PlayListView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(ListStateStrategy.class)
    void updateItemsList(List<PlayListItem> list);

    @OneExecution
    void showConfirmDeleteDialog(List<Composition> compositionsToDelete);

    @OneExecution
    void closeScreen();

    @AddToEndSingle
    void showPlayListInfo(PlayList playList);

    @OneExecution
    void showDeleteCompositionError(ErrorCommand errorCommand);

    @OneExecution
    void showDeletedCompositionMessage(List<Composition> compositionsToDelete);

    @OneExecution
    void showSelectPlayListDialog();

    @OneExecution
    void showAddingToPlayListError(ErrorCommand errorCommand);

    @OneExecution
    void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions);

    @OneExecution
    void showDeleteItemError(ErrorCommand errorCommand);

    @OneExecution
    void showDeleteItemCompleted(PlayList playList, List<PlayListItem> items);

    @OneExecution
    void showConfirmDeletePlayListDialog(PlayList playListToDelete);

    @OneExecution
    void showPlayListDeleteSuccess(PlayList playListToDelete);

    @OneExecution
    void showDeletePlayListError(ErrorCommand errorCommand);

    @Skip
    void notifyItemMoved(int from, int to);

    @Skip
    void showCompositionActionDialog(PlayListItem playListItem, int position);

    @OneExecution
    void showErrorMessage(ErrorCommand errorCommand);

    @OneExecution
    void notifyItemRemoved(int position);

    @OneExecution
    void showEditPlayListNameDialog(PlayList playListInMenu);

    @OneExecution
    void onCompositionsAddedToPlayNext(List<Composition> compositions);

    @OneExecution
    void onCompositionsAddedToQueue(List<Composition> compositions);

    @OneExecution
    void restoreListPosition(ListPosition listPosition);
}
