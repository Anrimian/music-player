package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.exceptions.TagReaderException
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.Constants.TRIGGER
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.tags.AudioFileInfo
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import com.github.anrimian.musicplayer.domain.models.scanner.Idle
import com.github.anrimian.musicplayer.domain.models.scanner.Running
import com.github.anrimian.musicplayer.domain.repositories.StateRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.TimeUnit

class FileScanner(
    private val compositionsDao: CompositionsDaoWrapper,
    private val compositionSourceEditor: CompositionSourceEditor,
    private val stateRepository: StateRepository,
    private val storageSourceRepository: StorageSourceRepository,
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

    fun runScanCompositionFile(composition: FullComposition) {
        scanCompositionFile(composition)
            .subscribeOn(scheduler)
            .subscribe()
    }

    fun getStateObservable(): Observable<FileScannerState> = stateSubject.distinctUntilChanged()

    private fun runFileScanner() {
        val lastCompleteScanTime = if (
            stateRepository.lastFileScannerVersion == stateRepository.currentFileScannerVersion
        ) 0L else stateRepository.lastCompleteScanTime
        compositionsDao.selectNextCompositionsToScan(lastCompleteScanTime, FILES_TO_SCAN_COUNT)
            .retry(READ_RETRY_TIMES)
            .flatMap(this::scanCompositionFiles)
            .flatMapMaybe { compositions ->
                if (compositions.size < FILES_TO_SCAN_COUNT) {
                    onScanCompleted()
                    return@flatMapMaybe Maybe.empty()
                }
                return@flatMapMaybe Maybe.just(compositions)
            }
            .doOnError(this::processError)
            .onErrorComplete()//represent db read error, in this case stop scan until next launch
            .doOnSuccess { runFileScanner() }
            .doOnComplete { stateSubject.onNext(Idle) }
            .subscribeOn(scheduler)
            .subscribe()
    }

    private fun scanCompositionFiles(compositions: List<FullComposition>): Single<List<FullComposition>> {
        return Observable.fromIterable(compositions)
            .flatMapMaybe { composition ->
                getCompositionSource(composition)
                    .doOnSuccess { stateSubject.onNext(Running(composition)) }
                    .flatMap(this::getAudioFileInfo)
                    .map { info -> Pair(composition, info) }
            }
            .collect(::ArrayList, ArrayList<Pair<FullComposition, AudioFileInfo>>::add)
            .doOnSuccess { scannedCompositions ->
                compositionsDao.updateCompositionsByFileInfo(scannedCompositions, compositions)
            }
            .map { compositions }
            .doOnError(this::processError)
            .onErrorReturnItem(compositions)//represent db write error, in this case run again
    }

    private fun onScanCompleted() {
        stateRepository.lastFileScannerVersion = stateRepository.currentFileScannerVersion
        stateRepository.lastCompleteScanTime = System.currentTimeMillis()
    }

    private fun scanCompositionFile(composition: FullComposition): Single<*> {
        return Single.just(composition)
            .flatMapMaybe(this::getCompositionSource)
            .flatMap(this::getAudioFileInfo)
            .doOnSuccess { info -> compositionsDao.updateCompositionByFileInfo(composition, info) }
            .doOnError(this::processError)
            .map { TRIGGER }
            .defaultIfEmpty(TRIGGER)
            .onErrorReturnItem(TRIGGER)
            .doOnSuccess { compositionsDao.setCompositionLastFileScanTime(composition, Date()) }
    }

    private fun getAudioFileInfo(source: CompositionContentSource): Maybe<AudioFileInfo> {
        return compositionSourceEditor.getAudioFileInfo(source)
            .timeout(READ_FILE_TIMEOUT_SECONDS, TimeUnit.SECONDS, scheduler)
            .retry(READ_RETRY_TIMES)
            .doOnError(this::processError)
            .onErrorComplete()//if we can't read the file - ignore and set last scan time anyway
    }

    private fun getCompositionSource(composition: FullComposition): Maybe<CompositionContentSource> {
        return storageSourceRepository.getStorageSource(composition.id)
            .observeOn(scheduler)
    }

    private fun processError(throwable: Throwable) {
        if (throwable is FileNotFoundException || throwable is TagReaderException) {
            return
        }
        analytics.processNonFatalError(throwable)
    }

    private companion object {
        const val FILES_TO_SCAN_COUNT = 150
        const val READ_RETRY_TIMES = 2L
        const val READ_FILE_TIMEOUT_SECONDS = 6L
    }

}