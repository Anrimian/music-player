package com.github.anrimian.musicplayer.data.storage.files

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.anrimian.musicplayer.data.repositories.library.edit.models.CompositionMoveData
import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import java.util.LinkedList

@RequiresApi(api = Build.VERSION_CODES.R)
class StorageFilesDataSourceApi30(
    private val storageMusicProvider: StorageMusicProvider
) : StorageFilesDataSource {

    private var latestCompositionsToDelete: List<DeletedComposition>? = null
    private var tokenForDelete: Any? = null

    //previous folder will be not deleted, no direct access to folder
    override fun renameCompositionsFolder(
        compositions: Collection<CompositionMoveData>,
        folderRelativePath: String,
        newDirectoryName: String,
    ): List<Pair<Long, String>> {
        val newFolderRelativePath = FileUtils.replaceFileName(folderRelativePath, newDirectoryName)
        val updatedCompositions = LinkedList<FilePathComposition>()
        for (composition in compositions) {
            val storageId = composition.storageId ?: continue
            val relativePath = getCompositionRelativePath(storageId)
            val newRelativePath = relativePath.replace(folderRelativePath, newFolderRelativePath)
            updatedCompositions.add(FilePathComposition(storageId, newRelativePath))
        }
        storageMusicProvider.updateCompositionsRelativePath(updatedCompositions)

        //in case that in destination folder were files with same names - request and compare names
        val updatedNames = LinkedList<Pair<Long, String>>()
        for (composition in compositions) {
            val storageId = composition.storageId ?: continue
            val name = storageMusicProvider.getCompositionFileName(storageId) ?: continue
            if (name != composition.fileName) {
                updatedNames.add(composition.id to name)
            }
        }
        return updatedNames
    }

    override fun renameCompositionFile(composition: CompositionMoveData, fileName: String): String {
        val storageId = composition.storageId ?: return fileName
        storageMusicProvider.updateCompositionFileName(storageId, fileName)
        return storageMusicProvider.getCompositionFileName(storageId)!!
    }

    override fun moveCompositionsToDirectory(
        compositions: Collection<CompositionMoveData>,
        fromFolderRelativePath: String,
        toFolderRelativePath: String,
    ): List<Pair<Long, String>> {
        val updatedCompositions = LinkedList<FilePathComposition>()
        for (composition in compositions) {
            val storageId = composition.storageId ?: continue
            val relativePath = getCompositionRelativePath(storageId).trim()

            val newRelativePath = if (fromFolderRelativePath.isEmpty()) {
                //from root folder case: relativePath = "/ "
                val lastDelimiterIndex = relativePath.lastIndexOf('/')//case without delimiters?
                relativePath.replaceRange(lastDelimiterIndex, lastDelimiterIndex + 1, toFolderRelativePath)
            } else {
                relativePath.replace(fromFolderRelativePath, toFolderRelativePath)
            }
            updatedCompositions.add(FilePathComposition(composition.storageId, newRelativePath))
        }
        storageMusicProvider.updateCompositionsRelativePath(updatedCompositions)

        //in case that in destination folder were files with same names - request and compare names
        val updatedNames = LinkedList<Pair<Long, String>>()
        for (composition in compositions) {
            val storageId = composition.storageId ?: continue
            val name = storageMusicProvider.getCompositionFileName(storageId) ?: continue
            if (name != composition.fileName) {
                updatedNames.add(composition.id to name)
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
        storageMusicProvider.deleteCompositions(
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

    private fun getCompositionRelativePath(storageId: Long): String {
        return storageMusicProvider.getCompositionRelativePath(storageId)
            ?: throw RuntimeException("composition path not found in system media store")
    }

}