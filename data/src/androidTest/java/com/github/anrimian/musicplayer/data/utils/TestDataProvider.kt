package com.github.anrimian.musicplayer.data.utils

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageAudioFile

object TestDataProvider {

    fun createFakeStorageFile(
        storageId: Long,
        parentPath: String,
        fileName: String,
    ): StorageAudioFile {
        return StorageAudioFile(
            storageId = storageId,
            parentPath = parentPath,
            fileName = fileName,
            artist = "Test Artist",
            title = "Test Title",
            duration = 120000,
            size = 1024,
            addedTime = System.currentTimeMillis(),
            modifiedTime = System.currentTimeMillis()
        )
    }

}
