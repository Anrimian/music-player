package com.github.anrimian.musicplayer.ui.editor.composition;

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;
import moxy.viewstate.strategy.alias.Skip;

public interface CompositionEditorView extends MvpView {

    String DISPLAY_COMPOSITION_STATE = "display_composition_state";
    String CHANGE_FILE_STATE = "change_file_state";

    @OneExecution
    void closeScreen();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_COMPOSITION_STATE)
    void showCompositionLoadingError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_COMPOSITION_STATE)
    void showComposition(FullComposition composition);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_FILE_STATE)
    void showChangeFileProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_FILE_STATE)
    void hideChangeFileProgress();

    @OneExecution
    void showErrorMessage(ErrorCommand errorCommand);

    @OneExecution
    void showCheckTagsErrorMessage(ErrorCommand errorCommand);

    @Skip
    void showEnterAuthorDialog(FullComposition composition, String[] hints);

    @Skip
    void showEnterTitleDialog(FullComposition composition);

    @Skip
    void showEnterFileNameDialog(FullComposition composition);

    @Skip
    void copyFileNameText(String filePath);

    @Skip
    void showEnterAlbumDialog(FullComposition composition, String[] hints);

    @Skip
    void showEnterAlbumArtistDialog(FullComposition composition, String[] hints);

    @Skip
    void showEnterLyricsDialog(FullComposition composition);

    @Skip
    void showAddGenreDialog(String[] genres);

    @Skip
    void showEditGenreDialog(ShortGenre shortGenre, String[] genres);

    @AddToEndSingle
    void showGenres(List<ShortGenre> shortGenres);

    @OneExecution
    void showRemovedGenreMessage(ShortGenre genre);

    @Skip
    void showCoverActionsDialog();

    @Skip
    void showSelectImageFromGalleryScreen();
}
