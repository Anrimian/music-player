package com.github.anrimian.musicplayer.ui.library.artists.items;

import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView;

import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface ArtistItemsView extends BaseLibraryCompositionsView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showArtistInfo(Artist artist);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();
}
