package com.github.anrimian.musicplayer.wear.utils.logger

//TODO-W pass analytics to device
class WearableFileLogger {

    /*
    subscribe on action 'event sent' from DeviceConnectionController OR on message received action from DeviceListenerService
    after 10-15 seconds after (success) event sent - send one piece of exc or message
    + reset timer when we receive another event
    after send lock listener and sending
    await success event from DeviceListenerService
    on success -> delete sent log part and send next part
     */

    fun writeMessage(message: String) {

    }

    fun writeException(throwable: Throwable, message: String?) {

    }

    fun onMessageReceived() {

    }

    fun onAnalyticsAckReceived() {

    }

}