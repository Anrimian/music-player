package com.github.anrimian.musicplayer.data.storage.files

import com.github.anrimian.musicplayer.data.repositories.library.edit.models.CompositionMoveData
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition

interface StorageFilesDataSource {

    /**
     * if dest folder already exist - files will be merged
     * returns id-new_file_name - if name was changed after rename
     */
    fun renameCompositionsFolder(
        compositions: Collection<CompositionMoveData>,
        folderRelativePath: String,
        newDirectoryName: String
    ): List<Pair<Long, String>>

    fun renameCompositionFile(composition: CompositionMoveData, fileName: String): String

    fun moveCompositionsToDirectory(
        compositions: Collection<CompositionMoveData>,
        fromFolderRelativePath: String,
        toFolderRelativePath: String
    ): List<Pair<Long, String>>

    fun deleteCompositionFiles(
        compositions: List<DeletedComposition>,
        tokenForDelete: Any
    ): List<DeletedComposition>

    fun deleteCompositionFile(composition: DeletedComposition): DeletedComposition

    fun clearDeleteData()

}