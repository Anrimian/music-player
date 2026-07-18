package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import com.github.anrimian.musicplayer.domain.models.scanner.StorageAnalyzeResult
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface StorageScannerRepository {

    fun getStorageChangesObservable(): Observable<StorageAnalyzeResult>

    fun rescanStorage()

    fun runRescanStorage(): Completable

    fun rescanStoragePlaylists(): Completable

    /**
     * No-use, but left for test purposes
     */
    fun runStorageAndFileScanner(): Completable

    fun getFileScannerStateObservable(): Observable<FileScannerState>

}