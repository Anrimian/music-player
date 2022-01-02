package com.github.anrimian.musicplayer.ui.settings.player

import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class PlayerSettingsPresenter(private val interactor: PlayerSettingsInteractor,
                              uiScheduler: Scheduler,
                              errorParser: ErrorParser
) : AppPresenter<PlayerSettingsView>(uiScheduler, errorParser) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showDecreaseVolumeOnAudioFocusLossEnabled(
            interactor.isDecreaseVolumeOnAudioFocusLossEnabled()
        )
        viewState.showPauseOnAudioFocusLossEnabled(interactor.isPauseOnAudioFocusLossEnabled())
        viewState.showPauseOnZeroVolumeLevelEnabled(interactor.isPauseOnZeroVolumeLevelEnabled())
        viewState.showEnabledMediaPlayers(interactor.getEnabledMediaPlayers())
        subscribeOnSelectedEqualizer()
    }

    fun onDecreaseVolumeOnAudioFocusLossChecked(checked: Boolean) {
        viewState.showDecreaseVolumeOnAudioFocusLossEnabled(checked)
        interactor.setDecreaseVolumeOnAudioFocusLossEnabled(checked)
    }

    fun onPauseOnAudioFocusLossChecked(checked: Boolean) {
        viewState.showPauseOnAudioFocusLossEnabled(checked)
        interactor.setPauseOnAudioFocusLossEnabled(checked)
    }

    fun onPauseOnZeroVolumeLevelChecked(checked: Boolean) {
        viewState.showPauseOnZeroVolumeLevelEnabled(checked)
        interactor.setPauseOnAudioFocusLossEnabled(checked)
    }

    fun onEnabledMediaPlayersSelected(mediaPlayers: IntArray) {
        viewState.showEnabledMediaPlayers(mediaPlayers)
        interactor.setEnabledMediaPlayers(mediaPlayers)
    }

    private fun subscribeOnSelectedEqualizer() {
        interactor.getSelectedEqualizerTypeObservable()
            .unsafeSubscribeOnUi(viewState::showSelectedEqualizerType)
    }
}