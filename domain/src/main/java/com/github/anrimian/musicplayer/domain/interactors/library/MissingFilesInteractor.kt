package com.github.anrimian.musicplayer.domain.interactors.library

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.AudioFileInfo
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.toFileKeys
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.utils.coroutines.launchCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

class MissingFilesInteractor(
    private val libraryRepository: LibraryRepository,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    private val appIoScope: CoroutineScope,
    private val analytics: Analytics
) {

    private val restoreInProgressFlow = MutableStateFlow(false)
    private val restoreFilesErrorsFlow = MutableStateFlow<Map<AudioFileInfo, Throwable>>(emptyMap())

    fun getMissingFilesCountFlow(): Flow<Int> {
        return libraryRepository.getMissingCompositionsCountObservable().asFlow()
    }

    fun getMissingAudioFilesFlow(): Flow<List<AudioFileInfo>> {
        return libraryRepository.getMissingAudioFilesObservable().asFlow()
    }

    fun getRestoreInProgressFlow(): Flow<Boolean> = restoreInProgressFlow

    fun getRestoreFilesErrorsFlow(): Flow<Map<AudioFileInfo, Throwable>> = restoreFilesErrorsFlow

    suspend fun deleteMissingCompositions(): List<DeletedComposition> {
        val c = libraryRepository.deleteMissingCompositions().await()
        syncInteractor.onLocalFilesDeleted(c.toFileKeys()).await()
        return c
    }

    fun launchRestoreMissingCompositions(files: List<AudioFileInfo>) {
        appIoScope.launchCatching(
            onError = analytics::processNonFatalError,
            onProgress = { progress -> restoreInProgressFlow.value = progress }
        ) {
            restoreFilesErrorsFlow.value = emptyMap()
            val errors = mutableMapOf<AudioFileInfo, Throwable>()
            files.forEach { file ->
                runCatching {
                    syncInteractor.requestFileSource(file.id).await()
                }.onFailure { throwable ->
                    errors[file] = throwable
                    restoreFilesErrorsFlow.value = HashMap(errors)
                }
            }
        }
    }

}