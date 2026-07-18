package com.github.anrimian.musicplayer.domain.interactors.storage

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.toChangedKeys
import com.github.anrimian.musicplayer.domain.repositories.StorageScannerRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class StorageScannerInteractor(
    private val storageScannerRepository: StorageScannerRepository,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
) {

    private val storageObserverDisposable = CompositeDisposable()

    fun runStorageObserver() {
        if (storageObserverDisposable.size() != 0) {
            return
        }
        storageObserverDisposable.add(storageScannerRepository.getStorageChangesObservable()
            .flatMapCompletable { result ->
                if (result.disappearedFiles.isNotEmpty()
                    || result.reappearedFiles.isNotEmpty()
                    || result.movedFiles.isNotEmpty()
                ) {
                    return@flatMapCompletable syncInteractor.onLocalFilesChanged(
                        result.disappearedFiles,
                        result.reappearedFiles,
                        result.movedFiles.toChangedKeys(),
                        result.modifyTime
                    )
                }
                if (result.hasChanges) {
                    return@flatMapCompletable Completable.fromAction { syncInteractor.requestFileSync() }
                }
                Completable.complete()
            }
            .subscribe()
        )
    }

    fun rescanStorage() {
        storageScannerRepository.rescanStorage()
    }

    fun runRescanStorage(): Completable {
        return storageScannerRepository.runRescanStorage()
    }

    fun rescanStoragePlaylists(): Completable {
        return storageScannerRepository.rescanStoragePlaylists()
    }

    fun getFileScannerStateObservable() = storageScannerRepository.getFileScannerStateObservable()

}