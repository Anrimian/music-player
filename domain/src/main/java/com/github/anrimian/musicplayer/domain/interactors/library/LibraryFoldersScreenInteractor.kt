package com.github.anrimian.musicplayer.domain.interactors.library

import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderInfo
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * Created on 24.10.2017.
 */
class LibraryFoldersScreenInteractor(
    private val foldersInteractor: LibraryFoldersInteractor,
    private val libraryRepository: LibraryRepository,
    private val uiStateRepository: UiStateRepository
) {

    private val moveModeSubject = BehaviorSubject.createDefault(false)

    private val filesToCopy: LinkedHashSet<FileSource> = LinkedHashSet()
    private val filesToMove: LinkedHashSet<FileSource> = LinkedHashSet()

    private var moveFromFolderId: Long? = null

    fun getFoldersInFolder(
        folderId: Long?,
        searchQuery: String?
    ): Observable<List<FileSource>> {
        //on receive remove files from move/copy of they are not present in list
        return foldersInteractor.getFoldersInFolder(folderId, searchQuery)
    }

    fun getFolderObservable(folderId: Long): Observable<FolderInfo> {
        return foldersInteractor.getFolderObservable(folderId)
    }

    fun playAllMusicInFolder(folderId: Long?): Completable {
        return foldersInteractor.playAllMusicInFolder(folderId)
    }

    fun getAllCompositionsInFolder(folderId: Long?): Single<List<Composition>> {
        return foldersInteractor.getAllCompositionsInFolder(folderId)
    }

    fun getAllCompositionsInFileSources(fileSources: List<FileSource>): Single<List<Composition>> {
        return foldersInteractor.getAllCompositionsInFileSources(fileSources)
    }

    fun play(fileSources: Collection<FileSource>, position: Int = Constants.NO_POSITION): Completable{
        return foldersInteractor.play(ArrayList(fileSources), position)
    }

    fun deleteFiles(fileSources: List<FileSource>): Single<List<DeletedComposition>> {
        return foldersInteractor.deleteFiles(fileSources)
    }

    fun deleteFolder(folder: FolderFileSource): Single<List<DeletedComposition>> {
        return foldersInteractor.deleteFolder(folder)
    }

    fun setFolderOrder(order: Order) {
        foldersInteractor.setFolderOrder(order)
    }

    fun getFolderOrder(): Order {
        return foldersInteractor.getFolderOrder()
    }

    fun saveCurrentFolder(folderId: Long?) {
        foldersInteractor.saveCurrentFolder(folderId)
    }

    fun getCurrentFolderScreens(): Single<List<Long>> {
        return foldersInteractor.getCurrentFolderScreens()
    }

    fun getParentFolders(compositionId: Long): Single<List<Long>> {
        return foldersInteractor.getParentFolders(compositionId)
    }

    fun renameFolder(folderId: Long, newName: String): Completable {
        return foldersInteractor.renameFolder(folderId, newName)
    }

    fun addFilesToMove(folderId: Long?, fileSources: Collection<FileSource>) {
        filesToMove.clear()
        filesToMove.addAll(fileSources)
        moveFromFolderId = folderId
        moveModeSubject.onNext(true)
    }

    fun addFilesToCopy(folderId: Long?, fileSources: Collection<FileSource>) {
        filesToCopy.clear()
        filesToCopy.addAll(fileSources)
        moveFromFolderId = folderId
        moveModeSubject.onNext(true)
    }

    fun stopMoveMode() {
        filesToCopy.clear()
        filesToMove.clear()
        moveFromFolderId = null
        moveModeSubject.onNext(false)
    }

    fun moveFilesTo(folderId: Long?): Completable {
        val completable = if (filesToMove.isNotEmpty()) {
            foldersInteractor.moveFiles(filesToMove, moveFromFolderId, folderId)
        } else if (filesToCopy.isNotEmpty()) {
            Completable.error(Exception("not implemented"))
        } else {
            Completable.complete()
        }
        return completable.doOnComplete { stopMoveMode() }
    }

    fun moveFilesToNewFolder(folderId: Long?, folderName: String): Completable {
        val completable = if (filesToMove.isNotEmpty()) {
            foldersInteractor.moveFilesToNewDirectory(
                filesToMove,
                moveFromFolderId,
                folderId,
                folderName
            )
        } else if (filesToCopy.isNotEmpty()) {
            Completable.error(Exception("not implemented"))
        } else {
            Completable.complete()
        }
        return completable.doOnComplete { stopMoveMode() }
    }

    fun getMoveModeObservable(): BehaviorSubject<Boolean> {
        return moveModeSubject
    }

    fun getFilesToMove(): LinkedHashSet<FileSource> {
        return filesToMove
    }

    fun addFolderToIgnore(folder: FolderFileSource): Single<IgnoredFolder> {
        return foldersInteractor.addFolderToIgnore(folder)
    }

    fun deleteIgnoredFolder(folder: IgnoredFolder): Completable {
        return foldersInteractor.deleteIgnoredFolder(folder)
    }

    fun saveListPosition(folderId: Long?, listPosition: ListPosition) {
        uiStateRepository.saveFolderListPosition(folderId, listPosition)
    }

    fun getSavedListPosition(folderId: Long?): ListPosition? {
        return uiStateRepository.getSavedFolderListPosition(folderId)
    }
}