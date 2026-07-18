package com.github.anrimian.musicplayer.wear.domain

import com.github.anrimian.common.WearableEvents
import com.github.anrimian.common.WearableFields
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.wear.data.WearStateRepository
import com.github.anrimian.musicplayer.wear.data.repositories.HostDeviceRepository
import com.github.anrimian.musicplayer.wear.domain.controllers.RemoteStateController
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never

class WearStateInteractorTest {

    private val wearStateRepository: WearStateRepository = mock {
        var isPlaying = false
        on { isPlaying() } doAnswer { isPlaying }
        on { setIsPlaying(eq(true)) } doAnswer { isPlaying = true }
        on { setIsPlaying(eq(false)) } doAnswer { isPlaying = false }

        on { getLastUpdateTime() } doReturn WearableFields.NO_STATE_TIME

        var volumeState = VolumeState.from(0, 5)
        val volumeArgumentCaptor = ArgumentCaptor.captor<Int>()
        on { getVolumeState() } doReturn volumeState
        on { setCurrentVolume(volumeArgumentCaptor.capture()) } doAnswer {
            volumeState = VolumeState.from(volumeArgumentCaptor.value, 5)
        }
    }

    private val remoteStateController: RemoteStateController = mock()
    private val deviceRepository: HostDeviceRepository = mock()

    private val wearStateInteractor = WearStateInteractor(
        wearStateRepository,
        deviceRepository,
        remoteStateController
    )

    private val inOrder = inOrder(wearStateRepository, deviceRepository, remoteStateController)

    @Test
    fun `send play pause, get state`() {
        val trackPosition = 200L
        val updateTime = 100L
        wearStateInteractor.playPause()
        inOrder.verify(wearStateRepository).setIsPlaying(true)
        inOrder.verify(remoteStateController).setIsPlaying(true)
        inOrder.verify(wearStateRepository, never()).setCurrentTrackPosition(any())
        inOrder.verify(deviceRepository).sendEventWithTimeout(
            eq(WearableEvents.PLAY_PAUSE),
            message = isNull(),
            targetNode = isNull(),
            fallback = any()
        )

        wearStateInteractor.onPlayingStateReceived(true, updateTime)
        inOrder.verify(wearStateRepository).setLastUpdateTime(eq(updateTime))
        inOrder.verify(wearStateRepository).setCurrentTrackPosition(eq(trackPosition))
        inOrder.verify(wearStateRepository, never()).setIsPlaying(any())
    }

}