package com.github.anrimian.musicplayer.data.repositories.scanner.files

//import android.util.Log
import android.util.Log
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.Constants.TRIGGER
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import com.github.anrimian.musicplayer.domain.models.scanner.Idle
import com.github.anrimian.musicplayer.domain.models.scanner.Running
import com.github.anrimian.musicplayer.domain.repositories.StateRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.TimeUnit

//apply album order
//apply genres data
//apply lyrics

//handle tag analyzer errors?
//clean logs

//check: media analyzer scan date condition: runs normally
//check: interaction with tag editor: on second attempt always freeze - fixed
//check: media scanner version update
private const val RETRY_TIMES = 2L
private const val READ_FILE_TIMEOUT_SECONDS = 2L

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
            .flatMapSingle(this::scanCompositionFile)
            .doOnSuccess {
                Log.d("KEK", "run next")
                runFileScanner()
            }
            .doOnComplete {
                Log.d("KEK", "completed")
                stateSubject.onNext(Idle)
            }
            .subscribeOn(scheduler)
            .subscribe()
    }

    private fun onScanCompleted() {
        stateRepository.lastFileScannerVersion = stateRepository.currentFileScannerVersion
        stateRepository.lastCompleteScanTime = System.currentTimeMillis()
    }

    private fun scanCompositionFile(composition: FullComposition): Single<*> {
        return Single.just(composition)
            .doOnSuccess { Log.d("KEK", "scanCompositionFile: " + composition.fileName) }
            .flatMap(this::getFullTags)
            .doOnSuccess { tags ->
                Log.d("KEK", "updateCompositionBySourceTags: " + composition.fileName)
                compositionsDao.updateCompositionBySourceTags(composition, tags) }
            .timeout(READ_FILE_TIMEOUT_SECONDS, TimeUnit.SECONDS, scheduler)
            .doOnError { Log.d("KEK", "scanCompositionFile error: ${it.message}") }
            .retry(RETRY_TIMES)
            .doOnError(this::processError)
            .map { TRIGGER }
            .onErrorReturnItem(TRIGGER)
            .doOnSuccess { compositionsDao.setCompositionLastFileScanTime(composition, Date()) }
    }

    private fun getFullTags(composition: FullComposition): Single<CompositionSourceTags> {
        //scheduler is required to prevent timeout and handle it correctly
        return compositionSourceEditor.getFullTags(composition).subscribeOn(scheduler)
    }

    private fun processError(throwable: Throwable) {
        Log.d("KEK", "processError: " + throwable.message)
        if (throwable is FileNotFoundException) {
            return
        }
        analytics.processNonFatalError(throwable)
    }

}