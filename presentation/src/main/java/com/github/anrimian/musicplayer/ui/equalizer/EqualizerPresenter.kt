package com.github.anrimian.musicplayer.ui.equalizer

import com.github.anrimian.musicplayer.domain.interactors.player.EqualizerInteractor
import com.github.anrimian.musicplayer.domain.models.equalizer.Band
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class EqualizerPresenter(
        private val interactor: EqualizerInteractor,
        uiScheduler: Scheduler,
        errorParser: ErrorParser
) : AppPresenter<EqualizerView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadEqualizerConfig()
    }

    private fun loadEqualizerConfig() {
        interactor.equalizerConfig.unsafeSubscribeOnUi(this::onEqualizerConfigReceived)
    }

    fun onBandLevelChanged(band: Band, value: Short) {
        interactor.setBandLevel(band.bandNumber, value)
    }

    fun onBandLevelDragStopped() {
        interactor.saveBandLevel()
    }

    fun onPresetSelected(preset: Preset) {
        interactor.setPreset(preset)
    }

    private fun onEqualizerConfigReceived(config: EqualizerConfig) {
        viewState.displayEqualizerConfig(config)
        subscribeOnEqualizerState(config)
    }

    private fun subscribeOnEqualizerState(config: EqualizerConfig) {
        interactor.equalizerStateObservable
                .subscribeOnUi(
                        { equalizerState -> viewState.displayEqualizerState(equalizerState, config) },
                        this::onDefaultError
                )
    }

    private fun onDefaultError(throwable: Throwable) {
        viewState.showErrorMessage(errorParser.parseError(throwable))
    }
}