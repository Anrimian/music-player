package com.github.anrimian.musicplayer.ui.equalizer.app;

import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface EqualizerView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void displayBands(List<Band> bands);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand parseError);
}
