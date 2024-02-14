package com.github.anrimian.filesync.models.state

import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.filesync.models.SyncEnvCondition
import com.github.anrimian.filesync.models.repo.RemoteRepoInfo
import com.github.anrimian.filesync.models.task.Task


sealed interface SyncState {
    sealed interface IdleSyncState: SyncState
    class Inactive(val lastSyncedTime: Long): IdleSyncState {
        override fun equals(other: Any?): Boolean {
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
    object NoRepos: IdleSyncState
    object Disabled: IdleSyncState

    sealed interface ActiveTask: SyncState
    object NoTask: ActiveTask

    sealed interface MetadataSync: ActiveTask
    object Idle : MetadataSync
    sealed class RemoteRepoAction(val repoInfo: RemoteRepoInfo): MetadataSync {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RemoteRepoAction

            if (repoInfo != other.repoInfo) return false

            return true
        }

        override fun hashCode(): Int {
            return repoInfo.hashCode()
        }
    }
    class GetRemoteMetadata(repoInfo: RemoteRepoInfo): RemoteRepoAction(repoInfo)
    class GetRemoteFileTable(repoInfo: RemoteRepoInfo): RemoteRepoAction(repoInfo)
    object CollectLocalFileInfo: MetadataSync
    object CalculateChanges: MetadataSync
    class SaveRemoteFileMetadata(repoInfo: RemoteRepoInfo): RemoteRepoAction(repoInfo)
    object SaveLocalFileTable: MetadataSync
    object ScheduleFileTasks: MetadataSync

    data class FileAction<K>(
        val currentTask: Task<K>,
        val repoInfo: RemoteRepoInfo? = null
    ): ActiveTask {
        private var progressInfo = ProgressInfo()
        fun setProgress(progressInfo: ProgressInfo): FileAction<K> {
            this.progressInfo = progressInfo
            return this
        }
        fun getProgress() = progressInfo

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FileAction<*>

            if (currentTask != other.currentTask) return false
            if (repoInfo != other.repoInfo) return false

            return true
        }

        override fun hashCode(): Int {
            var result = currentTask.hashCode()
            result = 31 * result + (repoInfo?.hashCode() ?: 0)
            return result
        }
    }


    data class WaitForAllow(val syncEnvConditions: List<SyncEnvCondition>): SyncState
    object PendingSync: SyncState

    data class Error(
        val throwable: Throwable,
        val retryRemainingMillis: Long
    ): SyncState
}