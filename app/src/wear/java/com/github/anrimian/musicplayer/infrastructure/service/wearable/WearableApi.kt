package com.github.anrimian.musicplayer.infrastructure.service.wearable

import android.content.Context
import android.util.Log
import com.github.anrimian.common.AppWearUtils
import com.github.anrimian.common.DeviceWearableEvent
import com.github.anrimian.common.WearableConstants
import com.github.anrimian.common.WearableFields
import com.github.anrimian.data.WearableModelsHelper
import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.utils.NumberUtils
import com.github.anrimian.utils.list.ListUpdateCommand
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import org.json.JSONArray
import org.json.JSONObject

//TODO-W send in every request protocol version?
class WearableApi(
    private val context: Context,
    private val analytics: Analytics
) {

    private val pathPrefix = context.getString(R.string.wear_path_prefix)

    fun sendCurrentState(
        targetNode: String? = null,
        lastUpdate: Long,
        isPlaying: Boolean,
        queueItemId: Long?,
        currentComposition: WearableComposition?,
        trackPosition: Long,
        volume: VolumeState,
        playbackSpeed: Float,
        randomMode: Boolean,
        repeatMode: Int
    ) {
        val state = JSONObject()
        state.put(WearableFields.PROTOCOL_VERSION, WearableConstants.WEARABLE_PROTOCOL_VERSION)
        state.put(WearableFields.UPDATE_TIME, lastUpdate)
        state.put(WearableFields.IS_PLAYING, isPlaying)
        state.put(WearableFields.QUEUE_ITEM_ID, queueItemId)
        state.put(
            WearableFields.CURRENT_COMPOSITION,
            WearableModelsHelper.serializeComposition(currentComposition)
        )
        state.put(WearableFields.POSITION, trackPosition)
        state.put(WearableFields.VOLUME, volume.toLong())
        state.put(WearableFields.SPEED, playbackSpeed)
        state.put(WearableFields.RANDOM_MODE, randomMode)
        state.put(WearableFields.REPEAT_MODE, repeatMode)
        sendMessage(DeviceWearableEvent.STATE, state.toString().toByteArray(), targetNode)
    }

    fun sendVolume(volumeState: VolumeState, senderNodeId: String? = null, requestId: Int? = null) {
        val array = ByteArray(Long.SIZE_BYTES)
        NumberUtils.longToBytes(volumeState.toLong(), array)
        sendMessage(DeviceWearableEvent.VOLUME, array, senderNodeId, requestId)
    }

    fun sendErrorEvent(errorType: Int, eventName: String, senderNodeId: String) {
        val data = JSONObject()
        data.put(WearableFields.ERROR_TYPE, errorType)
        data.put(WearableFields.SENT_EVENT_NAME, eventName)
        sendMessage(DeviceWearableEvent.ERROR, data.toString().toByteArray(), senderNodeId)
    }

    fun sendErrorMessage(message: String, senderNodeId: String) {
        val data = JSONObject()
        data.put(WearableFields.TEXT, message)
        sendMessage(DeviceWearableEvent.ERROR_MESSAGE, data.toString().toByteArray(), senderNodeId)
    }

    fun sendPlayPause(isPlaying: Boolean, updateTime: Long) {
        sendMessage(
            DeviceWearableEvent.PLAY_PAUSE,
            WearableModelsHelper.serializePlayState(isPlaying, updateTime)
        )
    }

    fun sendPlayQueueItemId(itemId: Long?, updateTime: Long) {
        val array = ByteArray(Long.SIZE_BYTES + Long.SIZE_BYTES)
        NumberUtils.longToBytes(itemId ?: 0, array)
        NumberUtils.longToBytes(updateTime, array, 1)
        sendMessage(DeviceWearableEvent.QUEUE_ITEM_ID, array)
    }

    fun sendCurrentComposition(source: WearableComposition?, updateTime: Long) {
        val jsonObject = JSONObject()
        jsonObject.put(WearableFields.UPDATE_TIME, updateTime)
        jsonObject.put(
            WearableFields.CURRENT_COMPOSITION,
            WearableModelsHelper.serializeComposition(source)
        )
        sendMessage(
            DeviceWearableEvent.SOURCE,
            jsonObject.toString().toByteArray()
        )
    }

    fun sendTrackPosition(position: Long) {
        val array = ByteArray(Long.SIZE_BYTES)
        NumberUtils.longToBytes(position, array)
        //TODO-W add protocol version?
        sendMessage(DeviceWearableEvent.POSITION, array)
    }

    fun sendPlaybackSpeed(speed: Float, updateTime: Long, requestId: Int? = null) {
        val array = ByteArray(Float.SIZE_BYTES + Long.SIZE_BYTES)
        NumberUtils.floatToBytes(speed, array)
        NumberUtils.longToBytes(updateTime, array, Float.SIZE_BYTES)
        sendMessage(DeviceWearableEvent.SPEED, array, requestId = requestId)
    }

    fun sendRandomMode(isRandom: Boolean, updateTime: Long, requestId: Int? = null) {
        val array = ByteArray(1 + Long.SIZE_BYTES)
        NumberUtils.booleanToBytes(isRandom, array)
        NumberUtils.longToBytes(updateTime, array, 1)
        sendMessage(DeviceWearableEvent.RANDOM_MODE, array, requestId = requestId)
    }

    fun sendRepeatMode(repeatMode: Int, updateTime: Long, requestId: Int? = null) {
        val array = ByteArray(Int.SIZE_BYTES + Long.SIZE_BYTES)
        NumberUtils.intToBytes(repeatMode, array)
        NumberUtils.longToBytes(updateTime, array, Int.SIZE_BYTES)
        sendMessage(DeviceWearableEvent.REPEAT_MODE, array, requestId = requestId)
    }

    fun sendPlayQueue(nodeId: String, listData: WindowListData<PlayQueueItem>) {
        Log.d("KEK", "SEND NEW QUEUE")
        val jsonObject = JSONObject()
        jsonObject.put(WearableFields.CONTENT_UPDATE_TIME, listData.updateTime)

        val jsonArray = JSONArray()
        listData.list.forEach { item -> jsonArray.put(serializeQueueItem(item)) }
        jsonObject.put(WearableFields.LIST, jsonArray)

        sendMessage(
            DeviceWearableEvent.PLAY_QUEUE_NEW,
            jsonObject.toString().toByteArray(),
            nodeId
        )
    }

    fun sendPlayQueueUpdate(
        nodeId: String,
        updateMessage: List<ListUpdateCommand<PlayQueueItem>>,
        listData: WindowListData<PlayQueueItem>
    ) {
        Log.d("KEK", "SEND UPDATE QUEUE")
        val jsonObject = JSONObject()
        jsonObject.put(WearableFields.CONTENT_UPDATE_TIME, listData.updateTime)

        val jsonArray = JSONArray()
        updateMessage.forEach { update ->
            val jsonItem = JSONObject()
            when (update) {
                is ListUpdateCommand.Insert -> {
                    jsonItem.put(WearableFields.UPDATE_TYPE, WearableFields.UPDATE_INSERT)
                    jsonItem.put(WearableFields.ITEM, serializeQueueItem(update.item))
                    jsonItem.put(WearableFields.INDEX, update.index)
                }
                is ListUpdateCommand.Remove -> {
                    jsonItem.put(WearableFields.UPDATE_TYPE, WearableFields.UPDATE_REMOVE)
                    jsonItem.put(WearableFields.INDEX, update.index)
                }
                is ListUpdateCommand.Move -> {
                    jsonItem.put(WearableFields.UPDATE_TYPE, WearableFields.UPDATE_MOVE)
                    jsonItem.put(WearableFields.FROM, update.from)
                    jsonItem.put(WearableFields.TO, update.to)
                }
                is ListUpdateCommand.StubInsert -> {
                    jsonItem.put(WearableFields.UPDATE_TYPE, WearableFields.UPDATE_STUB_INSERT)
                    jsonItem.put(WearableFields.POSITION, update.position)
                    jsonItem.put(WearableFields.COUNT, update.count)
                }
                is ListUpdateCommand.Update -> {
                    jsonItem.put(WearableFields.UPDATE_TYPE, WearableFields.UPDATE_MODIFY)
                    jsonItem.put(WearableFields.ITEM, serializeQueueItem(update.item))
                    jsonItem.put(WearableFields.INDEX, update.index)
                }
            }
            jsonArray.put(jsonItem)
        }
        jsonObject.put(WearableFields.LIST_UPDATE, jsonArray)

        sendMessage(
            DeviceWearableEvent.PLAY_QUEUE_UPDATE,
            jsonObject.toString().toByteArray(),
            nodeId
        )
    }

    private fun serializeQueueItem(source: PlayQueueItem): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(WearableFields.ID, source.id)
        jsonObject.put(WearableFields.ITEM_ID, source.itemId)
        jsonObject.put(WearableFields.TITLE, source.title)
        jsonObject.put(WearableFields.ARTIST, source.artist)
        jsonObject.put(WearableFields.DURATION, source.duration)
        return jsonObject
    }

    private fun sendMessage(
        eventName: String,
        message: ByteArray,
        targetNode: String? = null,
        requestId: Int? = null,
    ) {
        val wearableList = Wearable.getNodeClient(context).connectedNodes
        try {
            val nodes: List<Node> = Tasks.await(wearableList)
            for (node in nodes) {
                if (targetNode != null && targetNode != node.id) {
                    continue
                }
                val eventPath = AppWearUtils.buildEventPath(pathPrefix, eventName, requestId)
                val sendMessageTask = Wearable.getMessageClient(context)
                    .sendMessage(node.id, eventPath, message)
                Tasks.await(sendMessageTask)
            }
        } catch (e: Exception) {
            analytics.processNonFatalError(e)
        }
    }

}