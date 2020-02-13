package com.github.anrimian.musicplayer.ui.library.folders;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.ListStateStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

/**
 * Created on 23.10.2017.
 */

public interface LibraryFoldersView extends MvpView {

    String LIST_STATE = "list_state";
    String FOLDER_STATE = "back_path_button_state";

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

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = FOLDER_STATE)
    void showFolderInfo(FolderFileSource2 folder);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = FOLDER_STATE)
    void hideFolderInfo();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void goBackToParentFolderScreen();

    @StateStrategyType(ListStateStrategy.class)
    void updateList(List<FileSource2> update);

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
    void goToMusicStorageScreen(Long folderId);

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

    @StateStrategyType(SkipStrategy.class)
    void updateMoveFilesList();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showMoveFileMenu(boolean show);

    @StateStrategyType(SkipStrategy.class)
    void showInputNewFolderNameDialog();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showPlayState(boolean play);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddedIgnoredFolderMessage(IgnoredFolder folder);
}
