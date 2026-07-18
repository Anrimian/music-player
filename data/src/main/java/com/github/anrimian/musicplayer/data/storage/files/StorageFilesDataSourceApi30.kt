package com.github.anrimian.musicplayer.data.storage.files

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import java.util.LinkedList

@RequiresApi(api = Build.VERSION_CODES.R)
class StorageFilesDataSourceApi30(
    private val systemAudioCatalogProvider: SystemAudioCatalogProvider
) : StorageFilesDataSource {

    private var latestCompositionsToDelete: List<DeletedComposition>? = null
    private var tokenForDelete: Any? = null

    // previous folder will be not deleted, no direct access to folder
    override fun renameCompositionsDirectory(
        files: List<StorageFileInfo>,
        folderPath: String,
        newFolderPath: String,
    ): List<Pair<Long, String>> {
        if (files.isEmpty()) {
            return emptyList()
        }
        val updatedCompositions = LinkedList<FilePathComposition>()
        for (file in files) {
            val storageId = file.storageId
            val filePath = getCompositionFilePath(storageId)
            val newPath = filePath.replace(folderPath, newFolderPath)
            updatedCompositions.add(FilePathComposition(storageId, newPath))
        }
        systemAudioCatalogProvider.updateCompositionsFilePath(updatedCompositions)

        // in case that in destination folder were files with same names - request and compare names
        val updatedNames = LinkedList<Pair<Long, String>>()
        for (file in files) {
            val storageId = file.storageId
            val name = systemAudioCatalogProvider.getCompositionFileName(storageId) ?: continue
            if (name != file.fileName) {
                updatedNames.add(file.id to name)
            }
        }
        return updatedNames
    }

    override fun renameCompositionFile(storageId: Long, fileName: String): String {
        val oldPath = getCompositionFilePath(storageId)
        val newPath = FileUtils.replaceFileName(oldPath, fileName)
        systemAudioCatalogProvider.updateCompositionsFilePath(
            listOf(FilePathComposition(storageId, newPath))
        )
        return systemAudioCatalogProvider.getCompositionFileName(storageId)!!
    }

    override fun moveCompositionsToDirectory(
        files: Collection<StorageFileInfo>,
        fromFolderPath: String,
        toFolderPath: String,
    ): List<Pair<Long, String>> {
        val updatedCompositions = LinkedList<FilePathComposition>()
        for (composition in files) {
            val storageId = composition.storageId
            val filePath = getCompositionFilePath(storageId)

            val newPath = if (fromFolderPath.isEmpty()) {
                val lastDelimiterIndex = filePath.lastIndexOf('/')//case without delimiters?
                filePath.replaceRange(lastDelimiterIndex, lastDelimiterIndex + 1, toFolderPath)
            } else {
                filePath.replace(fromFolderPath, toFolderPath)
            }
            updatedCompositions.add(FilePathComposition(storageId, newPath))
        }
        systemAudioCatalogProvider.updateCompositionsFilePath(updatedCompositions)

        //in case that in destination folder were files with same names - request and compare names
        val updatedNames = LinkedList<Pair<Long, String>>()
        for (composition in files) {
            val storageId = composition.storageId
            val name = systemAudioCatalogProvider.getCompositionFileName(storageId) ?: continue
            if (name != composition.fileName) {
                updatedNames.add(composition.id to name)
            }
        }
        return updatedNames
    }

    override fun executePathChange(
        moveOperations: List<StorageMoveOperation>
    ): List<Pair<Long, String>> {
        val updatedCompositions = LinkedList<FilePathComposition>()
        for (operation in moveOperations) {
            val storageId = operation.file.storageId
            val newPath = operation.newPath
            updatedCompositions.add(FilePathComposition(storageId, newPath))
        }

        systemAudioCatalogProvider.updateCompositionsFilePath(updatedCompositions)

        // in case that in destination folder were files with same names - request and compare names
        val updatedNames = LinkedList<Pair<Long, String>>()
        for (operation in moveOperations) {
            val file = operation.file
            val storageId = file.storageId

            val actualFileName = systemAudioCatalogProvider.getCompositionFileName(storageId)

            val intendedFileName = file.fileName
            if (actualFileName != null && actualFileName != intendedFileName) {
                val compositionId = file.id
                updatedNames.add(compositionId to actualFileName)
            }
        }

        return updatedNames
    }

    //empty root folder will be not deleted, no direct access to folders
    override fun deleteCompositionFiles(
        compositions: List<DeletedComposition>,
        tokenForDelete: Any
    ): List<DeletedComposition> {
        // From android 11 delete actions are started twice.
        // Moreover, files will be deleted by the system after dialog confirmation.
        // So, on second attempt composition list can be null when it is received from folder by db query
        // So token for delete represent folder object that is not changed on second attempt
        if (tokenForDelete == this.tokenForDelete) {
            val listToReturn = latestCompositionsToDelete
            latestCompositionsToDelete = null
            this.tokenForDelete = null
            return listToReturn!!
        }
        latestCompositionsToDelete = compositions
        this.tokenForDelete = tokenForDelete
        systemAudioCatalogProvider.deleteCompositions(
            compositions.mapNotNull(DeletedComposition::storageId)
        )
        // we are always expecting exception from deleteCompositions() here. But
        // deleteCompositions() can be executed successfully on first attempt when we have created these files
        // in this case clean token and compositions
        val result = latestCompositionsToDelete
        latestCompositionsToDelete = null
        this.tokenForDelete = null
        return result!!
    }

    override fun deleteCompositionFile(composition: DeletedComposition): DeletedComposition {
        return deleteCompositionFiles(listOf(composition), composition)[0]
    }

    override fun clearDeleteData() {
        latestCompositionsToDelete = null
        tokenForDelete = null
    }

    private fun getCompositionFilePath(storageId: Long): String {
        return systemAudioCatalogProvider.getCompositionFilePath(storageId)
            ?: throw RuntimeException("composition path not found in system media store")
    }

}