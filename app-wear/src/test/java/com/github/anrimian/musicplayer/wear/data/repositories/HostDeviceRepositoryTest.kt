package com.github.anrimian.musicplayer.wear.data.repositories

import com.github.anrimian.common.WearableFields
import com.github.anrimian.musicplayer.wear.domain.models.ErrorEvent
import com.github.anrimian.musicplayer.wear.infrastructure.DeviceConnectionController
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class HostDeviceRepositoryTest {

    private val deviceConnectionController: DeviceConnectionController = mock {
        on { sendEvent(any(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturn Completable.complete()
    }
    private val scheduler = TestScheduler()

    private val deviceRepository = HostDeviceRepository(
        deviceConnectionController,
        scheduler,
        2000
    )

    private val errorObserver = deviceRepository.getErrorEventsObservable().test()

    private val eventName = "test event"

    @Test
    fun `send event, got ack event`() {
        var isFallbackCalled = false
        val fallback = { isFallbackCalled = true }
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        val shouldIgnoreResponse = deviceRepository.onRequestFinished(eventName)
        assertEquals(false, shouldIgnoreResponse)
        scheduler.advanceTimeBy(4000, TimeUnit.MILLISECONDS)

        assertFalse(isFallbackCalled, { "fallback should be never called" } )
        errorObserver.assertNoValues()
    }

    @Test
    fun `send event, didn't get ack event`() {
        var isFallbackCalled = false
        val fallback = { isFallbackCalled = true }
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        scheduler.advanceTimeBy(4000, TimeUnit.MILLISECONDS)

        assert(isFallbackCalled, { "fallback should be called" } )
        errorObserver.assertValue(ErrorEvent(WearableFields.ERROR_NO_ACK_EVENT, eventName))
    }

    @Test
    fun `send event, error on send`() {
        val ex = mock<Exception>()
        whenever(deviceConnectionController.sendEvent(any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .doReturn(Completable.error(ex))
        var isFallbackCalled = false
        val fallback = { isFallbackCalled = true }
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        scheduler.advanceTimeBy(3000, TimeUnit.MILLISECONDS)

        assert(isFallbackCalled, { "fallback should be called" } )
        errorObserver.assertValue(ErrorEvent(WearableFields.ERROR_SEND_EVENT, eventName, ex))
    }

    @Test
    fun `send event, got error event`() {
        var isFallbackCalled = false
        val fallback = { isFallbackCalled = true }
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        deviceRepository.onRequestErrorReceived(WearableFields.ERROR_NO_PERMISSION, eventName)
        scheduler.advanceTimeBy(3000, TimeUnit.MILLISECONDS)

        assert(isFallbackCalled, { "fallback should be called" } )
        errorObserver.assertValue(ErrorEvent(WearableFields.ERROR_NO_PERMISSION, eventName))
    }

    @Test
    fun `send event twice, got ack events`() {
        var isFallbackCalled = false
        val fallback = { isFallbackCalled = true }
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        val shouldIgnoreResponse1 = deviceRepository.onRequestFinished(eventName)
        assertEquals(true, shouldIgnoreResponse1)
        val shouldIgnoreResponse2 = deviceRepository.onRequestFinished(eventName)
        assertEquals(false, shouldIgnoreResponse2)
        scheduler.advanceTimeBy(4000, TimeUnit.MILLISECONDS)

        assertFalse(isFallbackCalled, { "fallback should be never called" } )
        errorObserver.assertNoValues()
    }

    @Test
    fun `send event twice, got error and ack events`() {
        var isFallbackCalled = false
        val fallback = { isFallbackCalled = true }
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        deviceRepository.onRequestErrorReceived(WearableFields.ERROR_NO_PERMISSION, eventName)
        val shouldIgnoreResponse = deviceRepository.onRequestFinished(eventName)
        assertEquals(false, shouldIgnoreResponse)
        scheduler.advanceTimeBy(4000, TimeUnit.MILLISECONDS)

        assertFalse(isFallbackCalled, { "fallback should be never called" } )
        errorObserver.assertValue(ErrorEvent(WearableFields.ERROR_NO_PERMISSION, eventName))
    }

    @Test
    fun `send event twice, got send error and ack events`() {
        val ex = mock<Exception>()
        whenever(deviceConnectionController.sendEvent(any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .doReturn(Completable.error(ex))
            .doReturn(Completable.complete())
        var isFallbackCalled = false
        val fallback = { isFallbackCalled = true }
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        val shouldIgnoreResponse = deviceRepository.onRequestFinished(eventName)
        assertEquals(false, shouldIgnoreResponse)
        scheduler.advanceTimeBy(4000, TimeUnit.MILLISECONDS)

        assert(isFallbackCalled, { "fallback should be called" } )
        //because it's called before second request is placed
        errorObserver.assertValue(ErrorEvent(WearableFields.ERROR_SEND_EVENT, eventName, ex))
    }

    @Test
    fun `send event twice, got two timeouts`() {
        var fallbackCallCount = 0
        val fallback: () -> Unit = { fallbackCallCount++ }
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        deviceRepository.sendEventWithTimeout(eventName, fallback = fallback)
        scheduler.advanceTimeBy(4000, TimeUnit.MILLISECONDS)

        assertEquals(1, fallbackCallCount, { "fallback should be called once" } )
        errorObserver.assertValue(ErrorEvent(WearableFields.ERROR_NO_ACK_EVENT, eventName))
    }

}