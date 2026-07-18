package com.github.anrimian.musicplayer.data.storage.providers.music;

public class FilePathComposition {

    private final Long storageId;
    private final String filePath;

    public FilePathComposition(Long storageId, String filePath) {
        this.storageId = storageId;
        this.filePath = filePath;
    }

    public Long getStorageId() {
        return storageId;
    }

    public String getFilePath() {
        return filePath;
    }
}
