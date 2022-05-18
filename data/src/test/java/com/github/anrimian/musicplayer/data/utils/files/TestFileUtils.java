package com.github.anrimian.musicplayer.data.utils.files;

import com.google.common.io.Files;

import java.io.File;

public class TestFileUtils {

    public static File createTempCopy(File tempDir, String filePath) {
        try {
            File original = new File(filePath);
            File tmp = new File(tempDir, original.getName());
            //noinspection ResultOfMethodCallIgnored
            tmp.createNewFile();
            //noinspection UnstableApiUsage
            Files.copy(original, tmp);
            return tmp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
