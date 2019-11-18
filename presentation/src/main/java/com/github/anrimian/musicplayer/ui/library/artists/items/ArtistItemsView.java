package com.github.anrimian.musicplayer.ui.library.artists.items;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView;

import java.util.List;

import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface ArtistItemsView extends BaseLibraryCompositionsView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showArtistInfo(Artist artist);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showArtistAlbums(List<Album> albums);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();
}
