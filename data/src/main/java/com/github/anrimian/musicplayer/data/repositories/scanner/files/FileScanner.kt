package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.Constants.TRIGGER
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags
import com.github.anrimian.musicplayer.domain.repositories.StateRepository
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.FileNotFoundException
import java.util.*

//TODO apply album order
//TODO apply genres data
//TODO apply lyrics

//check: media analyzer scan date condition
private const val RETRY_TIMES = 2L

class FileScanner(
    private val compositionsDao: CompositionsDaoWrapper,
    private val compositionSourceEditor: CompositionSourceEditor,
    private val stateRepository: StateRepository,
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
        val lastCompleteScanTime = if (
            stateRepository.lastFileScannerVersion == stateRepository.currentFileScannerVersion
        ) 0L else stateRepository.lastCompleteScanTime
        compositionsDao.selectNextCompositionToScan(lastCompleteScanTime)
            .doOnComplete(this::onScanCompleted)
            .retry(RETRY_TIMES)
            .doOnError(this::processError)
            .onErrorComplete()
            .doOnSuccess { composition -> stateSubject.onNext(Running(composition))}
            .flatMap(this::scanCompositionFile)
            .doOnSuccess { runFileScanner() }
            .doOnComplete { stateSubject.onNext(Idle) }
            .subscribeOn(scheduler)
            .subscribe()
    }

    private fun onScanCompleted() {
        stateRepository.lastFileScannerVersion = stateRepository.currentFileScannerVersion
        stateRepository.lastCompleteScanTime = System.currentTimeMillis()
    }

    private fun scanCompositionFile(composition: FullComposition): Maybe<*> {
        return compositionSourceEditor.getFullTags(composition)
            .doOnSuccess { tags -> processCompositionScan(composition, tags) }
            .retry(RETRY_TIMES)
            .doOnError(this::processError)
            .map { TRIGGER }
            .onErrorReturnItem(TRIGGER)
            .doOnSuccess { compositionsDao.setCompositionLastFileScanTime(composition, Date()) }
    }

    private fun processCompositionScan(fullComposition: FullComposition,
                                       fileTags: CompositionSourceTags) {
        //compare
        //apply data to database(in one transaction)
        compositionsDao.applyDetailData()
    }

    private fun processError(throwable: Throwable) {
        if (throwable is FileNotFoundException) {
            return
        }
        analytics.processNonFatalError(throwable)
    }

}