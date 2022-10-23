package com.github.anrimian.filesync.models.state.file

import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.filesync.models.repo.RemoteRepoInfo
import io.reactivex.rxjava3.core.Observable

interface FileSyncState

object NotActive: FileSyncState

//TODO consider remove internal models
class UploadingInternal(
    val repoInfo: RemoteRepoInfo,
    val progressObservable: Observable<ProgressInfo>
): FileSyncState

class DownloadingInternal(
    val repoInfo: RemoteRepoInfo,
    val progressObservable: Observable<ProgressInfo>
): FileSyncState

object NotPresented: FileSyncState

class Uploading(val repoInfo: RemoteRepoInfo): FileSyncState {
    private lateinit var progressInfo: ProgressInfo
    fun setProgress(progressInfo: ProgressInfo): Uploading {
        this.progressInfo = progressInfo
        return this
    }
    fun getProgress() = progressInfo
}

class Downloading(val repoInfo: RemoteRepoInfo): FileSyncState {
    private lateinit var progressInfo: ProgressInfo
    fun setProgress(progressInfo: ProgressInfo): Downloading {
        this.progressInfo = progressInfo
        return this
    }
    fun getProgress() = progressInfo
}
