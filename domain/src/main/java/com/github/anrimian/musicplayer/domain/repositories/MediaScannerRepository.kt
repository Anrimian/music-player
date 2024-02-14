package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface MediaScannerRepository {

    fun runStorageObserver()

    fun rescanStorage()

    fun rescanStorageAsync()

    fun rescanStoragePlaylists(): Completable

    fun runStorageScanner(): Completable

    fun runStorageAndFileScanner(): Completable

    fun getFileScannerStateObservable(): Observable<FileScannerState>

}