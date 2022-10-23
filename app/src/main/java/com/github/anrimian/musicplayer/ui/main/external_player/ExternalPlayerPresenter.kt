package com.github.anrimian.musicplayer.ui.main.external_player

import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
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
    
    private var currentPosition: Long = 0
    
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showKeepPlayerInBackground(interactor.isExternalPlayerKeepInBackground())
        getCurrentSource()?.let(viewState::displayComposition)

        subscribeOnPlayerStateChanges()
        subscribeOnTrackPositionChanging()
        subscribeOnRepeatMode()
        subscribeOnErrorEvents()
        subscribeOnSpeedAvailableState()
        subscribeOnSpeedState()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!interactor.isExternalPlayerKeepInBackground()) {
            interactor.stop()
        }
    }

    fun onSourceForPlayingReceived(source: ExternalCompositionSource) {
        interactor.startPlaying(source)
        viewState.displayComposition(source)
        viewState.showTrackState(currentPosition, source.duration)
    }

    fun onPlayPauseClicked() {
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
        interactor.setExternalPlayerKeepInBackground(checked)
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
        interactor.getPlayerStateObservable().unsafeSubscribeOnUi(this::onPlayerStateReceived)
    }

    private fun onPlayerStateReceived(playerState: PlayerState) {
        if (playerState is PlayerState.Error) {
            val errorCommand = errorParser.parseError(playerState.throwable)
            viewState.showPlayErrorState(errorCommand)
        } else {
            viewState.showPlayErrorState(null)
        }
    }

    private fun subscribeOnRepeatMode() {
        interactor.getExternalPlayerRepeatModeObservable().unsafeSubscribeOnUi(viewState::showRepeatMode)
    }

    private fun subscribeOnTrackPositionChanging() {
        interactor.getTrackPositionObservable().unsafeSubscribeOnUi(this::onTrackPositionChanged)
    }

    private fun onTrackPositionChanged(currentPosition: Long) {
        this.currentPosition = currentPosition
        val source = getCurrentSource()
        if (source != null) {
            val duration = source.duration
            viewState.showTrackState(currentPosition, duration)
        }
    }

    private fun subscribeOnPlayerStateChanges() {
        interactor.getIsPlayingStateObservable().unsafeSubscribeOnUi(viewState::showPlayerState)
    }

    private fun subscribeOnSpeedAvailableState() {
        interactor.getSpeedChangeAvailableObservable()
                .unsafeSubscribeOnUi(viewState::showSpeedChangeFeatureVisible)
    }

    private fun subscribeOnSpeedState() {
        interactor.getPlaybackSpeedObservable().unsafeSubscribeOnUi(viewState::displayPlaybackSpeed)
    }

    private fun getCurrentSource(): ExternalCompositionSource? {
        val source = interactor.getCurrentSource()
        if (source is ExternalCompositionSource) {
            return source
        }
        return null
    }
}