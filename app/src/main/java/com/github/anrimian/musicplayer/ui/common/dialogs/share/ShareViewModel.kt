package com.github.anrimian.musicplayer.ui.common.dialogs.share

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.fsync.models.state.file.FileTaskType
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.SimpleViewModel
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

class ShareViewModel(
    private val sourceInteractor: CompositionSourceInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser
) : SimpleViewModel<ShareState>(
    initialState = ShareState(),
    savedStateHandle = savedStateHandle,
    errorParser = errorParser
) {

    private val shareData = getArgs<ShareDialogData>()

    init {
        startSharing()
    }

    fun onRetry() {
        updateState { copy(error = null) }
        prepareFiles()
    }

    private fun startSharing() {
        if (shareData.ids.isEmpty()) {
            sendEffect(ShareEffect.Close)
            return
        }

        if (shareData.hasMissingFiles) {
            updateState { copy(isInLoadingMode = true, total = shareData.ids.size) }
            prepareFiles()
        } else {
            launch(
                onError = { errorCommand -> sendEffect(ShareEffect.Error(errorCommand)) }
            ) {
                val sources = sourceInteractor.getLibraryCompositionSources(shareData.ids.asIterable(), null).await()
                sendEffect(ShareEffect.Share(ArrayList(sources)))
            }
        }
    }

    private fun prepareFiles() {
        launch(onError = { error -> updateState { copy(error = error) } }) {
            val sources = performDownloadAndGetSources()
            sendEffect(ShareEffect.Share(ArrayList(sources)))
        }
    }

    private suspend fun performDownloadAndGetSources(): List<CompositionContentSource> {
        val currentFileIdSubject = BehaviorSubject.create<Long>()

        var preparedCount = 0
        val progressJob = currentFileIdSubject.asFlow()
            .onEach {
                preparedCount++
                updateState { copy(prepared = preparedCount) }
            }
            .flatMapLatest { fileId ->
                syncInteractor.getFileSyncStateObservable(fileId).asFlow()
            }
            .mapNotNull { syncState -> syncState.value }
            .filter { syncState -> syncState.taskType == FileTaskType.DOWNLOAD }
            .subscribe { syncState ->
                updateState { copy(progress = syncState.getProgress().asInt()) }
            }
        return try {
            sourceInteractor.getLibraryCompositionSources(
                shareData.ids.asIterable(),
                currentFileIdSubject
            ).await()
        } finally {
            progressJob.cancel()
        }
    }

    fun onCancel() {
        sendEffect(ShareEffect.Close)
    }

}