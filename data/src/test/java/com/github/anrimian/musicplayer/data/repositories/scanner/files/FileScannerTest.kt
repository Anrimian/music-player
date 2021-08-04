package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.*

class FileScannerTest {

    private val compositionsDao: CompositionsDaoWrapper = mock()
    private val compositionSourceEditor: CompositionSourceEditor = mock()
    private val analytics: Analytics = mock()
    private val scheduler = Schedulers.trampoline()

    private val fileScanner = FileScanner(
        compositionsDao,
        compositionSourceEditor,
        analytics,
        scheduler
    )

    private val testStateObserver = fileScanner.getStateObservable().test()

    //check scanner version set
    //check scanner version update
    //apply retry

    @Test
    fun `run successful scan`() {
        val composition: FullComposition = mock()

        whenever(compositionsDao.selectNextCompositionToScan())
            .thenReturn(Maybe.just(composition))
            .thenReturn(Maybe.empty())

        val source: CompositionSourceTags = mock()
        whenever(compositionSourceEditor.getFullTags(any()))
            .thenReturn(Maybe.just(source))

        fileScanner.scheduleFileScanner()

        verify(compositionsDao).setCompositionLastFileScanTime(any(), any())

        testStateObserver.assertValues(
            Idle,
            Running(composition),
            Idle
        )
    }

    @Test
    fun `error with getting composition from db - do not run next loop`() {
        val exception: Exception = mock()
        whenever(compositionsDao.selectNextCompositionToScan())
            .thenReturn(Maybe.error(exception))
            .thenReturn(Maybe.just(mock<FullComposition>()))

        val source: CompositionSourceTags = mock()
        whenever(compositionSourceEditor.getFullTags(any()))
            .thenReturn(Maybe.just(source))

        fileScanner.scheduleFileScanner()

        verify(compositionsDao, never()).setCompositionLastFileScanTime(any(), any())
        verify(analytics).processNonFatalError(exception)

        testStateObserver.assertValues(
            Idle
        )
    }

    @Test
    fun `error with scan - set scan time and run next loop`() {
        val composition1: FullComposition = mock()
        val composition2: FullComposition = mock()

        whenever(compositionsDao.selectNextCompositionToScan())
            .thenReturn(Maybe.just(composition1))
            .thenReturn(Maybe.just(composition2))
            .thenReturn(Maybe.empty())

        val source: CompositionSourceTags = mock()
        whenever(compositionSourceEditor.getFullTags(any()))
            .thenReturn(Maybe.just(source))

        val exception = RuntimeException()
        Mockito.doThrow(exception).doNothing().whenever(compositionsDao).applyDetailData()

        fileScanner.scheduleFileScanner()

        verify(compositionsDao, times(2)).setCompositionLastFileScanTime(any(), any())
        verify(analytics).processNonFatalError(exception)

        testStateObserver.assertValues(
            Idle,
            Running(composition1),
            Running(composition2),
            Idle
        )
    }

}