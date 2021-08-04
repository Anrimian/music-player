package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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

    //error with getting composition from db - after retry do not run next scan
    //error with scan - after retry set scan time and run next scan
    //check scanner version set
    //check scanner version update

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
}