package com.github.anrimian.musicplayer.ui.settings.folders;

import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;

public interface ExcludedFoldersView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showListState();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showEmptyListState();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showErrorState(ErrorCommand errorCommand);

    @AddToEndSingle
    void showExcludedFoldersList(List<IgnoredFolder> folders);

    @OneExecution
    void showRemovedFolderMessage(IgnoredFolder folder);

    @OneExecution
    void showErrorMessage(ErrorCommand errorCommand);
}
