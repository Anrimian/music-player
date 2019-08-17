package com.github.anrimian.musicplayer.ui.library.folders;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.ListStateStrategyStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created on 23.10.2017.
 */

public interface LibraryFoldersView extends MvpView {

    String LIST_STATE = "list_state";
    String BACK_PATH_BUTTON_STATE = "back_path_button_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptySearchResult();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = BACK_PATH_BUTTON_STATE)
    void showBackPathButton(@Nonnull String path);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = BACK_PATH_BUTTON_STATE)
    void hideBackPathButton();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void goBackToParentFolderScreen();

    @StateStrategyType(ListStateStrategyStrategy.class)
    void updateList(List<FileSource> update);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectOrderScreen(Order folderOrder);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectPlayListDialog();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectPlayListForFolderDialog();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showConfirmDeleteDialog(List<Composition> compositionsToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteCompositionError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteCompositionMessage(List<Composition> compositionsToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showConfirmDeleteDialog(FolderFileSource folder);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSearchMode(boolean show);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void sendCompositions(List<String> paths);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showReceiveCompositionsForSendError(ErrorCommand errorCommand);

    @StateStrategyType(SkipStrategy.class)
    void goToMusicStorageScreen(String path);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCurrentPlayingComposition(Composition composition);

    @StateStrategyType(SkipStrategy.class)
    void showCompositionActionDialog(Composition composition);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showQueueActions(boolean show);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setDisplayCoversEnabled(boolean isCoversEnabled);

    @StateStrategyType(SkipStrategy.class)
    void showInputFolderNameDialog(String path);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSelectionMode(int count);

    @StateStrategyType(SkipStrategy.class)
    void onItemSelected(FileSource item, int position);

    @StateStrategyType(SkipStrategy.class)
    void onItemUnselected(FileSource item, int position);

    @StateStrategyType(SkipStrategy.class)
    void setItemsSelected(boolean selected);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void updateMoveFilesList();
}
