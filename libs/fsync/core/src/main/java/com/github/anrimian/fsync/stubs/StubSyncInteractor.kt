package com.github.anrimian.fsync.stubs

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.fsync.models.Optional
import com.github.anrimian.fsync.models.RemoteFileSource
import com.github.anrimian.fsync.models.SyncEnvCondition
import com.github.anrimian.fsync.models.catalog.ChangedKey
import com.github.anrimian.fsync.models.run.ScheduledTaskRunResult
import com.github.anrimian.fsync.models.state.SyncState
import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.fsync.models.storage.RemoteStorageFullInfo
import com.github.anrimian.fsync.models.storage.RemoteStorageInfo
import com.github.anrimian.fsync.models.storage.StorageSetupTemplate
import com.github.anrimian.fsync.models.task.FileTaskInfo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.FileNotFoundException

class StubSyncInteractor<K, T, I> : SyncInteractor<K, T, I> {
    override fun start() {}
    override fun onAppVisibilityChanged(isVisible: Boolean) {}
    override fun isAppVisible() = false
    override fun requestFileSync(ignoreConditions: Boolean) {}
    override fun runScheduledSync(): Single<ScheduledTaskRunResult> = Single.never()
    override fun cancelCurrentTask() {}
    override fun postponeTasksExecution() {}
    override fun isLongTaskDetected() = false

    override fun runFileTasks() {}

    override fun addRemoteStorage(template: StorageSetupTemplate): Completable {
        return Completable.never()
    }

    override fun removeRemoteStorage(storage: RemoteStorageInfo): Completable {
        return Completable.never()
    }

    override fun enableRemoteStorage(storage: RemoteStorageInfo): Completable {
        return Completable.never()
    }

    override fun disableRemoteStorage(storage: RemoteStorageInfo): Completable {
        return Completable.never()
    }

    override fun onLocalFileAdded() {}
    override fun onLocalFileDeleted(key: K, time: Long) = Completable.complete()
    override fun onLocalFilesDeleted(keys: List<K>, time: Long) = Completable.complete()
    override fun onLocalFileRestored(key: K, time: Long) = Completable.complete()
    override fun onLocalFilesRestored(keys: List<K>, time: Long) = Completable.complete()
    override fun onLocalFileKeyChanged(key: ChangedKey<K>, time: Long) = Completable.complete()
    override fun onLocalFilesKeyChanged(keys: List<ChangedKey<K>>, time: Long) = Completable.complete()
    override fun onLocalKeyRecordsChanged(
        deletedKeys: List<K>,
        restoredKeys: List<K>,
        changedKeys: List<ChangedKey<K>>,
        time: Long,
    ) = Completable.complete()
    override fun onLocalFilesChanged(
        disappeared: List<K>,
        reappeared: List<K>,
        changedKeys: List<ChangedKey<K>>,
        time: Long,
    ) = Completable.complete()
    override fun notifyLocalFileChanged() {}
    override fun isSyncAvailable() = false
    override fun isSyncEnabled() = false
    override fun setSyncEnabled(enabled: Boolean) {}
    override fun isSyncEnabledAndSet() = false
    override fun resetStoragesState() = Completable.complete()
    override fun resetStoragesStateAndLogout() = Completable.complete()
    override fun getSyncConditions(): List<SyncEnvCondition> {
        return emptyList()
    }

    override fun setSyncConditionEnabled(condition: SyncEnvCondition, enabled: Boolean) {}

    override fun getAvailableRemoteStorages(): List<Int> {
        return emptyList()
    }

    override fun getSyncStateObservable(): Observable<SyncState> {
        return Observable.never()
    }

    override fun getStoragesObservable(): Observable<List<RemoteStorageFullInfo>> {
        return Observable.never()
    }

    override fun getTasksListObservable(): Observable<List<FileTaskInfo<K>>> {
        return Observable.never()
    }

    override fun getUnfinishedTasksCountObservable(): Observable<Int> {
        return Observable.never()
    }

    override fun getFileSyncStateObservable(fileId: I): Observable<Optional<FileSyncState>> {
        return Observable.never()
    }

    override fun getFilesSyncStateObservable(): Observable<Map<I, FileSyncState>> {
        return Observable.never()
    }

    override fun requestFileSource(fileId: I): Single<RemoteFileSource> {
        return Single.error(FileNotFoundException())
    }
}