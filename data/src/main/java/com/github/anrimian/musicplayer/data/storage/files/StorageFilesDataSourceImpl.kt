package com.github.anrimian.musicplayer.data.storage.files

import com.github.anrimian.musicplayer.data.repositories.library.edit.models.CompositionMoveData
import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.exceptions.FileWriteNotAllowedException
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import java.io.File
import java.util.LinkedList

class StorageFilesDataSourceImpl(
    private val storageMusicProvider: StorageMusicProvider
) : StorageFilesDataSource {

    override fun renameCompositionsFolder(
        compositions: Collection<CompositionMoveData>,
        folderRelativePath: String,
        newDirectoryName: String,
    ): List<Pair<Long, String>> {
        if (compositions.isEmpty()) {
            return emptyList()
        }
        val newFolderRelativePath = FileUtils.replaceFileName(folderRelativePath, newDirectoryName)
        val updatedCompositions = LinkedList<FilePathComposition>()
        val updatedNames = LinkedList<Pair<Long, String>>()
        val filesToRename = LinkedList<Pair<String, String>>()
        var folderPath: String? = null
        var folderNewFullPath: String? = null
        for (composition in compositions) {
            val storageId = composition.storageId ?: continue
            val filePath = getCompositionFilePath(storageId)

            val newPath = filePath.replace(folderRelativePath, newFolderRelativePath)
            val formattedNewPath = FileUtils.getUniqueFilePath(newPath)
            if (formattedNewPath != newPath) {
                filesToRename.add(filePath to formattedNewPath)
                updatedNames.add(composition.id to FileUtils.getFileName(formattedNewPath))
            }
            updatedCompositions.add(FilePathComposition(storageId, formattedNewPath))

            if (folderPath == null) {
                folderPath = filePath.substring(0, filePath.indexOf(folderRelativePath) + folderRelativePath.length)
                folderNewFullPath = folderPath.replace(folderRelativePath, newFolderRelativePath)
            }
        }

        for (paths in filesToRename) {
            renameFile(paths.first, paths.second)
        }
        renameFolder(folderPath!!, folderNewFullPath!!)
        storageMusicProvider.updateCompositionsFilePath(updatedCompositions)

        return updatedNames
    }

    override fun renameCompositionFile(composition: CompositionMoveData, fileName: String): String {
        val storageId = composition.storageId ?: return fileName
        val oldPath = getCompositionFilePath(storageId)
        val newPath = FileUtils.replaceFileName(oldPath, fileName)
        val formattedNewPath = FileUtils.getUniqueFilePath(newPath)

        renameFile(oldPath, formattedNewPath)

        storageMusicProvider.updateCompositionsFilePath(
            listOf(FilePathComposition(storageId, formattedNewPath))
        )

        return FileUtils.getFileName(formattedNewPath)
    }

    override fun moveCompositionsToDirectory(
        compositions: Collection<CompositionMoveData>,
        fromFolderRelativePath: String,
        toFolderRelativePath: String,
    ): List<Pair<Long, String>> {
        val updatedCompositions = LinkedList<FilePathComposition>()
        val updatedNames = LinkedList<Pair<Long, String>>()
        val filesToMove = LinkedList<Pair<String, String>>()
        for (composition in compositions) {
            val storageId = composition.storageId ?: continue
            val filePath = getCompositionFilePath(storageId)

            val newPath = if (fromFolderRelativePath.isEmpty()) {
                val lastDelimiterIndex = filePath.lastIndexOf('/')//case without delimiters?
                filePath.replaceRange(lastDelimiterIndex, lastDelimiterIndex, "/$toFolderRelativePath")
            } else {
                val formattedFromFolderRelativePath = if (toFolderRelativePath.isEmpty()) {
                    "/$fromFolderRelativePath"
                } else {
                    fromFolderRelativePath
                }
                filePath.replace(formattedFromFolderRelativePath, toFolderRelativePath)
            }
            val formattedNewPath = FileUtils.getUniqueFilePath(newPath)
            if (formattedNewPath != newPath) {
                updatedNames.add(composition.id to FileUtils.getFileName(formattedNewPath))
            }
            updatedCompositions.add(FilePathComposition(storageId, formattedNewPath))
            filesToMove.add(filePath to formattedNewPath)
        }
        for (paths in filesToMove) {
            moveFile(paths.first, paths.second)
        }
        storageMusicProvider.updateCompositionsFilePath(updatedCompositions)
        return updatedNames
    }

    override fun deleteCompositionFiles(
        compositions: List<DeletedComposition>,
        tokenForDelete: Any
    ): List<DeletedComposition> {
        for (composition in compositions) {
            deleteFile(composition)
        }
        storageMusicProvider.deleteCompositions(
            compositions.mapNotNull(DeletedComposition::storageId)
        )
        return compositions
    }

    override fun deleteCompositionFile(composition: DeletedComposition): DeletedComposition {
        deleteFile(composition)
        val storageId = composition.storageId
        if (storageId != null) {
            storageMusicProvider.deleteComposition(storageId)
        }
        return composition
    }

    override fun clearDeleteData() {}

    private fun deleteFile(composition: DeletedComposition) {
        val storageId = composition.storageId ?: return
        val filePath = storageMusicProvider.getCompositionFilePath(storageId) ?: return
        val parentDirectory = File(filePath).parentFile
        FileManager.deleteFile(filePath)
        if (parentDirectory != null) {
            FileManager.deleteEmptyDirectory(parentDirectory)
        }
    }

    private fun getCompositionFilePath(storageId: Long): String {
        return storageMusicProvider.getCompositionFilePath(storageId)
            ?: throw RuntimeException("composition path not found in system media store")
    }

    private fun moveFile(oldPath: String, newPath: String) {
        val destDirectoryPath = FileUtils.getParentDirPath(newPath)
        val destDirectory = File(destDirectoryPath)
        if (!destDirectory.exists()) {
            val created = destDirectory.mkdirs()
            if (!created) {
                throw RuntimeException("parent directory wasn't created")
            }
        }
        renameFile(oldPath, newPath)
        val oldDirectoryPath = FileUtils.getParentDirPath(oldPath)
        val oldDirectory = File(oldDirectoryPath)
        val files = oldDirectory.list()
        if (oldDirectory.isDirectory && files != null && files.isEmpty()) {
            //not necessary to check
            oldDirectory.delete()
        }
    }

    private fun renameFile(oldPath: String, newPath: String) {
        val oldFile = File(oldPath)
        if (!oldFile.exists()) {
            throw RuntimeException("target file not exists")
        }
        if (!oldFile.canWrite()) {
            throw FileWriteNotAllowedException("file write is not allowed")
        }
        val newFile = File(newPath)
        val renamed = oldFile.renameTo(newFile)
        if (!renamed) {
            throw RuntimeException("file wasn't renamed")
        }
    }

    private fun renameFolder(oldPath: String, newPath: String) {
        val oldFile = File(oldPath)
        if (!oldFile.exists()) {
            throw RuntimeException("target file not exists")
        }
        if (!oldFile.canWrite()) {
            throw FileWriteNotAllowedException("file write is not allowed")
        }
        val newFile = File(newPath)
        if (newFile.exists()) {
            FileManager.deleteEmptyDirectory(oldFile)
            return
        }
        val renamed = oldFile.renameTo(newFile)
        if (!renamed) {
            throw RuntimeException("file wasn't renamed")
        }
    }

}