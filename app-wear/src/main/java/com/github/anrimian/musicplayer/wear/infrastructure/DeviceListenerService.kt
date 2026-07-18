package com.github.anrimian.musicplayer.wear.infrastructure

import com.github.anrimian.common.BaseWearableListenerService
import com.github.anrimian.common.DeviceWearableEvent
import com.github.anrimian.common.WearableFields
import com.github.anrimian.data.WearableModelsHelper
import com.github.anrimian.musicplayer.data.utils.optionalLong
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.utils.NumberUtils
import com.github.anrimian.musicplayer.wear.R
import com.github.anrimian.musicplayer.wear.di.Components
import com.github.anrimian.musicplayer.wear.domain.models.PlayQueueItem
import com.github.anrimian.utils.list.ListUpdateCommand
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageEvent
import org.json.JSONObject

class DeviceListenerService: BaseWearableListenerService() {

    override fun onCapabilityChanged(info: CapabilityInfo) {
        super.onCapabilityChanged(info)
        if (info.name == getString(R.string.wear_device_capability)) {
            val wearStateInteractor = Components.getAppComponent().wearStateInteractor()
            wearStateInteractor.onDeviceAvailabilityChanged(info.nodes.isNotEmpty())
            info.nodes.forEach { node ->
                wearStateInteractor.onDeviceConnected(node.id)
            }
        }
    }

