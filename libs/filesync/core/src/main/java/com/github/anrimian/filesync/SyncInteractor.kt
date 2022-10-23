package com.github.anrimian.filesync

import com.github.anrimian.filesync.models.RemoteFileSource
import com.github.anrimian.filesync.models.SyncEnvCondition
import com.github.anrimian.filesync.models.repo.RemoteRepoCredentials
import com.github.anrimian.filesync.models.repo.RemoteRepoFullInfo
import com.github.anrimian.filesync.models.repo.RemoteRepoInfo
import com.github.anrimian.filesync.models.repo.RemoteRepoType
import com.github.anrimian.filesync.models.state.SyncState
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.filesync.models.task.FileTaskInfo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface SyncInteractor<K, T, I> {

    fun onAppStarted()
    fun requestFileSync()
    fun addRemoteRepository(
        type: RemoteRepoType,
        credentials: RemoteRepoCredentials
    ): Completable
    fun removeRemoteRepository(repo: RemoteRepoInfo): Completable
    fun setRemoteRepositoryEnabled(repo: RemoteRepoInfo): Completable
    fun onLocalFileAdded()
    fun onLocalFileDeleted()
    fun notifyLocalFileChanged()
    fun isSyncEnabled(): Boolean
    fun setSyncEnabled(enabled: Boolean)
    fun onScheduledSyncCalled(): Completable
    fun getSyncConditions(): List<SyncEnvCondition>
    fun setSyncConditionEnabled(condition: SyncEnvCondition, enabled: Boolean)
    fun getAvailableRemoteRepositories(): List<RemoteRepoType>
    fun getSyncStateObservable(): Observable<SyncState>
    fun getRepositoriesObservable(): Observable<List<RemoteRepoFullInfo>>
    fun getTasksListObservable(): Observable<List<FileTaskInfo<K>>>
    fun getTasksCountObservable(): Observable<Int>
    fun getFileSyncStateObservable(fileId: I): Observable<FileSyncState>
    fun requestFileSource(fileId: I): Single<RemoteFileSource>
}