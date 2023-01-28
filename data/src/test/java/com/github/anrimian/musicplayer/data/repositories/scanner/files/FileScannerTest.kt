package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.tags.AudioFileInfo
import com.github.anrimian.musicplayer.domain.models.scanner.Idle
import com.github.anrimian.musicplayer.domain.models.scanner.Running
import com.github.anrimian.musicplayer.domain.repositories.StateRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit

class FileScannerTest {

    private val compositionsDao: CompositionsDaoWrapper = mock()
    private val compositionSourceEditor: CompositionSourceEditor = mock()
    private val stateRepository: StateRepository = mock()
    private val storageSourceRepository: StorageSourceRepository = mock()
    private val analytics: Analytics = mock()
    private val scheduler = TestScheduler()

    private val fileScanner = FileScanner(
        compositionsDao,
        compositionSourceEditor,
        stateRepository,
        storageSourceRepository,
        analytics,
        scheduler
    )

    private val testStateObserver = fileScanner.getStateObservable().test()

    private val composition1: FullComposition = mock {
        on { id } doReturn 1
    }

    private val fileInfo: AudioFileInfo = mock()

    @BeforeEach
    fun setUp() {
        whenever(stateRepository.currentFileScannerVersion).thenReturn(1)
        whenever(stateRepository.lastFileScannerVersion).thenReturn(1)

        whenever(storageSourceRepository.getStorageSource(any())).thenReturn(Maybe.just(mock()))

        whenever(compositionSourceEditor.getAudioFileInfo(any())).thenReturn(Single.just(fileInfo))


    }

    @Test
    fun `run successful scan`() {
        val composition: FullComposition = mock()

        whenever(compositionsDao.selectNextCompositionsToScan(eq(0), any()))
            .thenReturn(Single.just(listOf(composition)))
            .thenReturn(Single.just(emptyList()))

        fileScanner.scheduleFileScanner()
        scheduler.triggerActions()

        verify(compositionsDao).updateCompositionsByFileInfo(any(), any())
        verify(stateRepository).lastFileScannerVersion = eq(1)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
            Idle,
            Running(composition),
            Idle
        )
    }

    @Test
    fun `error with getting composition from db - do not run next loop`() {
        val exception: Exception = mock()
        whenever(compositionsDao.selectNextCompositionsToScan(eq(0), any()))
            .thenReturn(Single.error(exception))
            .thenReturn(Single.just(listOf(mock())))

        fileScanner.scheduleFileScanner()
        scheduler.triggerActions()

        verify(compositionsDao, never()).updateCompositionsByFileInfo(any(), any())
        verify(stateRepository, never()).lastFileScannerVersion = any()
        verify(stateRepository, never()).lastCompleteScanTime = any()
        verify(analytics).processNonFatalError(exception)

        testStateObserver.assertValues(
            Idle
        )
    }

    @Test
    fun `error with scan - run update and run next loop`() {
        whenever(compositionsDao.selectNextCompositionsToScan(any(), any()))
            .thenReturn(Single.just(listOf(composition1)))
            .thenReturn(Single.just(emptyList()))

        val exception = RuntimeException()
        Mockito.doThrow(exception)
            .doThrow(exception)
            .doThrow(exception)
            .doNothing()
            .whenever(compositionsDao).updateCompositionsByFileInfo(any(), any())

        fileScanner.scheduleFileScanner()
        scheduler.triggerActions()

        verify(compositionsDao).updateCompositionsByFileInfo(any(), any())
        verify(analytics).processNonFatalError(eq(exception))
        verify(stateRepository).lastFileScannerVersion = eq(1)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
            Idle,
            Running(composition1),
            Idle
        )
    }

    @Test
    fun `test file scanner version update`() {
        whenever(stateRepository.currentFileScannerVersion).thenReturn(2)
        val lastScanTime = 1000L
        whenever(stateRepository.lastCompleteScanTime).thenReturn(lastScanTime)

        val composition: FullComposition = mock()

        whenever(compositionsDao.selectNextCompositionsToScan(any(), any()))
            .thenReturn(Single.just(listOf(composition)))
            .thenReturn(Single.just(emptyList()))

        fileScanner.scheduleFileScanner()
        scheduler.triggerActions()

        verify(compositionsDao).selectNextCompositionsToScan(eq(lastScanTime), any())
        verify(compositionsDao).updateCompositionsByFileInfo(any(), any())
        verify(stateRepository).lastFileScannerVersion = eq(2)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
            Idle,
            Running(composition),
            Idle
        )
    }

    @Test
    fun `test file read timeout`() {
        val testStateObserver = fileScanner.getStateObservable().test()

        val composition: FullComposition = mock()
        val fileInfo: AudioFileInfo = mock()

        whenever(compositionsDao.selectNextCompositionsToScan(any(), any()))
            .thenReturn(Single.just(listOf(composition)))
            .thenReturn(Single.just(emptyList()))
        whenever(compositionSourceEditor.getAudioFileInfo(any()))
            .thenReturn(Single.just(fileInfo).delay(7, TimeUnit.SECONDS, scheduler))
            .thenReturn(Single.just(fileInfo).delay(7, TimeUnit.SECONDS, scheduler))
            .thenReturn(Single.just(fileInfo))

        fileScanner.scheduleFileScanner()
        scheduler.advanceTimeBy(20, TimeUnit.SECONDS)

        verify(storageSourceRepository).getStorageSource(any())
        verify(compositionSourceEditor).getAudioFileInfo(any())
        verify(compositionsDao).updateCompositionsByFileInfo(any(), any())
        verify(stateRepository).lastFileScannerVersion = eq(1)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
            Idle,
            Running(composition),
            Idle
        )

    }
}