    override fun onAppMessageReceived(
        messageEvent: MessageEvent,
        eventName: String,
        requestId: Int?,
    ) {
        val messageData = messageEvent.data
        val wearStateInteractor = Components.getAppComponent().wearStateInteractor()
        when(eventName) {
            DeviceWearableEvent.PLAY_PAUSE -> {
                val isPlaying = NumberUtils.bytesToBoolean(messageData)
                val lastUpdateTime = NumberUtils.bytesToLong(messageData, 1)
                wearStateInteractor.onPlayingStateReceived(isPlaying, lastUpdateTime)
            }
            DeviceWearableEvent.SOURCE -> {
                val jsonObject = JSONObject(String(messageData))
                val lastUpdateTime = jsonObject.getLong(WearableFields.UPDATE_TIME)
                val currentQueueItem = WearableModelsHelper.deserializeComposition(
                    jsonObject.getJSONObject(WearableFields.CURRENT_COMPOSITION)
                )
                wearStateInteractor.onCurrentSourceReceived(currentQueueItem, lastUpdateTime)
            }
            DeviceWearableEvent.QUEUE_ITEM_ID -> {
                val itemId = NumberUtils.bytesToLong(messageData).takeIf { v -> v > 0 }
                val lastUpdateTime = NumberUtils.bytesToLong(messageData, Long.SIZE_BYTES)
                wearStateInteractor.onPlayQueueItemIdReceived(itemId, lastUpdateTime)
            }
            DeviceWearableEvent.VOLUME -> {
                val volume = VolumeState.from(NumberUtils.bytesToLong(messageData))
                wearStateInteractor.onVolumeStateReceived(volume, requestId)
            }
            DeviceWearableEvent.STATE -> {
                val jsonObject = JSONObject(String(messageData))
                val protocolVersion = jsonObject.getInt(WearableFields.PROTOCOL_VERSION)
                if (!wearStateInteractor.checkProtocolVersion(protocolVersion)) {
                    return
                }
                val lastUpdateTime = jsonObject.getLong(WearableFields.UPDATE_TIME)
                val isPlaying = jsonObject.getBoolean(WearableFields.IS_PLAYING)
                val position = jsonObject.getLong(WearableFields.POSITION)
                val queueItemId = jsonObject.optionalLong(WearableFields.QUEUE_ITEM_ID)
                val currentComposition = WearableModelsHelper.deserializeComposition(
                    jsonObject.getJSONObject(WearableFields.CURRENT_COMPOSITION)
                )
                val currentVolume = VolumeState.from(jsonObject.getLong(WearableFields.VOLUME))
                val playbackSpeed = jsonObject.getDouble(WearableFields.SPEED).toFloat()
                val randomMode = jsonObject.getBoolean(WearableFields.RANDOM_MODE)
                val repeatMode = jsonObject.getInt(WearableFields.REPEAT_MODE)
                wearStateInteractor.onCurrentStateReceived(
                    isPlaying,
                    queueItemId,
                    currentComposition,
                    position,
                    currentVolume,
                    lastUpdateTime,
                    playbackSpeed,
                    randomMode,
                    repeatMode
                )
            }
            DeviceWearableEvent.POSITION -> {
                val position = NumberUtils.bytesToLong(messageData)
                wearStateInteractor.onTrackPositionReceived(position)
            }
            DeviceWearableEvent.SPEED -> {
                val speed = NumberUtils.bytesToFloat(messageData)
                val lastUpdateTime = NumberUtils.bytesToLong(messageData, Float.SIZE_BYTES)

            }
            DeviceWearableEvent.RANDOM_MODE -> {
                val isRandom = NumberUtils.bytesToBoolean(messageData)
                val lastUpdateTime = NumberUtils.bytesToLong(messageData, 1)

            }
            DeviceWearableEvent.REPEAT_MODE -> {
                val repeatMode = NumberUtils.bytesToInt(messageData)
                val lastUpdateTime = NumberUtils.bytesToLong(messageData, Int.SIZE_BYTES)

            }
            DeviceWearableEvent.ERROR -> {
                val jsonObject = JSONObject(String(messageData))
                val errorType = jsonObject.getInt(WearableFields.ERROR_TYPE)
                val sentEventName = jsonObject.getString(WearableFields.SENT_EVENT_NAME)
                wearStateInteractor.onErrorReceived(errorType, sentEventName)
            }
            DeviceWearableEvent.ERROR_MESSAGE -> {
                val jsonObject = JSONObject(String(messageData))
                val errorMessage = jsonObject.getString(WearableFields.TEXT)

            }
            DeviceWearableEvent.PLAY_QUEUE_NEW -> {
                val jsonObject = JSONObject(String(messageData))
                val contentUpdateTime = jsonObject.getLong(WearableFields.CONTENT_UPDATE_TIME)
                val itemsArray = jsonObject.getJSONArray(WearableFields.LIST)
                val items = ArrayList<PlayQueueItem>(itemsArray.length())
                for (i in 0 until itemsArray.length()) {
                    val item = itemsArray.getJSONObject(i)
                    items.add(deserializeQueueItem(item))
                }
                Components.getAppComponent()
                    .playQueueInteractor()
                    .onPlayQueueReceived(contentUpdateTime, items)
            }
            DeviceWearableEvent.PLAY_QUEUE_UPDATE -> {
                val jsonObject = JSONObject(String(messageData))
                val contentUpdateTime = jsonObject.getLong(WearableFields.CONTENT_UPDATE_TIME)
                val updatesArray = jsonObject.getJSONArray(WearableFields.LIST_UPDATE)
                val updateMessage = ArrayList<ListUpdateCommand<PlayQueueItem>>(updatesArray.length())
                for (i in 0 until updatesArray.length()) {
                    val update = updatesArray.getJSONObject(i)
                    val updateCommand: ListUpdateCommand<PlayQueueItem> = when (update.getInt(WearableFields.UPDATE_TYPE)) {
                        WearableFields.UPDATE_INSERT -> {
                            ListUpdateCommand.Insert(
                                update.getInt(WearableFields.INDEX),
                                deserializeQueueItem(update.getJSONObject(WearableFields.ITEM)),
                            )
                        }
                        WearableFields.UPDATE_REMOVE -> {
                            ListUpdateCommand.Remove(
                                update.getInt(WearableFields.INDEX)
                            )
                        }
                        WearableFields.UPDATE_MOVE -> {
                            ListUpdateCommand.Move(
                                update.getInt(WearableFields.FROM),
                                update.getInt(WearableFields.TO)
                            )
                        }
                        WearableFields.UPDATE_STUB_INSERT -> {
                            ListUpdateCommand.StubInsert(
                                update.getInt(WearableFields.POSITION),
                                update.getInt(WearableFields.COUNT)
                            )
                        }
                        WearableFields.UPDATE_MODIFY -> {
                            ListUpdateCommand.Update(
                                update.getInt(WearableFields.INDEX),
                                deserializeQueueItem(update.getJSONObject(WearableFields.ITEM)),
                            )
                        }
                        else -> throw IllegalStateException()
                    }
                    updateMessage.add(updateCommand)
                }
                Components.getAppComponent()
                    .playQueueInteractor()
                    .onPlayQueueUpdateReceived(contentUpdateTime, updateMessage)
            }
        }
    }

    private fun deserializeQueueItem(jsonObject: JSONObject): PlayQueueItem {
        return PlayQueueItem(
            jsonObject.getLong(WearableFields.ID),
            jsonObject.getLong(WearableFields.ITEM_ID),
            jsonObject.getString(WearableFields.TITLE),
            jsonObject.optString(WearableFields.ARTIST),
            jsonObject.getLong(WearableFields.DURATION),
        )
    }

}