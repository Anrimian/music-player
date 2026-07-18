package com.github.anrimian.musicplayer.domain.utils

import java.io.File

object FileUtilsKt {

    /**
     * Replaces the base name of a file path/name string with a new base name,
     * keeping the original extension.
     * Assumes inputs are non-null and non-empty.
     *
     * @param originalPath The full original file path or just a file name (e.g., "/path/to/old.mp3" or "old.jpg").
     * @param newBaseName New base name without extension (e.g., "new" or "updated").
     * @return New file path/name string (e.g., "/path/to/new.mp3" or "updated.jpg").
     */
    fun applyNewName(originalPath: String, newBaseName: String): String {
        val lastSeparatorIdx = originalPath.lastIndexOf(File.separatorChar)

        val directoryPathPart = if (lastSeparatorIdx != -1) {
            originalPath.substring(0, lastSeparatorIdx + 1)
        } else {
            ""
        }
        val fileNamePart = if (lastSeparatorIdx != -1) {
            originalPath.substring(lastSeparatorIdx + 1)
        } else {
            originalPath
        }

        val dotIdx = fileNamePart.lastIndexOf('.')
        val newNameWithExt = if (dotIdx > 0 && dotIdx < fileNamePart.length - 1) {
            val extension = fileNamePart.substring(dotIdx)
            newBaseName + extension
        } else {
            // Original file had no extension or dot was at the start/end in the file name part
            newBaseName
        }
        return directoryPathPart + newNameWithExt
    }

    /**
     * Safely combines a parent path and a file name into a full path,
     * correctly handling the path separator.
     *
     * @param parentPath The parent directory path.
     * @param fileName The name of the file.
     * @return A correctly formatted full path.
     */
    fun buildFullPath(parentPath: String, fileName: String): String {
        if (parentPath.isEmpty()) {
            return fileName
        }

        return if (parentPath.endsWith("/")) {
            parentPath + fileName
        } else {
            "$parentPath/$fileName"
        }
    }

}