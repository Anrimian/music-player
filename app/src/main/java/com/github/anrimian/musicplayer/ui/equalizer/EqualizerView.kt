package com.github.anrimian.musicplayer.ui.equalizer

import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface EqualizerView : MvpView {

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @AddToEndSingle
    fun displayEqualizerConfig(config: EqualizerConfig)

    @AddToEndSingle
    fun displayEqualizerState(equalizerState: EqualizerState, config: EqualizerConfig)

    @AddToEndSingle
    fun showEqualizerRestartButton(show: Boolean)

}