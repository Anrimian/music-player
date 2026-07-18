package com.github.anrimian.fsync

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

interface SyncInteractor<K, T, I> {
    fun start()
    fun onAppVisibilityChanged(isVisible: Boolean)
    fun isAppVisible(): Boolean
    fun requestFileSync(ignoreConditions: Boolean = false)
    fun runScheduledSync(): Single<ScheduledTaskRunResult>
    fun cancelCurrentTask()
    fun postponeTasksExecution()
    fun isLongTaskDetected(): Boolean
    fun runFileTasks()
    fun addRemoteStorage(template: StorageSetupTemplate): Completable
    fun removeRemoteStorage(storage: RemoteStorageInfo): Completable
    fun enableRemoteStorage(storage: RemoteStorageInfo): Completable
    fun disableRemoteStorage(storage: RemoteStorageInfo): Completable
    fun onLocalFileAdded()
    fun onLocalFileDeleted(key: K, time: Long = System.currentTimeMillis()): Completable
    fun onLocalFilesDeleted(keys: List<K>, time: Long = System.currentTimeMillis()): Completable
    fun onLocalFileRestored(key: K, time: Long = System.currentTimeMillis()): Completable
    fun onLocalFilesRestored(keys: List<K>, time: Long = System.currentTimeMillis()): Completable
    fun onLocalFileKeyChanged(key: ChangedKey<K>, time: Long): Completable
    fun onLocalFilesKeyChanged(keys: List<ChangedKey<K>>, time: Long): Completable
    fun onLocalKeyRecordsChanged(
        deletedKeys: List<K> = emptyList(),
        restoredKeys: List<K>,
        changedKeys: List<ChangedKey<K>>,
        time: Long
    ): Completable
    fun onLocalFilesChanged(
        disappeared: List<K>,
        reappeared: List<K>,
        changedKeys: List<ChangedKey<K>>,
        time: Long
    ): Completable
    fun notifyLocalFileChanged()
    fun isSyncAvailable(): Boolean
    fun isSyncEnabled(): Boolean
    fun setSyncEnabled(enabled: Boolean)
    fun isSyncEnabledAndSet(): Boolean
    fun resetStoragesState(): Completable
    fun resetStoragesStateAndLogout(): Completable
    fun getSyncConditions(): List<SyncEnvCondition>
    fun setSyncConditionEnabled(condition: SyncEnvCondition, enabled: Boolean)
    fun getAvailableRemoteStorages(): List<Int>
    fun getSyncStateObservable(): Observable<SyncState>
    fun getStoragesObservable(): Observable<List<RemoteStorageFullInfo>>
    fun getTasksListObservable(): Observable<List<FileTaskInfo<K>>>
    fun getUnfinishedTasksCountObservable(): Observable<Int>
    fun getFileSyncStateObservable(fileId: I): Observable<Optional<FileSyncState>>
    fun getFilesSyncStateObservable(): Observable<Map<I, FileSyncState>>
    fun requestFileSource(fileId: I): Single<RemoteFileSource>
}