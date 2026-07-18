package com.github.anrimian.musicplayer.infrastructure.service.wearable

import com.github.anrimian.common.BaseWearableListenerService
import com.github.anrimian.common.WearableEvents
import com.github.anrimian.common.WearableFields
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.utils.NumberUtils
import com.github.anrimian.musicplayer.ui.common.AppAndroidUtils
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageEvent
import org.json.JSONObject

class AppWearableListenerService: BaseWearableListenerService() {

    override fun onCapabilityChanged(info: CapabilityInfo) {
        super.onCapabilityChanged(info)
        Components.getAppComponent().wearableManager().onCapabilityChanged(info)
    }

    override fun onAppMessageReceived(
        messageEvent: MessageEvent,
        eventName: String,
        requestId: Int?,
    ) {
        val appComponent = Components.getAppComponent()
        val wearableManager = appComponent.wearableManager()
        val sourceNodeId = messageEvent.sourceNodeId
        val allowed = wearableManager.checkWearableEvent(eventName, sourceNodeId)
        if (!allowed) {
            return
        }
        val messageData = messageEvent.data
        when(eventName) {
            WearableEvents.PLAY_PAUSE -> {
                AppAndroidUtils.playPause(this, appComponent.playerInteractor())
            }
            WearableEvents.SKIP_TO_NEXT -> {
                wearableManager.onSkipToNextRequested()
            }
            WearableEvents.SKIP_TO_PREVIOUS -> {
                wearableManager.onSkipToPreviousRequested()
            }
            WearableEvents.SKIP_TO_ITEM -> {
                val itemId = NumberUtils.bytesToLong(messageData)
                wearableManager.onSkipToItemRequested(itemId)
            }
            WearableEvents.SEEK_TO -> {
                val position = NumberUtils.bytesToLong(messageData)
                wearableManager.onSeekToRequested(position)
            }
            WearableEvents.FAST_SEEK_FORWARD -> {
                wearableManager.onFastSeekForwardRequested()
            }
            WearableEvents.FAST_SEEK_BACKWARD -> {
                wearableManager.onFastSeekBackwardRequested()
            }
            WearableEvents.CHANGE_VOLUME -> {
                val volume =  NumberUtils.bytesToInt(messageData)
                wearableManager.onChangeVolumeRequested(volume, requestId)
            }
            WearableEvents.CHANGE_SPEED -> {
                val speed = NumberUtils.bytesToFloat(messageData)
                wearableManager.onChangeSpeedRequested(speed, requestId)
            }
            WearableEvents.CHANGE_RANDOM_MODE -> {
                val isRandom = NumberUtils.bytesToBoolean(messageData)
                wearableManager.onRandomModeChangeRequested(isRandom, requestId)
            }
            WearableEvents.CHANGE_REPEAT_MODE -> {
                val repeatMode = NumberUtils.bytesToInt(messageData)
                wearableManager.onRepeatModeChangeRequested(repeatMode, requestId)
            }
            WearableEvents.REQUEST_QUEUE_SUBSCRIPTION -> {
                val jsonObject = JSONObject(String(messageData))
                val lastUpdateTime = jsonObject.getLong(WearableFields.UPDATE_TIME)
                wearableManager.onQueueSubscriptionRequested(sourceNodeId, lastUpdateTime)
            }
            WearableEvents.REQUEST_QUEUE_EXPAND -> {
                val jsonObject = JSONObject(String(messageData))
                val isForward = jsonObject.getBoolean(WearableFields.IS_FORWARD)
                val borderItemId = jsonObject.getLong(WearableFields.BORDER_ITEM_ID)
                val itemsCount = jsonObject.getInt(WearableFields.ITEMS_COUNT)
                val expandSize = jsonObject.getInt(WearableFields.EXPAND_SIZE)
                val lastUpdateTime = jsonObject.getLong(WearableFields.UPDATE_TIME)
                wearableManager.onQueueExpandRequested(
                    sourceNodeId,
                    isForward,
                    borderItemId,
                    itemsCount,
                    expandSize,
                    lastUpdateTime
                )
            }
            WearableEvents.CANCEL_QUEUE_SUBSCRIPTION -> {
                wearableManager.onQueueSubscriptionCancel(sourceNodeId)
            }
            WearableEvents.REQUEST_APP_STATE -> {
                val wearProtocolVersion = NumberUtils.bytesToInt(messageData)
                if (!wearableManager.checkProtocolVersion(wearProtocolVersion, sourceNodeId)) {
                    return
                }
                val wearLastUpdateTime = NumberUtils.bytesToLong(messageData, Int.SIZE_BYTES)
                wearableManager.onAppStateRequested(wearLastUpdateTime, sourceNodeId)
            }
            WearableEvents.REQUEST_ACTUAL_STATE -> {
                val volume = NumberUtils.bytesToLong(messageData)
                wearableManager.onActualStateRequested(VolumeState.from(volume), sourceNodeId)
            }
        }
    }

}