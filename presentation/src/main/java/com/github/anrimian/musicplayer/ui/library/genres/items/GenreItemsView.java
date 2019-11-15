package com.github.anrimian.musicplayer.ui.library.genres.items;

import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView;

import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface GenreItemsView extends BaseLibraryCompositionsView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showGenreInfo(Genre genre);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();
}
