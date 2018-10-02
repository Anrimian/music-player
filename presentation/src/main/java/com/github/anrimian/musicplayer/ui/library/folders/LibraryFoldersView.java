package com.github.anrimian.musicplayer.ui.library.folders;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created on 23.10.2017.
 */

public interface LibraryFoldersView extends MvpView {

    String LIST_STATE = "list_state";
    String BACK_PATH_BUTTON_STATE = "back_path_button_state";

    @StateStrategyType(AddToEndSingleStrategy.class)
    void bindList(List<FileSource> musicList);

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
    void goBackToMusicStorageScreen(String targetPath);

    @StateStrategyType(SkipStrategy.class)
    void updateList(List<FileSource> oldList, List<FileSource> sourceList);

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
}
