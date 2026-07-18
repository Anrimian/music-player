package com.github.anrimian.musicplayer.data.repositories.scanner

import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDiskIOException
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.repositories.scanner.files.FileScanner
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.StoragePlaylistsAnalyzer
import com.github.anrimian.musicplayer.data.storage.exceptions.ContentResolverQueryException
import com.github.anrimian.musicplayer.data.storage.providers.music.AudioFileKey
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageAudioFile
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylist
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlaylistsProvider
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import com.github.anrimian.musicplayer.domain.models.scanner.StorageAnalyzeResult
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.StateRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageScannerRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.Collections
import java.util.concurrent.TimeUnit

class StorageScannerRepositoryImpl(
    private val audioCatalogProvider: SystemAudioCatalogProvider,
    private val playListsProvider: StoragePlaylistsProvider,
    private val compositionsDao: CompositionsDaoWrapper,
    private val stateRepository: StateRepository,
    private val settingsRepository: SettingsRepository,
    private val compositionAnalyzer: StorageAudioFileAnalyzer,
    private val playlistAnalyzer: StoragePlaylistsAnalyzer,
    private val fileScanner: FileScanner,
    private val loggerRepository: LoggerRepository,
    private val analytics: Analytics,
    private val scheduler: Scheduler
) : StorageScannerRepository {

    private val rescanStorageSubject = BehaviorSubject.createDefault(Constants.TRIGGER)

    private val internalStorageChangesObservable by lazy { createStorageChangesObservable().share() }

    override fun getStorageChangesObservable(): Observable<StorageAnalyzeResult> {
        return internalStorageChangesObservable
    }

    override fun rescanStorage() {
        rescanStorageSubject.onNext(Constants.TRIGGER)
    }

    override fun runRescanStorage(): Completable {
        return Completable.defer {
            internalStorageChangesObservable
                .doOnSubscribe { rescanStorage() }
                .firstOrError()
                .ignoreElement()
        }
    }

    override fun runStorageAndFileScanner(): Completable {
        return Completable.fromAction(compositionsDao::cleanLastFileScanTime)
            .subscribeOn(scheduler)
            .andThen(runRescanStorage())
    }

    override fun rescanStoragePlaylists(): Completable {
        return Completable.fromAction(::readStoragePlaylists)
            .subscribeOn(scheduler)
    }

    override fun getFileScannerStateObservable(): Observable<FileScannerState> {
        return fileScanner.getStateObservable()
    }

    private fun createStorageChangesObservable(): Observable<StorageAnalyzeResult> {
        return Observable.defer {
            val settingsChangeObservable = Observable.combineLatest(
                settingsRepository.getAudioFileMinDurationMillisObservable(),
                settingsRepository.getShowAllAudioFilesEnabledObservable(),
                settingsRepository.getAllowedFileExtensionsObservable()
            ) { _, _, _ -> Constants.TRIGGER }

            val rescanObservable = Observable.merge(
                rescanStorageSubject,
                settingsChangeObservable,
                audioCatalogProvider.getChangeObservable()
                // add other storages later
            )

            var isPlaylistScanPerformed = false

            rescanObservable
                .observeOn(scheduler) // required bc rescanObservable can be fired from the main thread
                .throttleLatest(500, TimeUnit.MILLISECONDS, true)
                .map {
                    fetchAndAnalyzeFiles(!isPlaylistScanPerformed).also {
                        isPlaylistScanPerformed = true
                    }
                }
                .retry(RETRY_COUNT, ::isStandardError)
                .onErrorResumeNext(::handleScanError)
                .subscribeOn(scheduler)
        }
    }

    private fun fetchAndAnalyzeFiles(scanPlaylists: Boolean): StorageAnalyzeResult {
        val minDuration = settingsRepository.getAudioFileMinDurationMillis()
        val showAll = settingsRepository.isShowAllAudioFilesEnabled()
        val allowedExtensions = settingsRepository.getAllowedFileExtensions()
        val allFiles = HashMap<AudioFileKey, StorageAudioFile>()
        allFiles.putAll(
            audioCatalogProvider.getAudioFiles(minDuration, showAll, allowedExtensions) ?: emptyMap()
        )
        // add other storages later
        return onStorageFilesReceived(allFiles, scanPlaylists)
    }

    private fun handleScanError(throwable: Throwable): Observable<StorageAnalyzeResult> {
        return if (isStandardError(throwable)) {
            if (isStandardUnwantedError(throwable)) {
                analytics.processNonFatalError(throwable)
            }
            Observable.just(StorageAnalyzeResult(emptyList(), emptyList(), emptyList(), 0L, false))
        } else {
            loggerRepository.setWasCriticalFatalError(true)
            Observable.error(throwable)
        }
    }

    private fun onStorageFilesReceived(
        actualCompositions: HashMap<AudioFileKey, StorageAudioFile>,
        scanPlaylists: Boolean
    ): StorageAnalyzeResult {
        val wasChanges = compositionAnalyzer.applyCompositionsData(actualCompositions)

        if (scanPlaylists) {
            val playlistsToAnalyze: Map<String, StoragePlaylist>?
            if (stateRepository.isStoragePlaylistsImported()) {
                playlistsToAnalyze = Collections.emptyMap()
            } else {
                val loadedPlaylists = playListsProvider.getPlayLists()
                if (loadedPlaylists != null) {
                    stateRepository.setStoragePlaylistsImported(true)
                    playlistsToAnalyze = loadedPlaylists
                } else {
                    // playListsProvider does not respond
                    // Do not call applyPlayListsData for this case.
                    playlistsToAnalyze = null
                }
            }
            if (playlistsToAnalyze != null) {
                // it should always be called to trigger file cache analyze on app startup
                playlistAnalyzer.applyPlayListsData(playlistsToAnalyze)
            }
        }

        fileScanner.scheduleFileScanner()

        return wasChanges
    }

    private fun readStoragePlaylists() {
        val playlists = playListsProvider.getPlayLists() ?: return
        playlistAnalyzer.applyPlayListsData(playlists)
        stateRepository.setStoragePlaylistsImported(true)
    }

    private fun isStandardError(throwable: Throwable): Boolean {
        return throwable is SQLiteDiskIOException
                || throwable is SQLiteCantOpenDatabaseException
                || isStandardUnwantedError(throwable)
    }

    private fun isStandardUnwantedError(throwable: Throwable): Boolean {
        return throwable is ContentResolverQueryException
    }

    companion object {
        private const val RETRY_COUNT = 5L
    }

}