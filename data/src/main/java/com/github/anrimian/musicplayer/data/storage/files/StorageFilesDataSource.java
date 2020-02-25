package com.github.anrimian.musicplayer.data.storage.files;

import android.util.Log;

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

    public void renameCompositionsFolder(List<Composition> compositions,
                                         String oldPath,
                                         String newPath) {
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
        renameFile(oldPath, newPath);
        storageMusicProvider.updateCompositionsFilePath(compositions, oldPath, newPath);
//        }
    }

    public void moveCompositionsToFolder(List<Composition> compositions,
                                         String fromPath,
                                         String toPath) {
        Log.d("KEK2", "changeFolderName, fromPath: " + fromPath);
        Log.d("KEK2", "changeFolderName, toPath: " + toPath);

        for (Composition composition: compositions) {
            String oldPath = composition.getFilePath();
            String newPath = FileUtils.getChangedFilePath(oldPath, fromPath, toPath);

            Log.d("KEK2", "rename file, oldPath: " + oldPath);
            Log.d("KEK2", "rename file, newPath: " + newPath);

            renameFile(oldPath, newPath);//can't move folder
        }
        storageMusicProvider.updateCompositionsFilePath(compositions, fromPath, toPath);
    }

    public static void renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            throw new RuntimeException("target file not exists");
        }
        File newFile = new File(newPath);
        boolean renamed = oldFile.renameTo(newFile);
        if (!renamed) {
            throw new RuntimeException("file not renamed");
        }
    }
}
