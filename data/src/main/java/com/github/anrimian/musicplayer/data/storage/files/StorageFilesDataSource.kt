package com.github.anrimian.musicplayer.data.storage.files

import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition

interface StorageFilesDataSource {

    /**
     * if dest folder already exist - files will be merged
     * returns id-new_file_name - if name was changed after rename
     */
    fun renameCompositionsDirectory(
        files: List<StorageFileInfo>,
        folderPath: String,
        newFolderPath: String
    ): List<Pair<Long, String>>

    fun renameCompositionFile(storageId: Long, fileName: String): String

    fun moveCompositionsToDirectory(
        files: Collection<StorageFileInfo>,
        fromFolderPath: String,
        toFolderPath: String
    ): List<Pair<Long, String>>

    fun executePathChange(moveOperations: List<StorageMoveOperation>): List<Pair<Long, String>>

    fun deleteCompositionFiles(
        compositions: List<DeletedComposition>,
        tokenForDelete: Any
    ): List<DeletedComposition>

    fun deleteCompositionFile(composition: DeletedComposition): DeletedComposition

    fun clearDeleteData()

}