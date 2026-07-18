package com.github.anrimian.musicplayer.infrastructure.service.wearable

import android.content.Context
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.CommonPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.utils.wear.NodesAvailabilityObservable
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable

class WearableStateController(
    context: Context,
    playerInteractor: PlayerInteractor,
    libraryPlayerInteractor: LibraryPlayerInteractor,
    commonPlayerInteractor: CommonPlayerInteractor,
    systemMusicController: SystemMusicController,
    private val analytics: Analytics,
    private val ioScheduler: Scheduler,
    errorParser: ErrorParser
) {

    private val nodesObservable = NodesAvailabilityObservable(
        context,
        context.getString(R.string.wear_capability),
        analytics::processNonFatalError
    )
    private var nodesDisposable: Disposable? = null

    private val wearableApi = WearableApi(context, analytics)

    private val persistentStatesController = PersistentStatesController(
        context,
        wearableApi,
        playerInteractor,
        libraryPlayerInteractor,
        commonPlayerInteractor,
        systemMusicController,
        ioScheduler
    )
    private val activeStatesController = ActiveStatesController(
        libraryPlayerInteractor,
        wearableApi,
        analytics,
        ioScheduler,
        errorParser
    )

    fun init() {
        nodesDisposable = nodesObservable.getObservable()
            .subscribeOn(ioScheduler)
            .subscribe(this::onAvailableNodesChanged)
    }

    fun onCapabilityChanged(info: CapabilityInfo) {
        nodesObservable.onCapabilityChanged(info)
    }

    fun checkProtocolVersion(wearProtocolVersion: Int, senderNodeId: String): Boolean {
        return persistentStatesController.checkProtocolVersion(wearProtocolVersion, senderNodeId)
    }

    fun onActualStateRequested(volumeState: VolumeState, senderNodeId: String) {
        persistentStatesController.onActualStateRequested(volumeState, senderNodeId)
    }

    fun onAppStateRequested(nodeStateLastUpdateTime: Long, senderNodeId: String, ) {
        persistentStatesController.onAppStateRequested(nodeStateLastUpdateTime, senderNodeId)
    }

    fun onSkipToNextRequested() {
        persistentStatesController.onSkipToNextRequested()
    }

    fun onSkipToPreviousRequested() {
        persistentStatesController.onSkipToPreviousRequested()
    }

    fun onSkipToItemRequested(itemId: Long) {
        persistentStatesController.onSkipToItemRequested(itemId)
    }

    fun onSeekToRequested(position: Long) {
        persistentStatesController.onSeekToRequested(position)
    }

    fun onFastSeekForwardRequested() {
        persistentStatesController.onFastSeekForwardRequested()
    }

    fun onFastSeekBackwardRequested() {
        persistentStatesController.onFastSeekBackwardRequested()
    }

    fun onChangeVolumeRequested(volume: Int, requestId: Int?) {
        persistentStatesController.onChangeVolumeRequested(volume, requestId)
    }

    fun onChangeSpeedRequested(speed: Float, requestId: Int?) {
        persistentStatesController.onChangeSpeedRequested(speed, requestId)
    }

    fun onRandomModeChangeRequested(isRandom: Boolean, requestId: Int?) {
        persistentStatesController.onRandomModeChangeRequested(isRandom, requestId)
    }

    fun onRepeatModeChangeRequested(repeatMode: Int, requestId: Int?) {
        persistentStatesController.onRepeatModeChangeRequested(repeatMode, requestId)
    }

    fun checkWearableEvent(eventName: String, senderNodeId: String): Boolean {
        return persistentStatesController.checkWearableEvent(eventName, senderNodeId)
    }

    fun onQueueSubscriptionRequested(targetNode: String, updateTime: Long?) {
        activeStatesController.onQueueSubscriptionRequested(targetNode, updateTime)
    }

    fun onQueueExpandRequested(
        targetNode: String,
        isForward: Boolean,
        borderItemId: Long,
        currentItemsCount: Int,
        expandSize: Int,
        lastReceivedUpdateTime: Long
    ) {
        activeStatesController.onQueueExpandRequested(
            targetNode,
            isForward,
            borderItemId,
            currentItemsCount,
            expandSize,
            lastReceivedUpdateTime
        )
    }

    fun onQueueSubscriptionCancel(targetNode: String) {
        activeStatesController.onQueueSubscriptionCancel(targetNode)
    }

    private fun onAvailableNodesChanged(nodes: Set<Node>) {
        persistentStatesController.onAvailableNodesChanged(nodes)
        activeStatesController.onAvailableNodesChanged(nodes)
    }

}