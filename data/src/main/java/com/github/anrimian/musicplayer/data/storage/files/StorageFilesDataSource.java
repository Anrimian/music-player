package com.github.anrimian.musicplayer.data.storage.files;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.io.File;
import java.util.List;

public class StorageFilesDataSource {

    private final StorageMusicProvider storageMusicProvider;

    public StorageFilesDataSource(StorageMusicProvider storageMusicProvider) {
        this.storageMusicProvider = storageMusicProvider;
    }

    //rename to duplicate path
    public String renameCompositionsFolder(List<Composition> compositions,
                                           String oldPath,
                                           String newName) {
//        Log.d("KEK2", "changeFolderName, oldPath: " + oldPath);
//        Log.d("KEK2", "changeFolderName, newPath: " + newPath);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            String newName = FileUtils.formatFileName(newPath);
//            File oldFile = new File(oldPath);
//            DocumentFile documentFile = DocumentFile.fromFile(oldFile);
//            documentFile.renameTo(newName);
//            storageMusicProvider.scanMedia(oldPath);
//            storageMusicProvider.scanMedia(newPath);
//        } else {
        //seems working for android <10, implement for scoped storage
        String newPath = FileUtils.getChangedFilePath(oldPath, newName);
        renameFile(oldPath, newPath);
        storageMusicProvider.updateCompositionsFilePath(compositions, oldPath, newPath);
        return newPath;
//        }
    }

    public void moveCompositionsToFolder(List<Composition> compositions,
                                         String fromPath,
                                         String toPath) {
//        Log.d("KEK2", "changeFolderName, fromPath: " + fromPath);
//        Log.d("KEK2", "changeFolderName, toPath: " + toPath);

        for (Composition composition: compositions) {
            String oldPath = composition.getFilePath();
            String newPath = FileUtils.getChangedFilePath(oldPath, fromPath, toPath);

//            Log.d("KEK2", "rename file, oldPath: " + oldPath);
//            Log.d("KEK2", "rename file, newPath: " + newPath);

            moveFile(oldPath, newPath);//can't move folder
        }
        storageMusicProvider.updateCompositionsFilePath(compositions, fromPath, toPath);
    }

    private static void moveFile(String oldPath,
                                 String newPath) {
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
            //noinspection ResultOfMethodCallIgnored
            oldDirectory.delete();
        }
    }

    public static void renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            throw new RuntimeException("target file not exists");
        }
        File newFile = new File(newPath);
        boolean renamed = oldFile.renameTo(newFile);
        if (!renamed) {
            throw new RuntimeException("file wasn't renamed");
        }
    }
}
