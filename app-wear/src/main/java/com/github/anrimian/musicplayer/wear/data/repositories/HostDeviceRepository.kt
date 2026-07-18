package com.github.anrimian.musicplayer.wear.data.repositories

import android.annotation.SuppressLint
import android.util.Log
import com.github.anrimian.common.WearableFields
import com.github.anrimian.musicplayer.wear.domain.models.DeviceState
import com.github.anrimian.musicplayer.wear.domain.models.ErrorEvent
import com.github.anrimian.musicplayer.wear.infrastructure.DeviceConnectionController
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.LinkedList
import java.util.concurrent.TimeUnit

class HostDeviceRepository(
    private val deviceConnectionController: DeviceConnectionController,
    private val ioScheduler: Scheduler,
    private val timeoutMillis: Long
) {

    private val requests = HashMap<Any, StateRequests>()

    private val errorEventSubject = PublishSubject.create<ErrorEvent>()

    private val deviceAvailabilitySubject = BehaviorSubject.create<Boolean>()

    //TODO-W can be united with NodesAvailabilityObservable
    private val deviceAvailabilityObservable by lazy {
        deviceAvailabilitySubject.startWith(deviceConnectionController.isDeviceConnected())
            .map { isAvailable -> toDeviceState(isAvailable) }
            .distinctUntilChanged()
    }

    fun onDeviceAvailabilityChanged(isAvailable: Boolean) {
        deviceAvailabilitySubject.onNext(isAvailable)
    }

    fun getHostDeviceAvailabilityObservable() = deviceAvailabilityObservable

    /**
     * @param fallback will be not called for non-last event in multiple events case
     */
    fun sendEventWithTimeout(
        eventName: String,
        message: ByteArray? = null,
        targetNode: String? = null,
        fallback: (() -> Unit)?
    ) {
        val timeoutDisposable = Single.fromCallable {
            val requests = getRequests(eventName)
            val shouldIgnoreResponse = requests.size > 1
            requests.removeFirst()
            if (!shouldIgnoreResponse) {
                fallback?.invoke()
            }
            return@fromCallable shouldIgnoreResponse
        }.delaySubscription(timeoutMillis, TimeUnit.MILLISECONDS, ioScheduler)
            .subscribe { ignore ->
                if (ignore) {
                    return@subscribe
                }
                errorEventSubject.onNext(
                    ErrorEvent(WearableFields.ERROR_NO_ACK_EVENT, eventName)
                )
            }

        val fallbackAction = Completable.fromAction {
            if (!onRequestFinished(eventName)) {
                fallback?.invoke()
            }
        }
        val stateRequests = getStateRequests(eventName)
        stateRequests.requests.addLast(RequestInfo(fallbackAction, timeoutDisposable))
        val requestId = if (stateRequests.requests.size > 1) {
            stateRequests.lastSentRequestId + 1
        } else {
            1
        }
        stateRequests.lastSentRequestId = requestId
        Log.d("KEK", "sendEventWithTimeout: $eventName, requestId: $requestId")

        sendEvent(eventName, message, targetNode, requestId)
    }

    @SuppressLint("CheckResult")
    fun sendEvent(
        eventName: String,
        message: ByteArray? = null,
        targetNode: String? = null,
        requestId: Int? = null
    ) {
        deviceConnectionController.sendEvent(eventName, message, targetNode, requestId)
            .subscribe(
                {},
                { t ->
                    onRequestFailed(eventName)
                    errorEventSubject.onNext(ErrorEvent(WearableFields.ERROR_SEND_EVENT, eventName, t))
                }
            )
    }

    fun onRequestErrorReceived(errorType: Int, sentEventName: String) {
        onRequestFailed(sentEventName)
        errorEventSubject.onNext(ErrorEvent(errorType, sentEventName))
    }

    fun onRequestFinished(eventName: String, requestId: Int? = null): Boolean {
        val stateRequests = getStateRequests(eventName)
        val requests = stateRequests.requests
        if (stateRequests.lastSentRequestId == requestId) {
            while (requests.isNotEmpty()) {
                requests.removeFirst().timeoutDisposable.dispose()
            }
            stateRequests.lastSentRequestId = 0
            return false
        }
        val shouldIgnoreResponse = requests.size > 1
        val firstRequest = requests.peekFirst() ?: return shouldIgnoreResponse
        firstRequest.timeoutDisposable.dispose()
        requests.removeFirst()
        return shouldIgnoreResponse
    }

    fun hasActiveRequest(evenName: String): Boolean {
        return requests.containsKey(evenName)
    }

    fun getErrorEventsObservable(): Observable<ErrorEvent> = errorEventSubject

    private fun onRequestFailed(eventName: String) {
        val requests = getRequests(eventName)
        val firstRequest = requests.peekFirst() ?: return
        firstRequest.fallback.subscribe()//list will be reduced here
        firstRequest.timeoutDisposable.dispose()
    }

    private fun getRequests(eventName: String): LinkedList<RequestInfo> {
        return getStateRequests(eventName).requests
    }

    private fun getStateRequests(eventName: String): StateRequests {
        return requests.getOrPut(eventName, ::StateRequests)
    }

    private fun toDeviceState(isAvailable: Boolean): DeviceState {
        if (!isAvailable) {
            return DeviceState.NOT_CONNECTED
        }
        return DeviceState.CONNECTED
    }

    private class StateRequests {
        var lastSentRequestId = 0
        val requests = LinkedList<RequestInfo>()
    }

    private class RequestInfo(val fallback: Completable, val timeoutDisposable: Disposable)

}