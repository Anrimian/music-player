package com.github.anrimian.musicplayer.wear.ui

import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import com.github.anrimian.musicplayer.wear.domain.WearStateInteractor
import com.github.anrimian.musicplayer.wear.domain.models.DeviceState
import com.github.anrimian.musicplayer.wear.domain.models.ErrorEvent
import com.github.anrimian.musicplayer.wear.domain.queue.PlayQueueInteractor
import io.reactivex.rxjava3.core.Scheduler

class MainPresenter(
    private val wearStateInteractor: WearStateInteractor,
    private val playQueueInteractor: PlayQueueInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
): AppPresenter<MainView>(uiScheduler, errorParser) {

    private var currentComposition: WearableComposition? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showDeviceState(DeviceState.CONNECTED)

        wearStateInteractor.getDeviceStateObservable()
            .unsafeSubscribeOnUi(viewState::showDeviceState)
        wearStateInteractor.getErrorEventsObservable()
            .unsafeSubscribeOnUi(this::onErrorEventReceived)
        wearStateInteractor.getIsPlayingObservable()
            .unsafeSubscribeOnUi(viewState::showIsPlaying)
        wearStateInteractor.getCurrentCompositionObservable()
            .unsafeSubscribeOnUi(this::onCompositionReceived)
        wearStateInteractor.getTrackPositionObservable()
            .unsafeSubscribeOnUi(this::onTrackPositionReceived)
        wearStateInteractor.getVolumeStateObservable()
            .map(VolumeState::toLong)
            .unsafeSubscribeOnUi(viewState::showCurrentVolume)
        playQueueInteractor.getPlayQueueObservable()
            .unsafeSubscribeOnUi(viewState::updatePlayQueue)
    }

    fun onScreenStarted() {
        wearStateInteractor.onAppScreenStarted()
        playQueueInteractor.startObserveQueue()
    }

    fun onScreenStopped() {
        playQueueInteractor.stopObserveQueue()
    }

    fun onPlayPauseClicked() {
        wearStateInteractor.playPause()
    }

    fun onPreviousClicked() {
        //TODO-W case if we have source from external player
        wearStateInteractor.skipToPrevious()
    }

    fun onNextClicked() {
        wearStateInteractor.skipToNext()
    }

    fun onTrackRewoundTo(position: Int) {

    }

    fun onPositionSeekStart() {

    }

    fun onPositionSeekStop(position: Int) {
        wearStateInteractor.seekTo(position.toLong())
    }

    fun onVolumeChangeRequested(increase: Boolean): Boolean {
        return wearStateInteractor.changeVolume(increase)
    }

    private fun onCompositionReceived(compositionOpt: Opt<WearableComposition>) {
        this.currentComposition = compositionOpt.value
        viewState.showComposition(compositionOpt)
    }

    private fun onTrackPositionReceived(position: Long) {
        currentComposition?.let { item -> viewState.showTrackState(position, item.duration) }
    }

    private fun onErrorEventReceived(errorEvent: ErrorEvent) {
        val throwable = errorEvent.throwable
        if (throwable != null) {
            errorParser.logError(throwable)
        }
        viewState.showErrorEvent(errorEvent)
    }

}