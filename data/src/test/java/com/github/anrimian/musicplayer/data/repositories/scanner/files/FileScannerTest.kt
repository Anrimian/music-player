package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags
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
    private val composition2: FullComposition = mock {
        on { id } doReturn 2
    }

    private val sourceTags: CompositionSourceTags = mock()

    @BeforeEach
    fun setUp() {
        whenever(stateRepository.currentFileScannerVersion).thenReturn(1)
        whenever(stateRepository.lastFileScannerVersion).thenReturn(1)

        whenever(storageSourceRepository.getStorageSource(any())).thenReturn(Maybe.just(mock()))

        whenever(compositionSourceEditor.getFullTags(any())).thenReturn(Single.just(sourceTags))


    }

    @Test
    fun `run successful scan`() {
        val composition: FullComposition = mock()

        whenever(compositionsDao.selectNextCompositionToScan(eq(0)))
            .thenReturn(Maybe.just(composition))
            .thenReturn(Maybe.empty())

        fileScanner.scheduleFileScanner()
        scheduler.triggerActions()

        verify(compositionsDao).setCompositionLastFileScanTime(any(), any())
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
        whenever(compositionsDao.selectNextCompositionToScan(eq(0)))
            .thenReturn(Maybe.error(exception))
            .thenReturn(Maybe.just(mock<FullComposition>()))

        fileScanner.scheduleFileScanner()
        scheduler.triggerActions()

        verify(compositionsDao, never()).setCompositionLastFileScanTime(any(), any())
        verify(stateRepository, never()).lastFileScannerVersion = any()
        verify(stateRepository, never()).lastCompleteScanTime = any()
        verify(analytics).processNonFatalError(exception)

        testStateObserver.assertValues(
            Idle
        )
    }

    @Test
    fun `error with scan - set scan time and run next loop`() {
        whenever(compositionsDao.selectNextCompositionToScan(any()))
            .thenReturn(Maybe.just(composition1))
            .thenReturn(Maybe.just(composition2))
            .thenReturn(Maybe.empty())

        val exception = RuntimeException()
        Mockito.doThrow(exception)
            .doThrow(exception)
            .doThrow(exception)
            .doNothing()
            .whenever(compositionsDao).updateCompositionBySourceTags(any(), any())

        fileScanner.scheduleFileScanner()
        scheduler.triggerActions()

        verify(compositionsDao, times(2)).setCompositionLastFileScanTime(any(), any())
        verify(analytics).processNonFatalError(eq(exception))
        verify(stateRepository).lastFileScannerVersion = eq(1)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
            Idle,
            Running(composition1),
            Running(composition2),
            Idle
        )
    }

    @Test
    fun `test file scanner version update`() {
        whenever(stateRepository.currentFileScannerVersion).thenReturn(2)
        val lastScanTime = 1000L
        whenever(stateRepository.lastCompleteScanTime).thenReturn(lastScanTime)

        val composition: FullComposition = mock()

        whenever(compositionsDao.selectNextCompositionToScan(any()))
            .thenReturn(Maybe.just(composition))
            .thenReturn(Maybe.empty())

        fileScanner.scheduleFileScanner()
        scheduler.triggerActions()

        verify(compositionsDao, times(2)).selectNextCompositionToScan(eq(lastScanTime))
        verify(compositionsDao).setCompositionLastFileScanTime(eq(composition), any())
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
        val tags: CompositionSourceTags = mock()

        whenever(compositionsDao.selectNextCompositionToScan(any()))
            .thenReturn(Maybe.just(composition))
            .thenReturn(Maybe.empty())
        whenever(compositionSourceEditor.getFullTags(any()))
            .thenReturn(Single.just(tags).delay(3, TimeUnit.SECONDS, scheduler))
            .thenReturn(Single.just(tags).delay(3, TimeUnit.SECONDS, scheduler))
            .thenReturn(Single.just(tags))

        fileScanner.scheduleFileScanner()
        scheduler.advanceTimeBy(4, TimeUnit.SECONDS)

        verify(storageSourceRepository, times(3)).getStorageSource(any())
        verify(compositionSourceEditor, times(3)).getFullTags(any())
        verify(compositionsDao).setCompositionLastFileScanTime(eq(composition), any())
        verify(compositionsDao).updateCompositionBySourceTags(eq(composition), eq(tags))
        verify(stateRepository).lastFileScannerVersion = eq(1)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
            Idle,
            Running(composition),
            Idle
        )

    }
}