package com.github.anrimian.musicplayer.infrastructure.service.wearable

import android.content.Context
import com.github.anrimian.common.WearableConstants
import com.github.anrimian.common.WearableEvents
import com.github.anrimian.common.WearableFields
import com.github.anrimian.domain.models.ExternalWearableComposition
import com.github.anrimian.domain.models.LibraryWearableComposition
import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.data.utils.Permissions
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.interactors.player.CommonPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import com.google.android.gms.wearable.Node
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable

class PersistentStatesController(
    private val context: Context,
    private val wearableApi: WearableApi,
    private val playerInteractor: PlayerInteractor,
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    private val commonPlayerInteractor: CommonPlayerInteractor,
    private val systemMusicController: SystemMusicController,
    private val ioScheduler: Scheduler,
) {

    private val stateDisposable = CompositeDisposable()

    private val wearableStateHolder = WearableStateHolder(context)

    fun onAvailableNodesChanged(nodes: Set<Node>) {
        if (nodes.isEmpty()) {
            stateDisposable.clear()
            return
        }
        if (WearableConstants.WEARABLE_PROTOCOL_VERSION != wearableStateHolder.getProtocolVersion()) {
            //(perhaps) clean state if we receive higher protocol version?
            wearableStateHolder.setProtocolVersion(WearableConstants.WEARABLE_PROTOCOL_VERSION)
            sendCurrentState()
        }
        stateDisposable.add(
            playerInteractor.getIsPlayingStateObservable()
                .observeOn(ioScheduler)
                .subscribe(this::onPlayerStateReceived)
        )
        stateDisposable.add(
            libraryPlayerInteractor.getCurrentQueueItemObservable()
                .observeOn(ioScheduler)
                .subscribe(this::onCurrentQueueItemReceived)
        )
        stateDisposable.add(
            playerInteractor.getCurrentSourceObservable()
                .observeOn(ioScheduler)
                .subscribe(this::onCurrentSourceReceived)
        )
        stateDisposable.add(
            systemMusicController.getVolumeStateObservable()
                .observeOn(ioScheduler)
                .subscribe(this::onVolumeStateReceived)
        )
        stateDisposable.add(
            commonPlayerInteractor.trackPositionChangeObservable
                .observeOn(ioScheduler)
                .subscribe(this::onTrackPositionReceived)
        )
        stateDisposable.add(
            commonPlayerInteractor.playbackSpeedObservable
                .observeOn(ioScheduler)
                .subscribe(this::onPlaybackSpeedReceived)
        )
        stateDisposable.add(
            commonPlayerInteractor.randomModeObservable
                .observeOn(ioScheduler)
                .subscribe(this::onRandomModeReceived)
        )
        stateDisposable.add(
            commonPlayerInteractor.repeatModeObservable
                .observeOn(ioScheduler)
                .subscribe(this::onRepeatModeReceived)
        )
    }

    fun checkProtocolVersion(wearProtocolVersion: Int, senderNodeId: String): Boolean {
        if (wearProtocolVersion < WearableConstants.WEARABLE_PROTOCOL_VERSION) {
            sendCurrentState(senderNodeId)
            return false
        }
        return true
    }

    fun onActualStateRequested(volumeState: VolumeState, senderNodeId: String) {
        val currentVolumeState = systemMusicController.getVolumeState()
        wearableStateHolder.applyVolumeState(currentVolumeState)
        if (volumeState == currentVolumeState) {
            return
        }
        wearableApi.sendVolume(volumeState, senderNodeId)
    }

    //TODO-W +++ Case when request with time from wearable failed
    //TODO-W check case: app is not open, request state
    fun onAppStateRequested(nodeStateLastUpdateTime: Long, senderNodeId: String, ) {
        if (nodeStateLastUpdateTime == WearableFields.NO_STATE_TIME
            || nodeStateLastUpdateTime < wearableStateHolder.getLastUpdateTime()
        ) {
            sendCurrentState(senderNodeId)
        }
    }

    fun onSkipToNextRequested() {
        commonPlayerInteractor.skipToNext()
    }

    fun onSkipToPreviousRequested() {
        commonPlayerInteractor.skipToPrevious()
    }

    fun onSkipToItemRequested(itemId: Long) {
        libraryPlayerInteractor.skipToItem(itemId)
    }

    fun onSeekToRequested(position: Long) {
        commonPlayerInteractor.seekTo(position)
    }

    fun onFastSeekForwardRequested() {
        commonPlayerInteractor.fastSeekForward()
    }

    fun onFastSeekBackwardRequested() {
        commonPlayerInteractor.fastSeekBackward()
    }

    fun onChangeVolumeRequested(volume: Int, requestId: Int?) {
        systemMusicController.setVolume(volume)
        /*
            Attention: response can be send by this method or by listener
            We have race condition here, but it behaves correctly:
              Requester just should receive equal amount of responses
         */
        onVolumeStateReceived(systemMusicController.getVolumeState(), requestId)
    }

    fun onChangeSpeedRequested(speed: Float, requestId: Int?) {
        commonPlayerInteractor.setPlaybackSpeed(speed)
        onPlaybackSpeedReceived(speed, requestId)
    }

    fun onRandomModeChangeRequested(isRandom: Boolean, requestId: Int?) {
        commonPlayerInteractor.setRandomPlayingEnabled(isRandom)
        onRandomModeReceived(isRandom, requestId)
    }

    fun onRepeatModeChangeRequested(repeatMode: Int, requestId: Int?) {
        commonPlayerInteractor.setRepeatMode(repeatMode)
        onRepeatModeReceived(repeatMode, requestId)
    }

    fun checkWearableEvent(eventName: String, senderNodeId: String): Boolean {
        var errorType: Int? = null
        if (!Permissions.hasFilePermission(context)) {
            errorType = WearableFields.ERROR_NO_PERMISSION
        }
        if (errorType != null) {
            wearableApi.sendErrorEvent(errorType, eventName, senderNodeId)
            return false
        }
        if (wearableStateHolder.getCurrentComposition() == null
            && eventName != WearableEvents.REQUEST_APP_STATE
        ) {
            sendCurrentState(senderNodeId)
            return false
        }
        return true
    }

    private fun sendCurrentState(targetNode: String? = null) {
        wearableApi.sendCurrentState(
            targetNode,
            wearableStateHolder.getLastUpdateTime(),
            wearableStateHolder.isPlaying(),
            wearableStateHolder.getCurrentQueueItemId(),
            wearableStateHolder.getCurrentComposition(),
            wearableStateHolder.getTrackPosition(),
            wearableStateHolder.getVolume(),
            wearableStateHolder.getPlaybackSpeed(),
            wearableStateHolder.getRandomMode(),
            wearableStateHolder.getRepeatMode()
        )
    }


    private fun onPlayerStateReceived(isPlaying: Boolean) {
        val updateTime = wearableStateHolder.applyPlayingState(isPlaying)
        if (updateTime != null) {
            wearableApi.sendPlayPause(isPlaying, updateTime)
        }
    }

    private fun onCurrentQueueItemReceived(event: PlayQueueEvent) {
        val itemId = event.playQueueItem?.itemId
        val updateTime = wearableStateHolder.applyCurrentQueueItemId(itemId)
        if (updateTime != null) {
            wearableApi.sendPlayQueueItemId(itemId, updateTime)
        }
    }

    private fun onCurrentSourceReceived(sourceOpt: Opt<CompositionSource>) {
        val source = toWearableModel(sourceOpt.value)
        val updateTime = wearableStateHolder.applyCurrentComposition(source)
        if (updateTime != null) {
            wearableApi.sendCurrentComposition(source, updateTime)
        }
    }

    private fun onVolumeStateReceived(volumeState: VolumeState, requestId: Int? = null) {
        val updateTime = wearableStateHolder.applyVolumeState(volumeState)
        if (updateTime != null) {
            wearableApi.sendVolume(volumeState, requestId = requestId)
        }
    }

    private fun onTrackPositionReceived(position: Long) {
        //TODO-W set update time too? check cold connection:
        // set state, disconnect, seekTo on device, connect
        //  but we'll receive it from subscription, not from state. Second wearable case?
        // if update time is needed, add explanation why
        val updated = wearableStateHolder.setTrackPosition(position)
        if (updated) {
            wearableApi.sendTrackPosition(position)
        }
    }

    private fun onPlaybackSpeedReceived(speed: Float, requestId: Int? = null) {
        val updateTime = wearableStateHolder.setPlaybackSpeed(speed)
        if (updateTime != null) {
            wearableApi.sendPlaybackSpeed(speed, updateTime, requestId)
        }
    }

    private fun onRandomModeReceived(isRandom: Boolean, requestId: Int? = null) {
        val updateTime = wearableStateHolder.setRandomMode(isRandom)
        if (updateTime != null) {
            wearableApi.sendRandomMode(isRandom, updateTime, requestId)
        }
    }

    private fun onRepeatModeReceived(repeatMode: Int, requestId: Int? = null) {
        val updateTime = wearableStateHolder.setRepeatMode(repeatMode)
        if (updateTime != null) {
            wearableApi.sendRepeatMode(repeatMode, updateTime, requestId)
        }
    }

    private fun toWearableModel(source: CompositionSource?): WearableComposition? {
        return when (source) {
            null -> null
            is LibraryCompositionSource -> {
                val composition = source.composition
                LibraryWearableComposition(
                    composition.id,
                    composition.title,
                    composition.artist,
                    composition.duration
                )
            }
            is ExternalCompositionSource -> {
                ExternalWearableComposition(
                    CompositionHelper.formatCompositionName(source.title, source.displayName),
                    source.artist,
                    source.duration
                )
            }
            else -> null
        }
    }

}