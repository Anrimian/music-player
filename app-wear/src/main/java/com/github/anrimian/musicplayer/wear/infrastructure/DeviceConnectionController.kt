package com.github.anrimian.musicplayer.wear.infrastructure

import android.content.Context
import com.github.anrimian.common.AppWearUtils
import com.github.anrimian.musicplayer.wear.R
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single

class DeviceConnectionController(
    private val context: Context,
    private val ioScheduler: Scheduler
) {

    private val pathPrefix = context.getString(R.string.wear_path_prefix)
    private val deviceCapability = context.getString(R.string.wear_device_capability)

    fun isDeviceConnected(): Single<Boolean> {
        return Single.fromCallable {
            try {
                val capabilityTask = Wearable.getCapabilityClient(context)
                    .getCapability(deviceCapability, CapabilityClient.FILTER_ALL)
                val capability = Tasks.await(capabilityTask)
                capability.nodes.isNotEmpty()
            } catch (e: Exception) {
//            analytics.processNonFatalError(e)
                false
            }
        }.subscribeOn(ioScheduler)
    }

    fun sendEvent(
        eventName: String,
        message: ByteArray? = null,
        targetNode: String? = null,
        requestId: Int? = null
    ): Completable {
        return Completable.fromCallable { sendMessage(eventName, message, targetNode, requestId) }
            .subscribeOn(ioScheduler)
    }

    private fun sendMessage(
        eventName: String,
        message: ByteArray? = null,
        targetNode: String? = null,
        requestId: Int? = null
    ) {
        val wearableList: Task<List<Node>> = Wearable.getNodeClient(context).connectedNodes
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
    }

}