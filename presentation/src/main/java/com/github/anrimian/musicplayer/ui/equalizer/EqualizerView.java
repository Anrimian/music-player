package com.github.anrimian.musicplayer.ui.equalizer;

import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface EqualizerView extends MvpView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand parseError);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void displayEqualizerConfig(EqualizerConfig config);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void displayEqualizerState(EqualizerState equalizerState, EqualizerConfig config);
}
