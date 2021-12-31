package com.github.anrimian.musicplayer.ui.main.external_player

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource
import com.github.anrimian.musicplayer.domain.interactors.player.ExternalPlayerInteractor
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class ExternalPlayerPresenter(
        private val interactor: ExternalPlayerInteractor,
        uiScheduler: Scheduler,
        errorParser: ErrorParser
) : AppPresenter<ExternalPlayerView>(uiScheduler, errorParser) {
    
    private var compositionSource: UriCompositionSource? = null
    
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showKeepPlayerInBackground(interactor.isExternalPlayerKeepInBackground)

        subscribeOnPlayerStateChanges()
        subscribeOnTrackPositionChanging()
        subscribeOnRepeatMode()
        subscribeOnErrorEvents()
        subscribeOnSpeedAvailableState()
        subscribeOnSpeedState()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!interactor.isExternalPlayerKeepInBackground) {
            interactor.stop()
        }
    }

    fun onSourceForPlayingReceived(source: UriCompositionSource) {
        compositionSource = source
        interactor.startPlaying(compositionSource)
        viewState.displayComposition(source)
    }

    fun onPlayPauseClicked() {
        viewState.showPlayErrorEvent(null)
        interactor.playOrPause()
    }

    fun onTrackRewoundTo(progress: Int) {
        interactor.seekTo(progress.toLong())
    }

    fun onSeekStart() {
        interactor.onSeekStarted()
    }

    fun onSeekStop(progress: Int) {
        interactor.onSeekFinished(progress.toLong())
    }

    fun onRepeatModeButtonClicked() {
        interactor.changeExternalPlayerRepeatMode()
    }

    fun onKeepPlayerInBackgroundChecked(checked: Boolean) {
        interactor.isExternalPlayerKeepInBackground = checked
    }

    fun onFastSeekForwardCalled() {
        interactor.fastSeekForward()
    }

    fun onFastSeekBackwardCalled() {
        interactor.fastSeekBackward()
    }

    fun onPlaybackSpeedSelected(speed: Float) {
        viewState.displayPlaybackSpeed(speed)
        interactor.setPlaybackSpeed(speed)
    }

    private fun subscribeOnErrorEvents() {
        interactor.errorEventsObservable.unsafeSubscribeOnUi(viewState::showPlayErrorEvent)
    }

    private fun subscribeOnRepeatMode() {
        interactor.externalPlayerRepeatModeObservable.unsafeSubscribeOnUi(viewState::showRepeatMode)
    }

    private fun subscribeOnTrackPositionChanging() {
        interactor.trackPositionObservable.unsafeSubscribeOnUi(this::onTrackPositionChanged)
    }

    private fun onTrackPositionChanged(currentPosition: Long) {
        if (compositionSource != null) {
            val duration = compositionSource!!.duration
            viewState.showTrackState(currentPosition, duration)
        }
    }

    private fun subscribeOnPlayerStateChanges() {
        interactor.playerStateObservable.unsafeSubscribeOnUi(this::onPlayerStateChanged)
    }

    private fun onPlayerStateChanged(playerState: PlayerState) {
        viewState.showPlayerState(playerState)
    }

    private fun subscribeOnSpeedAvailableState() {
        interactor.speedChangeAvailableObservable
                .unsafeSubscribeOnUi(viewState::showSpeedChangeFeatureVisible)
    }

    private fun subscribeOnSpeedState() {
        interactor.playbackSpeedObservable.unsafeSubscribeOnUi(viewState::displayPlaybackSpeed)
    }
}