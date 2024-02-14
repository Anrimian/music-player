package com.github.anrimian.musicplayer.data.storage.files

import java.io.File

object FileManager {

    fun deleteFile(path: String) {
        val file = File(path)
        if (!file.exists()) {
            return
        }
        file.delete()
    }

    fun deleteEmptyDirectory(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            val files = fileOrDirectory.listFiles() ?: return
            for (child in files) {
                if (fileOrDirectory.isDirectory) {
                    deleteEmptyDirectory(child)
                }
            }
            if (files.isEmpty()) {
                fileOrDirectory.delete()
            }
        }
    }
}