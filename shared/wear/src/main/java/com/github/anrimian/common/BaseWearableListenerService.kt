package com.github.anrimian.common

import com.github.anrimian.musicplayer.shared.wear.R
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

//TODO-W consider send request id in first byte in message data
abstract class BaseWearableListenerService: WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val pathPrefix = getString(R.string.wear_path_prefix)
        val path = messageEvent.path
        if (!path.startsWith(pathPrefix)) {
            return
        }
        val idDelimiterIndex = path.indexOf('-', startIndex = pathPrefix.length)
        val eventNameEnd = if (idDelimiterIndex == -1) path.length else idDelimiterIndex
        val eventName = path.substring(pathPrefix.length, eventNameEnd)
        val requestId = if (idDelimiterIndex == -1) {
            null
        } else {
            path.substring(idDelimiterIndex + 1, path.length).toInt()
        }
        onAppMessageReceived(messageEvent, eventName, requestId)
    }

    protected abstract fun onAppMessageReceived(
        messageEvent: MessageEvent,
        eventName: String,
        requestId: Int?,
    )
}