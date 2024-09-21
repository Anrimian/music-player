package com.github.anrimian.musicplayer.domain.utils;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

import com.github.anrimian.musicplayer.domain.models.exceptions.FileWriteNotAllowedException;

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
        String displayPath = path;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            displayPath = path.substring(++lastSlashIndex);
        }
        return displayPath;
    }

    public static String formatFileName(String filePath) {
        return formatFileName(filePath, false);
    }

    public static String formatFileName(String filePath, boolean showExtension) {
        if (isEmpty(filePath)) {
            return "";
        }
        String fileName = getFileName(filePath);
        if (!showExtension) {
            int cropIndex = fileName.lastIndexOf('.');
            if (cropIndex != -1) {
                return fileName.substring(0, cropIndex);
            }
        }
        return fileName;
    }

    public static String replaceFileName(String fullPath, String newFileName) {
        String fileName = FileUtils.formatFileName(fullPath);
        if (fileName.equals(newFileName)) {
            return fullPath;
        }
        return TextUtils.replaceLast(fullPath, fileName, newFileName);
    }

    public static String getChangedFilePath(String fullPath, String newFileName) {
        String newPath = replaceFileName(fullPath, newFileName);
        return getUniqueFilePath(newPath, newFileName);
    }

    public static String getChangedFilePath(String filePath, String oldPath, String newPath) {
        String fileName = FileUtils.formatFileName(filePath);
        String path = filePath.replaceFirst(oldPath, newPath);
        return getUniqueFilePath(path, fileName);
    }

    public static String safeReplacePath(String filePath, String oldPath, String newPath) {
        String fileName = FileUtils.formatFileName(filePath);
        String path = filePath.replace(oldPath, newPath);
        return getUniqueFilePath(path, fileName);
    }

    private static String getUniqueFilePath(String filePath, String fileName) {
        File file = new File(filePath);
        int filesCount = 0;
        String newFileName;
        if (file.exists()) {
            while (file.exists()) {
                filesCount++;

                newFileName = fileName + "(" + filesCount + ")";//hmm, check on new name like name(1)?
                String newPath = filePath.replace(fileName, newFileName);

                file = new File(newPath);
            }
        }
        return file.getPath();
    }

    public static String getUniqueFilePath(String filePath) {
        File file = new File(filePath);
        int filesCount = 0;
        String fileName = FileUtils.formatFileName(filePath);
        if (file.exists()) {
            while (file.exists()) {
                filesCount++;

                String newFileName = fileName + " (" + filesCount + ")";
                String newPath = filePath.replace(fileName, newFileName);
                fileName = newFileName;

                file = new File(newPath);
            }
        }
        return file.getPath();
    }

    public static void moveFile(String oldPath, String newPath) {
        String destDirectoryPath = FileUtils.getParentDirPath(newPath);
        File destDirectory = new File(destDirectoryPath);
        if (!destDirectory.exists()) {
            boolean created = destDirectory.mkdirs();
            if (!created) {
                throw new RuntimeException("parent directory wasn't created");
            }
        }
        renameFile(oldPath, newPath);
        String oldDirectoryPath = FileUtils.getParentDirPath(oldPath);
        File oldDirectory = new File(oldDirectoryPath);
        String[] files = oldDirectory.list();
        if (oldDirectory.isDirectory() && files != null && files.length == 0) {
            //not necessary to check
            oldDirectory.delete();
        }
    }

    public static void renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            throw new RuntimeException("target file not exists");
        }
        if (!oldFile.canWrite()) {
            throw new FileWriteNotAllowedException("file write is not allowed");
        }
        File newFile = new File(newPath);
        boolean renamed = oldFile.renameTo(newFile);
        if (!renamed) {
            throw new RuntimeException("file wasn't renamed");
        }
    }

}
