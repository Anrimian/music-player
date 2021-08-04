package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.Constants.TRIGGER
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
//TODO on error exclude composition from scan after several attempts
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

    fun getStateObservable(): Observable<FileScannerState> = stateSubject.distinctUntilChanged()

    private fun runFileScanner() {
        compositionsDao.selectNextCompositionToScan()
            .doOnError(this::processError)
            .onErrorComplete()
            .doOnSuccess { composition -> stateSubject.onNext(Running(composition))}
            .flatMap(this::scanCompositionFile)
            .doOnSuccess { runFileScanner() }
            .doOnComplete { stateSubject.onNext(Idle) }
            .subscribeOn(scheduler)
            .subscribe()
    }

    private fun scanCompositionFile(composition: FullComposition): Maybe<*> {
        return compositionSourceEditor.getFullTags(composition)
            .doOnSuccess { tags -> processCompositionScan(composition, tags) }
            .doOnError(this::processError)
            .map { TRIGGER }
            .onErrorReturnItem(TRIGGER)
            //and set last modify time to prevent overwriting by scanner?
            //no, just add condition to media analyzer(hasActualChanges - also compare last file scan time and last modify time)
            .doOnSuccess { compositionsDao.setCompositionLastFileScanTime(composition.id, Date()) }
    }

    private fun processCompositionScan(fullComposition: FullComposition,
                                       fileTags: CompositionSourceTags) {
        //compare
        //apply data to database(in one transaction)
        compositionsDao.applyDetailData()
    }

    //on error - rerun?
    //+try several times and them skip?
    //onErrorResumeNext?
    //variant:
    //--retry three times
    //on error exclude from scan(how, just set file scan time?..no, it should rescan again, right),
    // log if need, and consume event
    private fun processError(throwable: Throwable) {
        if (throwable is FileNotFoundException) {
            return
        }
        analytics.processNonFatalError(throwable)
    }

}