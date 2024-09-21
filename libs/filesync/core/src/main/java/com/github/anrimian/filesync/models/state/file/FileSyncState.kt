package com.github.anrimian.filesync.models.state.file

import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.filesync.models.repo.RemoteRepoInfo

sealed interface FileSyncState {

    data object NotActive : FileSyncState

    abstract class Active(val repoInfo: RemoteRepoInfo): FileSyncState

    class Uploading(repoInfo: RemoteRepoInfo) : Active(repoInfo) {
        private var progressInfo = ProgressInfo()
        fun setProgress(progressInfo: ProgressInfo): Uploading {
            this.progressInfo = progressInfo
            return this
        }

        fun getProgress() = progressInfo

        override fun toString(): String {
            return "Uploading(repoInfo=$repoInfo, progressInfo=$progressInfo)"
        }
    }

    class Downloading(repoInfo: RemoteRepoInfo) : Active(repoInfo) {
        private var progressInfo = ProgressInfo()
        fun setProgress(progressInfo: ProgressInfo): Downloading {
            this.progressInfo = progressInfo
            return this
        }

        fun getProgress() = progressInfo

        override fun toString(): String {
            return "Downloading(repoInfo=$repoInfo, progressInfo=$progressInfo)"
        }
    }
}
