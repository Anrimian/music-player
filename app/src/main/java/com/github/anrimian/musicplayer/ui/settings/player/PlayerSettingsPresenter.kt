package com.github.anrimian.musicplayer.ui.settings.player

import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
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
        viewState.showSoundBalance(interactor.getSoundBalance())
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
        interactor.setPauseOnZeroVolumeLevelEnabled(checked)
    }

    fun onSoundBalanceClicked() {
        viewState.showSoundBalanceDialog(interactor.getSoundBalance())
    }

    fun onSoundBalancePicked(soundBalance: SoundBalance) {
        viewState.showSoundBalance(soundBalance)
        interactor.setSoundBalance(soundBalance)
    }

    fun onSoundBalanceSelected(soundBalance: SoundBalance) {
        interactor.saveSoundBalance(soundBalance)
    }

    fun onResetSoundBalanceClick() {
        val soundBalance = SoundBalance(1f, 1f)
        viewState.showSoundBalance(soundBalance)
        interactor.saveSoundBalance(soundBalance)
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