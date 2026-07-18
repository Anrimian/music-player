package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.composition.change.MoveRequest
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FilesChangeResult
import io.reactivex.rxjava3.core.Single

interface LibraryFilesRepository {

    fun changeCompositionFileName(
        compositionId: Long,
        fileName: String
    ): Single<FilesChangeResult>

    fun changeFolderName(
        folderId: Long,
        newFolderName: String
    ): Single<FilesChangeResult>

    fun moveFiles(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        toFolderId: Long?
    ): Single<FilesChangeResult>

    fun moveFilesToNewDirectory(
        files: Collection<FileSource>,
        fromFolderId: Long?,
        targetParentFolderId: Long?,
        directoryName: String
    ): Single<FilesChangeResult>

    fun moveFiles(requests: List<MoveRequest>): Single<FilesChangeResult>

}