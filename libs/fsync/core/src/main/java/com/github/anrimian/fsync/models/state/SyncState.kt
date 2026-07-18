package com.github.anrimian.fsync.models.state

import com.github.anrimian.fsync.models.ProgressInfo
import com.github.anrimian.fsync.models.SyncEnvCondition
import com.github.anrimian.fsync.models.state.SyncState.ActiveState
import com.github.anrimian.fsync.models.storage.RemoteStorageInfo
import com.github.anrimian.fsync.models.task.Task


sealed interface SyncState {
    sealed interface IdleSyncState: SyncState
    class Inactive(val lastSyncedTime: Long): IdleSyncState {
        override fun equals(other: Any?): Boolean {
            return javaClass == other?.javaClass
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
    data object NoStorages: IdleSyncState
    data object Disabled: IdleSyncState

    sealed interface ActiveState: SyncState
    data object NoTask: ActiveState
    data object RunningTask: ActiveState

    data class FileAction<K>(val currentTask: Task<K>): ActiveState {

        private var progressInfo = ProgressInfo()

        fun setProgress(progressInfo: ProgressInfo): FileAction<K> {
            this.progressInfo = progressInfo
            return this
        }
        fun getProgress() = progressInfo

    }


    data class WaitForAllow(val syncEnvConditions: List<SyncEnvCondition>): SyncState
    data class PendingSync(val nextSyncTime: Long): SyncState

    data class Error(
        val throwable: Throwable,
        val retryRemainingMillis: Long
    ): SyncState
}

sealed interface CatalogSyncState: ActiveState {
    data object Idle : CatalogSyncState
    sealed class RemoteStorageAction(val storageInfo: RemoteStorageInfo): CatalogSyncState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RemoteStorageAction

            return storageInfo == other.storageInfo
        }

        override fun hashCode(): Int {
            return storageInfo.hashCode()
        }
    }
    class GetRemoteCatalog(storageInfo: RemoteStorageInfo): RemoteStorageAction(storageInfo)
    class GetRemoteFileTable(storageInfo: RemoteStorageInfo): RemoteStorageAction(storageInfo)
    data object CollectLocalFileInfo: CatalogSyncState
    data object CalculateChanges: CatalogSyncState
    class SaveRemoteCatalog(storageInfo: RemoteStorageInfo): RemoteStorageAction(storageInfo)
    data object SaveLocalFileTable: CatalogSyncState
    data object ScheduleFileTasks: CatalogSyncState
}