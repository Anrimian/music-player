package com.github.anrimian.musicplayer.domain.interactors.library

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.toFileKeys
import com.github.anrimian.musicplayer.domain.models.utils.toKeyPairs
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Created on 24.10.2017.
 */
class LibraryFoldersInteractor(
    private val libraryRepository: LibraryRepository,
    private val editorRepository: EditorRepository,
    private val musicPlayerInteractor: LibraryPlayerInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    private val settingsRepository: SettingsRepository,
    private val uiStateRepository: UiStateRepository,
) {

    fun getFoldersInFolder(
        folderId: Long?,
        searchQuery: String?,
    ): Observable<List<FileSource>> {
        return libraryRepository.getFoldersInFolder(folderId, searchQuery)
    }

    fun getFolderObservable(folderId: Long): Observable<FolderFileSource> {
        return libraryRepository.getFolderObservable(folderId)
    }

    fun playAllMusicInFolder(folderId: Long?): Completable {
        return libraryRepository.getAllCompositionsInFolder(folderId)
            .flatMapCompletable(musicPlayerInteractor::setCompositionsQueueAndPlay)
    }

    fun getAllCompositionsInFolder(folderId: Long?): Single<List<Composition>> {
        return libraryRepository.getAllCompositionsInFolder(folderId)
    }

    fun getAllCompositionsInFileSources(fileSources: List<FileSource>): Single<List<Composition>> {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
    }

    fun play(fileSources: List<FileSource>, position: Int): Completable {
        if (position >= fileSources.size) {
            return Completable.complete()
        }
        var composition: Composition? = null
        if (position != Constants.NO_POSITION) {
            val source = fileSources[position]
            if (source is CompositionFileSource) {
                composition = source.composition
            }
        }
        return play(fileSources, composition)
    }

    fun play(folderId: Long?, compositionId: Long): Completable {
        return libraryRepository.getAllCompositionsInFolder(folderId)
            .flatMapCompletable { compositions ->
                val firstPosition = compositions.indexOfFirst {
                        composition -> composition.id == compositionId
                }
                musicPlayerInteractor.setCompositionsQueueAndPlay(compositions, firstPosition)
            }
    }

    fun deleteFiles(fileSources: List<FileSource>): Single<List<DeletedComposition>> {
        return libraryRepository.deleteFolders(fileSources).doOnSuccess(::onCompositionsDeleted)
    }

    fun deleteFolder(folder: FolderFileSource?): Single<List<DeletedComposition>> {
        return libraryRepository.deleteFolder(folder).doOnSuccess(::onCompositionsDeleted)
    }

    fun getFolderOrder(): Order = settingsRepository.folderOrder

    fun setFolderOrder(order: Order) {
        settingsRepository.folderOrder = order
    }

    fun saveCurrentFolder(folderId: Long?) {
        uiStateRepository.selectedFolderScreen = folderId
    }

    fun getCurrentFolderScreens(): Single<List<Long>> {
        val currentFolder = uiStateRepository.selectedFolderScreen
        return libraryRepository.getAllParentFolders(currentFolder)
    }

    fun getParentFolders(compositionId: Long): Single<List<Long>> {
        return libraryRepository.getAllParentFoldersForComposition(compositionId)
    }

    fun renameFolder(folderId: Long, newName: String): Completable {
        return editorRepository.changeFolderName(folderId, newName)
            .doOnSuccess { paths -> syncInteractor.onLocalFilesKeyChanged(paths.toKeyPairs()) }
            .ignoreElement()
    }

    fun moveFiles(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        toFolderId: Long?,
    ): Completable {
        return editorRepository.moveFiles(files, fromFolderId, toFolderId)
            .doOnSuccess { paths -> syncInteractor.onLocalFilesKeyChanged(paths.toKeyPairs()) }
            .ignoreElement()
    }

    fun moveFilesToNewDirectory(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        targetParentFolderId: Long?,
        directoryName: String?,
    ): Completable {
        return editorRepository.moveFilesToNewDirectory(
            files,
            fromFolderId,
            targetParentFolderId,
            directoryName
        ).doOnSuccess { paths -> syncInteractor.onLocalFilesKeyChanged(paths.toKeyPairs()) }
            .ignoreElement()
    }

    fun addFolderToIgnore(folder: IgnoredFolder): Completable {
        return libraryRepository.addFolderToIgnore(folder)
    }

    fun addFolderToIgnore(folder: FolderFileSource): Single<IgnoredFolder> {
        return libraryRepository.addFolderToIgnore(folder)
    }

    fun getIgnoredFoldersObservable(): Observable<List<IgnoredFolder>> {
        return libraryRepository.ignoredFoldersObservable
    }

    fun deleteIgnoredFolder(folder: IgnoredFolder): Completable {
        return libraryRepository.deleteIgnoredFolder(folder)
    }

    private fun play(fileSources: List<FileSource>, composition: Composition?): Completable {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
            .flatMapCompletable { compositions ->
                //in folders we can have duplicates in list in search mode,
                // so compare by references too
                @Suppress("SuspiciousEqualsCombination")
                val firstPosition = compositions.indexOfFirst { c ->
                    c == composition && c === composition
                }
                musicPlayerInteractor.setCompositionsQueueAndPlay(compositions, firstPosition)
            }
    }

    private fun onCompositionsDeleted(compositions: List<DeletedComposition>) {
        syncInteractor.onLocalFilesDeleted(compositions.toFileKeys())
    }

}