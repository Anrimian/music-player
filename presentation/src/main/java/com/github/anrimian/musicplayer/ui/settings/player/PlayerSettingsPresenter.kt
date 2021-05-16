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
                interactor.isDecreaseVolumeOnAudioFocusLossEnabled
        )
        subscribeOnSelectedEqualizer()
    }

    fun onDecreaseVolumeOnAudioFocusLossChecked(checked: Boolean) {
        viewState.showDecreaseVolumeOnAudioFocusLossEnabled(checked)
        interactor.isDecreaseVolumeOnAudioFocusLossEnabled = checked
    }

    private fun subscribeOnSelectedEqualizer() {
        interactor.selectedEqualizerTypeObservable
                .unsafeSubscribeOnUi(viewState::showSelectedEqualizerType)
    }
}