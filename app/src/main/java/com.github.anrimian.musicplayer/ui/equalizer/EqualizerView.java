package com.github.anrimian.musicplayer.ui.equalizer;

import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;

public interface EqualizerView extends MvpView {

    @OneExecution
    void showErrorMessage(ErrorCommand parseError);

    @AddToEndSingle
    void displayEqualizerConfig(EqualizerConfig config);

    @AddToEndSingle
    void displayEqualizerState(EqualizerState equalizerState, EqualizerConfig config);

    @AddToEndSingle
    void showEqualizerRestartButton(boolean show);
}
