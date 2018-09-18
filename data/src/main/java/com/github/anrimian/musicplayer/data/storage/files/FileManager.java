package com.github.anrimian.musicplayer.data.storage.files;

import java.io.File;

public class FileManager {

    public void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
