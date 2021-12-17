package com.github.anrimian.musicplayer.ui.library.albums.items;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView;

import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;
import moxy.viewstate.strategy.alias.Skip;

public interface AlbumItemsView extends BaseLibraryCompositionsView {

    @AddToEndSingle
    void showAlbumInfo(Album album);

    @OneExecution
    void closeScreen();

    @Skip
    void showEditAlbumScreen(Album album);
}
