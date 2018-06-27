package com.github.anrimian.simplemusicplayer.data.utils;

import java.io.File;

public class FileUtils {

    public static String getParentDirPath(String path) {
        int lastSeparatorIndex = path.lastIndexOf("/");
        if (lastSeparatorIndex != -1) {
            return path.substring(0, lastSeparatorIndex);
        }
        return path;
    }


    public static String getFileName(String path) {
        int lastSeparatorIndex = path.lastIndexOf("/");
        if (lastSeparatorIndex != -1) {
            return path.substring(lastSeparatorIndex + 1, path.length());
        }
        return path;
    }
}
