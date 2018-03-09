package com.github.anrimian.simplemusicplayer.ui.storage_library_screen;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created on 23.10.2017.
 */

interface StorageLibraryView extends MvpView {

    String LIST_STATE = "list_state";
    String BACK_PATH_BUTTON_STATE = "back_path_button_state";

    @StateStrategyType(AddToEndSingleStrategy.class)
    void bindList(List<FileSource> musicList);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

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

    @StateStrategyType(OneExecutionStateStrategy.class)
    void notifyItemsLoaded(int start, int size);
}
