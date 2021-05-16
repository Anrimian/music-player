package com.github.anrimian.musicplayer.ui.library.artists.items;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.viewstate.strategy.StateStrategyType;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;
import moxy.viewstate.strategy.alias.Skip;

public interface ArtistItemsView extends BaseLibraryCompositionsView {

    String RENAME_STATE = "rename_state";

    @AddToEndSingle
    void showArtistInfo(Artist artist);

    @AddToEndSingle
    void showArtistAlbums(List<Album> albums);

    @OneExecution
    void closeScreen();

    @Skip
    void showRenameArtistDialog(Artist artist);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = RENAME_STATE)
    void showRenameProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = RENAME_STATE)
    void hideRenameProgress();
}
