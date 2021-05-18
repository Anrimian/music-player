package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.FileNotFoundException
import java.util.*

//TODO apply album order
//TODO apply genres data
//TODO apply lyrics
class FileScanner(
    private val compositionsDao: CompositionsDaoWrapper,
    private val compositionSourceEditor: CompositionSourceEditor,
    private val analytics: Analytics,
    private val scheduler: Scheduler
) {

    private val stateSubject = BehaviorSubject.createDefault<FileScannerState>(Idle)

    @Synchronized
    fun scheduleFileScanner() {
        if (stateSubject.value != Idle) {
            return
        }
        runFileScanner()
    }

    fun getStateObservable(): Observable<FileScannerState> = stateSubject

    @Synchronized
    private fun runFileScanner() {
        compositionsDao.selectNextCompositionToScan()
            .doOnSuccess { composition -> stateSubject.onNext(Running(composition))}
            .flatMap(this::scanCompositionFile)
            .doOnSuccess { runFileScanner() }
            .doOnError(this::processError)
            .onErrorComplete()
            .doOnComplete { stateSubject.onNext(Idle) }
            .subscribeOn(scheduler)
            .subscribe()
    }

    private fun scanCompositionFile(composition: FullComposition): Maybe<*> {
        return compositionSourceEditor.getFullTags(composition)
            .doOnSuccess { tags -> processCompositionScan(composition, tags) }
    }

    private fun processCompositionScan(fullComposition: FullComposition,
                                       fileTags: CompositionSourceTags) {
        //compare
        //apply data to database

        //and set last modify time to prevent overwriting by scanner?
        //no, just add condition to media analyzer
        compositionsDao.setCompositionLastFileScanTime(fullComposition.id, Date())
    }

    private fun processError(throwable: Throwable) {
        if (throwable is FileNotFoundException) {
            return
        }
        analytics.processNonFatalError(throwable)
    }

}