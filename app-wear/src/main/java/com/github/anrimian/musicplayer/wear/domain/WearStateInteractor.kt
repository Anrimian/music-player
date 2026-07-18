package com.github.anrimian.musicplayer.wear.domain

import android.util.Log
import com.github.anrimian.common.WearableConstants
import com.github.anrimian.common.WearableEvents
import com.github.anrimian.common.WearableFields
import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.utils.NumberUtils
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.github.anrimian.musicplayer.wear.Constants
import com.github.anrimian.musicplayer.wear.data.WearStateRepository
import com.github.anrimian.musicplayer.wear.data.repositories.HostDeviceRepository
import com.github.anrimian.musicplayer.wear.domain.controllers.RemoteStateController
import com.github.anrimian.musicplayer.wear.domain.models.DeviceState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

//TODO-W track position predict logic
// + case when position is changed from device
//   + A: (done) forceChangeTrackPositionObservable in PlayerInteractor
//        possible apply to latest api media session as well(do not fire it every second)
// + handle playback speed and possible silence skip
// + (?opt)predict only if there are subscribers(screen is resumed)
class WearStateInteractor(
    private val wearStateRepository: WearStateRepository,
    private val deviceRepository: HostDeviceRepository,
    private val remoteStateController: RemoteStateController
) {

    private val trackPositionSubject = BehaviorSubject.create<Long>()
    private var trackPositionDisposable: Disposable? = null

    private var lastSyncedVolume = 0

    fun onAppStarted() {
        val updateTime = wearStateRepository.getLastUpdateTime()
        if (updateTime == WearableFields.NO_STATE_TIME) {
            requestAppState(updateTime, true)
        }
    }

    fun onAppScreenStarted() {
        if (wearStateRepository.getLastUpdateTime() != WearableFields.NO_STATE_TIME
            && !deviceRepository.hasActiveRequest(WearableEvents.REQUEST_APP_STATE)
        ) {
            requestActualState()
        }
    }

    fun onDeviceAvailabilityChanged(isAvailable: Boolean) {
        deviceRepository.onDeviceAvailabilityChanged(isAvailable)
    }

    fun onDeviceConnected(nodeId: String) {
        requestAppState(wearStateRepository.getLastUpdateTime(), false, nodeId)
    }

    fun onErrorReceived(errorType: Int, sentEventName: String) {
        deviceRepository.onRequestErrorReceived(errorType, sentEventName)
    }

    fun onPlayingStateReceived(isPlaying: Boolean, updateTime: Long) {
        if (deviceRepository.onRequestFinished(WearableEvents.PLAY_PAUSE)) {
            return
        }
        wearStateRepository.setLastUpdateTime(updateTime)
        setIsPlaying(isPlaying)
    }

    fun onCurrentSourceReceived(
        currentQueueItem: WearableComposition?,
        updateTime: Long
    ) {
        wearStateRepository.setCurrentComposition(currentQueueItem)
        wearStateRepository.setLastUpdateTime(updateTime)

        remoteStateController.setCurrentComposition(currentQueueItem)
    }

    fun onPlayQueueItemIdReceived(itemId: Long?, updateTime: Long) {
        wearStateRepository.setPlayQueueItemId(itemId)
        wearStateRepository.setLastUpdateTime(updateTime)
    }

    fun onVolumeStateReceived(newVolume: VolumeState, requestId: Int?) {
        Log.d("KEK", "onVolumeStateReceived, newVolume: $newVolume, requestId: $requestId ")
        if (deviceRepository.onRequestFinished(WearableEvents.CHANGE_VOLUME, requestId)) {
            return
        }
        val volumeState = wearStateRepository.getVolumeState()
        if (newVolume != volumeState) {
            Log.d("KEK", "onVolumeStateReceived, setVolumeState: $newVolume ")
            wearStateRepository.setVolumeState(newVolume)
            remoteStateController.setVolumeState(wearStateRepository.getVolumeState())
        }
    }

    fun checkProtocolVersion(protocolVersion: Int): Boolean {
        //(perhaps) clean prefs/state if we receive higher protocol version?
        wearStateRepository.setHostProtocolVersion(protocolVersion)
        if (protocolVersion != WearableConstants.WEARABLE_PROTOCOL_VERSION) {
            return false
        }
        return true
    }

    fun onCurrentStateReceived(
        isPlaying: Boolean,
        currentItemId: Long?,
        currentComposition: WearableComposition?,
        trackPosition: Long,
        currentVolume: VolumeState,
        updateTime: Long,
        playbackSpeed: Float,
        randomMode: Boolean,
        repeatMode: Int
    ) {
        deviceRepository.onRequestFinished(WearableEvents.REQUEST_APP_STATE)
        wearStateRepository.setPlayQueueItemId(currentItemId)
        wearStateRepository.setCurrentComposition(currentComposition)
        remoteStateController.setCurrentComposition(currentComposition)
        Log.d("KEK", "onCurrentStateReceived: ")
        wearStateRepository.setVolumeState(currentVolume)
        remoteStateController.setVolumeState(wearStateRepository.getVolumeState())
        wearStateRepository.setLastUpdateTime(updateTime)
        setCurrentTrackPosition(trackPosition)
        setIsPlaying(isPlaying)
    }

    fun onTrackPositionReceived(trackPosition: Long) {
        val currentComposition = getCurrentComposition()
        if (currentComposition == null || currentComposition.duration < trackPosition) {
            return
        }
        setCurrentTrackPosition(trackPosition)
    }

    fun playPause() {
        val isPlaying = wearStateRepository.isPlaying()
        setIsPlaying(!isPlaying)
        if (isPlaying) {
            wearStateRepository.setCurrentTrackPosition(getCurrentTrackPosition())
        }

        deviceRepository.sendEventWithTimeout(
            WearableEvents.PLAY_PAUSE,
            fallback = { setIsPlaying(isPlaying) }
        )
    }

    fun skipToNext() {
        deviceRepository.sendEvent(WearableEvents.SKIP_TO_NEXT)
    }

    fun skipToPrevious() {
        deviceRepository.sendEvent(WearableEvents.SKIP_TO_PREVIOUS)
    }

    fun skipToItem(itemId: Long) {
        val array = ByteArray(Long.SIZE_BYTES)
        NumberUtils.longToBytes(itemId, array)
        deviceRepository.sendEvent(WearableEvents.SKIP_TO_ITEM, array)
    }

    fun seekTo(position: Long) {
        //TODO-W predict position logic
        val array = ByteArray(Long.SIZE_BYTES)
        NumberUtils.longToBytes(position, array)
        deviceRepository.sendEvent(WearableEvents.SEEK_TO, array)
    }

    fun fastSeekForward() {
        //TODO-W predict position logic
        //TODO-W do not send such event types? Send `seekTo(expectedPosition)?`
        deviceRepository.sendEvent(WearableEvents.FAST_SEEK_FORWARD)
    }

    fun fastSeekBackward() {
        deviceRepository.sendEvent(WearableEvents.FAST_SEEK_BACKWARD)
    }

    //TODO-W (solved) Close host app, change volume - wear app has wrong state
    //TODO-W but! : open wear app, close main app, change volume on phone, change volume on wear -> we have wear volume
    // O1: send increase/decrease command instead of absolute volume?
    fun changeVolume(increase: Boolean): Boolean {
        val volumeState = wearStateRepository.getVolumeState()
        if (volumeState.toLong() == Constants.NO_STATE) {
            return false
        }
        val volume = volumeState.getVolume()
        val maxVolume = volumeState.getMaxVolume()
        val newVolume = volume + if (increase) 1 else -1
        if (newVolume < 0 || newVolume > maxVolume) {
            return false
        }
        if (!deviceRepository.hasActiveRequest(WearableEvents.CHANGE_VOLUME)) {
            lastSyncedVolume = volume
        }
        Log.d("KEK", "setCurrentVolume: $newVolume")
        wearStateRepository.setCurrentVolume(newVolume)
        remoteStateController.setVolumeState(wearStateRepository.getVolumeState())
        val array = ByteArray(Int.SIZE_BYTES)
        NumberUtils.intToBytes(newVolume, array)
        deviceRepository.sendEventWithTimeout(
            WearableEvents.CHANGE_VOLUME,
            array,
            fallback = {
                Log.d("KEK", "fallback - setCurrentVolume: $volume")
                wearStateRepository.setCurrentVolume(lastSyncedVolume)
            },
        )
        return true
    }

    fun getDeviceStateObservable(): Observable<DeviceState> {
        return deviceRepository.getHostDeviceAvailabilityObservable()
            .switchMap { state ->
                if (state == DeviceState.CONNECTED) {
                    wearStateRepository.getHostProtocolVersionObservable().map { hostVersion ->
                        when {
                            hostVersion == WearableConstants.WEARABLE_PROTOCOL_VERSION
                                    || hostVersion == Constants.UNKNOWN_HOST_PROTOCOL_VERSION -> state
                            hostVersion < WearableConstants.WEARABLE_PROTOCOL_VERSION -> DeviceState.HOST_UPDATE_REQUIRED
                            else -> DeviceState.WEAR_UPDATE_REQUIRED
                        }
                    }
                } else {
                    Observable.just(state)
                }
            }
    }

    fun getErrorEventsObservable() = deviceRepository.getErrorEventsObservable()

    fun isPlaying() = wearStateRepository.isPlaying()

    fun getIsPlayingObservable() = wearStateRepository.getIsPlayingObservable()

    fun getCurrentComposition() = wearStateRepository.getCurrentComposition()

    fun getCurrentCompositionObservable() = wearStateRepository.getCurrentCompositionObservable()

    fun getTrackPositionObservable(): Observable<Long> {
        return RxUtils.withDefaultValue(trackPositionSubject, wearStateRepository::getCurrentTrackPosition)
    }

    fun getVolumeState() = wearStateRepository.getVolumeState()

    fun getVolumeStateObservable(): Observable<VolumeState> {
        return wearStateRepository.getVolumeStateObservable()
    }

    //returns only first part of queue
    fun getPlayQueueObservable() {//observable?
        //get current item id and queue update time(more?)
        //get cached queue
        //if not exist ->
        // request queue from device
        // return loading status
        //if item id is not found in queue OR item(?) update time is more that queue time ->
        // clear cached queue
        // request queue from device
        // return loading status
        //return cached queue
    }

    fun getNextQueuePart(lastItemId: Long) {//return single
        //check item id contains and time compare
        //if not actual ->
        // clear cache and compare time
        //if cache contains next records ->
        // return select them from cache (if amount if them is too low - request more?)
        //request from device
        //return progress
    }

    private fun setCurrentTrackPosition(position: Long) {
        if (position != wearStateRepository.getCurrentTrackPosition()) {
            wearStateRepository.setCurrentTrackPosition(position)
            trackPositionSubject.onNext(position)
        }
    }

    private fun setIsPlaying(isPlaying: Boolean) {
        if (wearStateRepository.isPlaying() == isPlaying) {
            return
        }
        wearStateRepository.setIsPlaying(isPlaying)
        remoteStateController.setIsPlaying(isPlaying)
        if (isPlaying) {
            startTrackPositionObservable()
        } else {
            stopTrackPositionObservable()
        }
    }

    private fun startTrackPositionObservable() {
        trackPositionDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .map { getCurrentTrackPosition() + 1 }
            .subscribe(trackPositionSubject::onNext)
    }

    private fun getCurrentTrackPosition(): Long {
        return trackPositionSubject.value ?: wearStateRepository.getCurrentTrackPosition()
    }

    private fun stopTrackPositionObservable() {
        trackPositionDisposable?.dispose()
    }

    private fun requestActualState() {
        val volumeState = wearStateRepository.getVolumeState()
        if (volumeState.toLong() == Constants.NO_STATE) {
            return
        }
        val array = ByteArray(Long.SIZE_BYTES)
        NumberUtils.longToBytes(volumeState.toLong(), array)
        //TODO-W check protocol update with change on that array. Check it
        deviceRepository.sendEvent(WearableEvents.REQUEST_ACTUAL_STATE, array)
    }

    private fun requestAppState(
        lastUpdateTime: Long,
        expectResponse: Boolean,
        deviceNodeId: String? = null
    ) {
        val array = ByteArray(Int.SIZE_BYTES + Long.SIZE_BYTES)
        NumberUtils.intToBytes(wearStateRepository.getHostProtocolVersion(), array)
        NumberUtils.longToBytes(lastUpdateTime, array, Int.SIZE_BYTES)
        if (expectResponse) {
            deviceRepository.sendEventWithTimeout(
                WearableEvents.REQUEST_APP_STATE,
                array,
                deviceNodeId,
                null
            )
        } else {
            deviceRepository.sendEvent(WearableEvents.REQUEST_APP_STATE, array, deviceNodeId)
        }
    }



}