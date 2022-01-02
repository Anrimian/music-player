package com.github.anrimian.musicplayer.ui.editor.album;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import moxy.MvpView;
import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.OneExecution;
import moxy.viewstate.strategy.alias.Skip;

public interface AlbumEditorView extends MvpView {

    String DISPLAY_ALBUM_STATE = "display_album_state";
    String CHANGE_STATE = "change_state";

    @OneExecution
    void closeScreen();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_ALBUM_STATE)
    void showAlbumLoadingError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_ALBUM_STATE)
    void showAlbum(Album album);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_STATE)
    void showRenameProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_STATE)
    void hideRenameProgress();

    @OneExecution
    void showErrorMessage(ErrorCommand errorCommand);

    @Skip
    void showEnterAuthorDialog(Album album, String[] hints);

    @Skip
    void showEnterNameDialog(Album album);

}
