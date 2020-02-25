package com.github.anrimian.musicplayer.data.storage.files;

import android.os.Build;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.io.File;
import java.util.List;

public class StorageFilesDataSource {

    private final StorageMusicProvider storageMusicProvider;

    public StorageFilesDataSource(StorageMusicProvider storageMusicProvider) {
        this.storageMusicProvider = storageMusicProvider;
    }

    public void renameCompositionsFolder(List<Composition> compositions,
                                         String oldPath,
                                         String newPath,
                                         String newName) {
        Log.d("KEK2", "changeFolderName, oldPath: " + oldPath);
        Log.d("KEK2", "changeFolderName, newPath: " + newPath);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            //not working
            File oldFile = new File(oldPath);
            DocumentFile documentFile = DocumentFile.fromFile(oldFile);
            documentFile.renameTo(newName);
            storageMusicProvider.scanMedia(oldPath);
            storageMusicProvider.scanMedia(newPath);

//            storageMusicProvider.updateCompositionsRelativePath(compositions, oldPath, newPath);
        } else {
            //seems working for android <10, implement for scoped storage
            renameFile(oldPath, newPath);
            storageMusicProvider.updateCompositionsFilePath(compositions, oldPath, newPath);
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
            throw new RuntimeException("file not renamed");
        }
    }
}
