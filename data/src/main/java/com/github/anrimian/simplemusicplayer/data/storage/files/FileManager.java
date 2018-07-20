package com.github.anrimian.simplemusicplayer.data.storage.files;

import com.github.anrimian.simplemusicplayer.data.models.exceptions.DeleteFileException;

import java.io.File;

public class FileManager {

    public void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        boolean deleted = file.delete();
        if (!deleted) {
            throw new DeleteFileException("can not delete file: " + path);
        }
    }
}
