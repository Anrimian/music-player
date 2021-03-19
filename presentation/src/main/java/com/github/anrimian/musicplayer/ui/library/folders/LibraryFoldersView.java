package com.github.anrimian.musicplayer.ui.library.folders;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
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

/**
 * Created on 23.10.2017.
 */

public interface LibraryFoldersView extends MvpView {

    String LIST_STATE = "list_state";
    String FOLDER_STATE = "back_path_button_state";
    String PROGRESS_DIALOG_STATE = "progress_dialog_state";

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
    void showFolderInfo(FolderFileSource folder);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = FOLDER_STATE)
    void hideFolderInfo();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PROGRESS_DIALOG_STATE)
    void hideProgressDialog();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PROGRESS_DIALOG_STATE)
    void showMoveProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PROGRESS_DIALOG_STATE)
    void showDeleteProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PROGRESS_DIALOG_STATE)
    void showRenameProgress();

    @OneExecution
    void goBackToParentFolderScreen();

    @StateStrategyType(ListStateStrategy.class)
    void updateList(List<FileSource> update);

    @OneExecution
    void showSelectOrderScreen(Order folderOrder);

    @OneExecution
    void showSelectPlayListDialog();

    @OneExecution
    void showAddingToPlayListError(ErrorCommand errorCommand);

    @OneExecution
    void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions);

    @OneExecution
    void showSelectPlayListForFolderDialog(FolderFileSource folder);

    @OneExecution
    void showConfirmDeleteDialog(List<Composition> compositionsToDelete);

    @OneExecution
    void showDeleteCompositionError(ErrorCommand errorCommand);

    @OneExecution
    void showDeleteCompositionMessage(List<Composition> compositionsToDelete);

    @OneExecution
    void showConfirmDeleteDialog(FolderFileSource folder);

    @AddToEndSingle
    void showSearchMode(boolean show);

    @OneExecution
    void sendCompositions(List<Composition> compositions);

    @OneExecution
    void showReceiveCompositionsForSendError(ErrorCommand errorCommand);

    @Skip
    void goToMusicStorageScreen(Long folderId);

    @Skip
    void showCompositionActionDialog(Composition composition);

    @OneExecution
    void showErrorMessage(ErrorCommand errorCommand);

    @AddToEndSingle
    void setDisplayCoversEnabled(boolean isCoversEnabled);

    @Skip
    void showInputFolderNameDialog(FolderFileSource folder);

    @AddToEndSingle
    void showSelectionMode(int count);

    @Skip
    void onItemSelected(FileSource item, int position);

    @Skip
    void onItemUnselected(FileSource item, int position);

    @Skip
    void setItemsSelected(boolean selected);

    @Skip
    void updateMoveFilesList();

    @AddToEndSingle
    void showMoveFileMenu(boolean show);

    @Skip
    void showInputNewFolderNameDialog();

    @OneExecution
    void showAddedIgnoredFolderMessage(IgnoredFolder folder);

    @OneExecution
    void onCompositionsAddedToPlayNext(List<Composition> compositions);

    @OneExecution
    void onCompositionsAddedToQueue(List<Composition> compositions);

    @AddToEndSingle
    void showCurrentComposition(CurrentComposition currentComposition);

    @OneExecution
    void restoreListPosition(ListPosition listPosition);
}
