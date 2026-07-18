package com.github.anrimian.musicplayer.domain.interactors.settings

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.domain.interactors.storage.StorageScannerInteractor
import com.github.anrimian.musicplayer.domain.models.search.CompositionLookup
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

class LibrarySettingsInteractor(
    private val settingsRepository: SettingsRepository,
    private val libraryRepository: LibraryRepository,
    private val storageScannerInteractor: StorageScannerInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
) {

    fun getAppConfirmDeleteDialogEnabledFlow() = settingsRepository.appConfirmDeleteDialogEnabledObservable.asFlow()

    fun setAppConfirmDeleteDialogEnabled(enabled: Boolean) {
        settingsRepository.isAppConfirmDeleteDialogEnabled = enabled
    }

    fun isAppConfirmDeleteDialogEnabled() = settingsRepository.isAppConfirmDeleteDialogEnabled

    fun getAudioFileMinDurationMillisFlow() = settingsRepository.audioFileMinDurationMillisObservable.asFlow()

    suspend fun checkMinDurationModify(newMillis: Long): Int {
        if (!syncInteractor.isSyncAvailable()) {
            return 0
        }
        val oldMillis = settingsRepository.audioFileMinDurationMillis
        if (newMillis > oldMillis) {
            val lookup = CompositionLookup(minDuration = oldMillis, maxDuration = newMillis)
            val filesToRemove = libraryRepository.getCompositionKeys(lookup).await()
            return filesToRemove.size
        }
        return 0
    }

    suspend fun setAudioFileMinDurationMillis(newMillis: Long) {
        val oldMillis = settingsRepository.audioFileMinDurationMillis

        if (newMillis == oldMillis) {
            return
        }

        if (newMillis > oldMillis) {
            // Case 1: Filter is stricter (e.g., 30s -> 60s). Files will be newly filtered.
            val lookup = CompositionLookup(minDuration = oldMillis, maxDuration = newMillis)
            val compositionsToDelete = libraryRepository.getCompositionKeys(lookup).await()
            settingsRepository.audioFileMinDurationMillis = newMillis
            storageScannerInteractor.runRescanStorage().await()
            syncInteractor.onLocalFilesDeleted(compositionsToDelete).await()
        } else {
            // Case 2: Filter is less strict (e.g., 60s -> 30s). Files will be "restored".
            val lookup = CompositionLookup(minDuration = newMillis, maxDuration = oldMillis)
            settingsRepository.audioFileMinDurationMillis = newMillis
            storageScannerInteractor.runRescanStorage().await()
            val restoredCompositions = libraryRepository.getCompositionKeys(lookup).await()
            syncInteractor.onLocalFilesRestored(restoredCompositions).await()
        }
    }

    fun getAudioFileMinDurationMillis() = settingsRepository.audioFileMinDurationMillis

    fun setShowAllAudioFilesEnabled(enabled: Boolean) {
        if (settingsRepository.setShowAllAudioFilesEnabled(enabled)) {
            storageScannerInteractor.rescanStorage()
        }
    }

    fun getAllowedFileExtensionsFlow(): Flow<Set<String>> {
        return settingsRepository.allowedFileExtensionsObservable.asFlow()
    }

    fun getAllowedFileExtensions(): Set<String> {
        return settingsRepository.allowedFileExtensions
    }

    suspend fun checkAllowedFileExtensionsModify(newExtensions: Set<String>): Int {
        if (!syncInteractor.isSyncAvailable()) {
            return 0
        }

        val oldExtensions = settingsRepository.allowedFileExtensions
        val removedExtensions = oldExtensions - newExtensions

        if (removedExtensions.isEmpty()) {
            return 0
        }
        val lookup = CompositionLookup(fileExtensions = removedExtensions)
        val filesToRemove = libraryRepository.getCompositionKeys(lookup).await()
        return filesToRemove.size
    }

    suspend fun setAllowedFileExtensions(newExtensions: Set<String>) {
        val oldExtensions = settingsRepository.allowedFileExtensions
        if (newExtensions == oldExtensions) {
            return
        }

        val addedExtensions = newExtensions - oldExtensions
        val removedExtensions = oldExtensions - newExtensions

        val compositionsToDelete = if (removedExtensions.isNotEmpty()) {
            val lookup = CompositionLookup(fileExtensions = removedExtensions)
            libraryRepository.getCompositionKeys(lookup).await()
        } else {
            emptyList()
        }

        settingsRepository.allowedFileExtensions = newExtensions
        storageScannerInteractor.runRescanStorage().await()

        val restoredCompositions = if (addedExtensions.isNotEmpty()) {
            val lookup = CompositionLookup(fileExtensions = addedExtensions)
            libraryRepository.getCompositionKeys(lookup).await()
        } else {
            emptyList()
        }

        if (compositionsToDelete.isEmpty() && restoredCompositions.isEmpty()) {
            syncInteractor.cancelCurrentTask()
            syncInteractor.requestFileSync()
            return
        }

        if (compositionsToDelete.isNotEmpty()) {
            syncInteractor.onLocalFilesDeleted(compositionsToDelete).await()
        }
        if (restoredCompositions.isNotEmpty()) {
            syncInteractor.onLocalFilesRestored(restoredCompositions).await()
        }
    }

    fun getShowAllAudioFilesEnabledObservable(): Observable<Boolean> =
        settingsRepository.showAllAudioFilesEnabledObservable

    fun setPlaylistInsertStartEnabled(enabled: Boolean) {
        settingsRepository.isPlaylistInsertStartEnabled = enabled
    }

    fun isPlaylistInsertStartEnabled(): Boolean = settingsRepository.isPlaylistInsertStartEnabled

    fun setPlaylistDuplicateCheckEnabled(enabled: Boolean) {
        settingsRepository.isPlaylistDuplicateCheckEnabled = enabled
    }

    fun getPlaylistDuplicateCheckEnabledFlow(): Flow<Boolean> {
        return settingsRepository.playlistDuplicateCheckObservable.asFlow()
    }

}