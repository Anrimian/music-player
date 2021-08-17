package com.github.anrimian.musicplayer.ui.equalizer

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.EqInitializationException
import com.github.anrimian.musicplayer.domain.interactors.player.EqualizerInteractor
import com.github.anrimian.musicplayer.domain.models.equalizer.Band
import com.github.anrimian.musicplayer.domain.models.equalizer.EqInitializationState
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
        subscribeOnEqInitializationState()
        loadEqualizerConfig()
    }

    private fun subscribeOnEqInitializationState() {
        interactor.eqInitializationState
            .unsafeSubscribeOnUi(this::onEqInitializationStateReceived)
    }

    private fun onEqInitializationStateReceived(state: EqInitializationState) {
        //on error show error button
    }

    private fun loadEqualizerConfig() {
        interactor.equalizerConfig
            .subscribeOnUi(this::onEqualizerConfigReceived, this::onDefaultError)//show message and button
    }

    fun onBandLevelChanged(band: Band, value: Short) {
        interactor.setBandLevel(band.bandNumber, value)
    }

    fun onBandLevelDragStopped() {
        interactor.saveBandLevel()
    }

    fun onPresetSelected(preset: Preset) {
        try {
            interactor.setPreset(preset)
        } catch (e: EqInitializationException) {
            viewState.showErrorMessage(errorParser.parseError(e))
        }
    }

    fun onRestartAppEqClicked() {
        interactor.tryToReattachEqualizer()
        //reload config if not loaded
        //resubscribe on state if not subscribed and if config is loaded
    }

    private fun onEqualizerConfigReceived(config: EqualizerConfig) {
        viewState.displayEqualizerConfig(config)//optional?
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