package com.github.anrimian.simplemusicplayer.ui.library.compositions;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Order;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

public interface LibraryCompositionsView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void bindList(List<Composition> compositions);

    @StateStrategyType(SkipStrategy.class)
    void updateList(List<Composition> oldList, List<Composition> sourceList);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectOrderScreen(Order folderOrder);
}
