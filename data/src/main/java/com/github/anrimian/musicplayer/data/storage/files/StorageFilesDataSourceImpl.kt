package com.github.anrimian.musicplayer.data.storage.files

import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.exceptions.FileWriteNotAllowedException
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import java.io.File
import java.util.LinkedList

class StorageFilesDataSourceImpl(
    private val systemAudioCatalogProvider: SystemAudioCatalogProvider
) : StorageFilesDataSource {

    override fun renameCompositionsDirectory(
        files: List<StorageFileInfo>,
        folderPath: String,
        newFolderPath: String,
    ): List<Pair<Long, String>> {
        if (files.isEmpty()) {
            return emptyList()
        }
        val updatedCompositions = LinkedList<FilePathComposition>()
        val updatedNames = LinkedList<Pair<Long, String>>()
        val filesToRename = LinkedList<Pair<String, String>>()
        var oldFolderPath: String? = null
        var folderNewFullPath: String? = null
        for (file in files) {
            val storageId = file.storageId
            val filePath = getCompositionFilePath(storageId)

            val newPath = filePath.replace(folderPath, newFolderPath)
            val formattedNewPath = FileUtils.getUniqueFilePath(newPath)
            if (formattedNewPath != newPath) {
                filesToRename.add(filePath to formattedNewPath)
                updatedNames.add(file.id to FileUtils.getFileName(formattedNewPath))
            }
            updatedCompositions.add(FilePathComposition(storageId, formattedNewPath))

            if (oldFolderPath == null) {
                oldFolderPath = filePath.substring(0, filePath.indexOf(folderPath) + folderPath.length)
                folderNewFullPath = oldFolderPath.replace(folderPath, newFolderPath)
            }
        }

        for (paths in filesToRename) {
            FileUtils.renameFile(paths.first, paths.second)
        }
        renameFolder(oldFolderPath!!, folderNewFullPath!!)
        systemAudioCatalogProvider.updateCompositionsFilePath(updatedCompositions)

        return updatedNames
    }

    override fun renameCompositionFile(storageId: Long, fileName: String): String {
        val oldPath = getCompositionFilePath(storageId)
        val newPath = FileUtils.replaceFileName(oldPath, fileName)
        val formattedNewPath = FileUtils.getUniqueFilePath(newPath)

        FileUtils.renameFile(oldPath, formattedNewPath)

        systemAudioCatalogProvider.updateCompositionsFilePath(
            listOf(FilePathComposition(storageId, formattedNewPath))
        )

        return FileUtils.getFileName(formattedNewPath)
    }

    override fun moveCompositionsToDirectory(
        files: Collection<StorageFileInfo>,
        fromFolderPath: String,
        toFolderPath: String,
    ): List<Pair<Long, String>> {
        val updatedCompositions = LinkedList<FilePathComposition>()
        val updatedNames = LinkedList<Pair<Long, String>>()
        val filesToMove = LinkedList<Pair<String, String>>()
        for (composition in files) {
            val storageId = composition.storageId
            val filePath = getCompositionFilePath(storageId)

            val newPath = if (fromFolderPath.isEmpty()) {
                val lastDelimiterIndex = filePath.lastIndexOf('/')//case without delimiters?
                filePath.replaceRange(lastDelimiterIndex, lastDelimiterIndex, "/$toFolderPath")
            } else {
                val formattedFromFolderRelativePath = if (toFolderPath.isEmpty()) {
                    "/$fromFolderPath"
                } else {
                    fromFolderPath
                }
                filePath.replace(formattedFromFolderRelativePath, toFolderPath)
            }
            val formattedNewPath = FileUtils.getUniqueFilePath(newPath)
            if (formattedNewPath != newPath) {
                updatedNames.add(composition.id to FileUtils.getFileName(formattedNewPath))
            }
            updatedCompositions.add(FilePathComposition(storageId, formattedNewPath))
            filesToMove.add(filePath to formattedNewPath)
        }
        for (paths in filesToMove) {
            FileUtils.moveFile(paths.first, paths.second)
        }
        systemAudioCatalogProvider.updateCompositionsFilePath(updatedCompositions)
        return updatedNames
    }

    override fun executePathChange(
        moveOperations: List<StorageMoveOperation>
    ): List<Pair<Long, String>> {
        val updatedCompositions = LinkedList<FilePathComposition>()
        val changedFileNames = LinkedList<Pair<Long, String>>()
        val filesToMove = LinkedList<Pair<String, String>>()

        for (operation in moveOperations) {
            val fileInfo = operation.file
            val storageId = fileInfo.storageId
            val compositionId = fileInfo.id
            val newFullPath = operation.newPath

            val oldPath = getCompositionFilePath(storageId)

            val formattedNewPath = FileUtils.getUniqueFilePath(newFullPath)
            val formattedNewFileName = FileUtils.getFileName(formattedNewPath)

            if (formattedNewFileName != fileInfo.fileName) {
                changedFileNames.add(compositionId to formattedNewFileName)
            }

            filesToMove.add(oldPath to formattedNewPath)
            updatedCompositions.add(FilePathComposition(storageId, formattedNewPath))
        }

        for ((oldPath, newPath) in filesToMove) {
            FileUtils.moveFile(oldPath, newPath)
        }

        systemAudioCatalogProvider.updateCompositionsFilePath(updatedCompositions)

        return changedFileNames
    }

    override fun deleteCompositionFiles(
        compositions: List<DeletedComposition>,
        tokenForDelete: Any
    ): List<DeletedComposition> {
        for (composition in compositions) {
            deleteFile(composition)
        }
        systemAudioCatalogProvider.deleteCompositions(
            compositions.mapNotNull(DeletedComposition::storageId)
        )
        return compositions
    }

    override fun deleteCompositionFile(composition: DeletedComposition): DeletedComposition {
        deleteFile(composition)
        val storageId = composition.storageId
        if (storageId != null) {
            systemAudioCatalogProvider.deleteComposition(storageId)
        }
        return composition
    }

    override fun clearDeleteData() {}

    private fun deleteFile(composition: DeletedComposition) {
        val storageId = composition.storageId ?: return
        val filePath = systemAudioCatalogProvider.getCompositionFilePath(storageId) ?: return
        val parentDirectory = File(filePath).parentFile
        FileManager.deleteFile(filePath)
        if (parentDirectory != null) {
            FileManager.deleteEmptyDirectory(parentDirectory)
        }
    }

    private fun getCompositionFilePath(storageId: Long): String {
        return systemAudioCatalogProvider.getCompositionFilePath(storageId)
            ?: throw RuntimeException("composition path not found in system media store")
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