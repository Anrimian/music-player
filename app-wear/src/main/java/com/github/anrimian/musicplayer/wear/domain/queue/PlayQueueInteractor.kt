package com.github.anrimian.musicplayer.wear.domain.queue

import com.github.anrimian.common.WearableEvents
import com.github.anrimian.common.WearableFields
import com.github.anrimian.musicplayer.wear.data.repositories.HostDeviceRepository
import com.github.anrimian.musicplayer.wear.domain.models.PlayQueueItem
import com.github.anrimian.utils.list.ListUpdateCommand
import com.github.anrimian.utils.list.ListUpdateUtil
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.json.JSONObject

class PlayQueueInteractor(
    private val hostDeviceRepository: HostDeviceRepository
) {

    private val playQueueSubject = BehaviorSubject.createDefault(ArrayList<PlayQueueItem>())

    private var lastUpdateTime = 0L

    //TODO-W if error -> show error state
    fun startObserveQueue() {
        val jsonObject = JSONObject()
        jsonObject.put(WearableFields.UPDATE_TIME, lastUpdateTime)
        hostDeviceRepository.sendEvent(
            WearableEvents.REQUEST_QUEUE_SUBSCRIPTION,
            jsonObject.toString().toByteArray(),
        )
    }

    fun stopObserveQueue() {
        hostDeviceRepository.sendEvent(WearableEvents.CANCEL_QUEUE_SUBSCRIPTION)
    }

    fun onPlayQueueReceived(updateTime: Long, queue: ArrayList<PlayQueueItem>) {
        lastUpdateTime = updateTime
        playQueueSubject.onNext(queue)
    }

    fun onPlayQueueUpdateReceived(
        updateTime: Long,
        updateMessage: ArrayList<ListUpdateCommand<PlayQueueItem>>
    ) {
        lastUpdateTime = updateTime
        ListUpdateUtil.applyDiff(ArrayList(playQueueSubject.value!!), updateMessage)
    }

    fun getPlayQueueObservable(): Observable<ArrayList<PlayQueueItem>> = playQueueSubject

}