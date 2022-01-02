package com.github.anrimian.musicplayer.ui.equalizer

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.EqInitializationException
import com.github.anrimian.musicplayer.domain.interactors.player.EqualizerInteractor
import com.github.anrimian.musicplayer.domain.models.equalizer.*
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class EqualizerPresenter(
    private val interactor: EqualizerInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<EqualizerView>(uiScheduler, errorParser) {

    private var eqInitializationState: EqInitializationState? = null
    private var configLoadingError = false
    private var equalizerStateError = false

    private lateinit var equalizerConfig: EqualizerConfig

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showEqualizerRestartButton(false)

        subscribeOnEqInitializationState()
        loadEqualizerConfig()
    }

    private fun subscribeOnEqInitializationState() {
        interactor.eqInitializationState
            .unsafeSubscribeOnUi(this::onEqInitializationStateReceived)
    }

    private fun onEqInitializationStateReceived(state: EqInitializationState) {
        eqInitializationState = state
        tryToShowRestartEqButton()
    }

    private fun loadEqualizerConfig() {
        interactor.equalizerConfig
            .subscribeOnUi(this::onEqualizerConfigReceived, this::onEqConfigError)
    }

    fun onBandLevelChanged(band: Band, value: Short) {
        try {
            interactor.setBandLevel(band.bandNumber, value)
        } catch (e: Exception) {
            viewState.showErrorMessage(errorParser.parseError(e))
        }
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
        if (configLoadingError) {
            loadEqualizerConfig()
        } else if (equalizerStateError) {
            subscribeOnEqualizerState(equalizerConfig)
        }
    }

    private fun onEqualizerConfigReceived(config: EqualizerConfig) {
        this.equalizerConfig = config
        configLoadingError = false
        viewState.displayEqualizerConfig(config)
        tryToShowRestartEqButton()

        subscribeOnEqualizerState(config)
    }

    private fun onEqConfigError(throwable: Throwable) {
        configLoadingError = true
        viewState.showErrorMessage(errorParser.parseError(throwable))
        tryToShowRestartEqButton()
    }

    private fun subscribeOnEqualizerState(config: EqualizerConfig) {
        interactor.equalizerStateObservable
            .subscribeOnUi(
                { equalizerState -> onEqualizerStateReceived(equalizerState, config) },
                this::onEqStateError
            )
    }

    private fun onEqualizerStateReceived(equalizerState: EqualizerState, config: EqualizerConfig) {
        equalizerStateError = false
        viewState.displayEqualizerState(equalizerState, config)
        tryToShowRestartEqButton()
    }

    private fun onEqStateError(throwable: Throwable) {
        equalizerStateError = true
        viewState.showErrorMessage(errorParser.parseError(throwable))
        tryToShowRestartEqButton()
    }

    private fun tryToShowRestartEqButton() {
        val show = eqInitializationState == EqInitializationState.INITIALIZATION_ERROR
                || configLoadingError
                || equalizerStateError
        viewState.showEqualizerRestartButton(show)
    }
}