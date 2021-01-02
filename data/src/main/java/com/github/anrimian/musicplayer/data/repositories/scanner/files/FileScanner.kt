package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler

//TODO display scanner state
//TODO apply album order
//TODO apply genres data
//TODO apply lyrics
class FileScanner(
        private val compositionsDao: CompositionsDaoWrapper,
        private val compositionSourceEditor: CompositionSourceEditor,
        private val scheduler: Scheduler
) {

    private var isRunning = false
    private var retryAfterFinish = false

    fun scheduleFileScanner() {
        if (isRunning) {
            retryAfterFinish = true
            return
        }
        Completable.fromAction(this::runFileScanner)
                .doOnSubscribe { isRunning = true }
                .doFinally { isRunning = false }
                .retryUntil { !retryAfterFinish.apply { retryAfterFinish = false } }
                .subscribeOn(scheduler)
                .subscribe()
    }

    private fun runFileScanner() {
        //get all compositions from db where lastScanTime is not present(or isScanned is false)
        //get tags from composition
        //compare
        //apply data to database
        //set last scan time
    }
}