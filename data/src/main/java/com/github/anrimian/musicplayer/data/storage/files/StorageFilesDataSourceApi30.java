package com.github.anrimian.musicplayer.data.storage.files;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;

import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapListNotNull;

@RequiresApi(api = Build.VERSION_CODES.R)
public class StorageFilesDataSourceApi30 implements StorageFilesDataSource {

    private final StorageMusicProvider storageMusicProvider;

    @Nullable
    private List<Composition> latestCompositionsToDelete;

    public StorageFilesDataSourceApi30(StorageMusicProvider storageMusicProvider) {
        this.storageMusicProvider = storageMusicProvider;
    }

    @Override
    public String renameCompositionsFolder(List<Composition> compositions,
                                           String oldPath,
                                           String newName,
                                           List<FilePathComposition> updatedCompositions) {
        String newPath = FileUtils.getChangedFilePath(oldPath, newName);
        for (Composition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                String oldFilePath = getCompositionFilePath(storageId);
                String newFilePath = FileUtils.safeReplacePath(oldFilePath, oldPath, newPath);

                updatedCompositions.add(new FilePathComposition(
                        composition.getId(),
                        composition.getStorageId(),
                        newFilePath)
                );
            }
        }
        storageMusicProvider.updateCompositionsRelativePath(updatedCompositions);
        return FileUtils.getFileName(newPath);
    }

    @Override
    public Pair<String, String> renameCompositionFile(FullComposition composition, String fileName) {
        Long storageId = composition.getStorageId();
        if (storageId == null) {
            return new Pair<>(null, fileName);
        }

        storageMusicProvider.updateCompositionFileName(storageId, fileName);

        String newFileName = storageMusicProvider.getCompositionFileName(storageId);

        return new Pair<>(null, newFileName);
    }

    @Override
    public List<FilePathComposition> moveCompositionsToFolder(List<Composition> compositions,
                                                              String fromPath,
                                                              String toPath) {
        List<FilePathComposition> updatedCompositions = new LinkedList<>();
        for (Composition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                String oldPath = getCompositionFilePath(storageId);
                String newPath = FileUtils.safeReplacePath(oldPath, fromPath, toPath);

                updatedCompositions.add(new FilePathComposition(composition.getId(),
                        composition.getStorageId(),
                        newPath)
                );
            }
        }
        storageMusicProvider.updateCompositionsRelativePath(updatedCompositions);
        return updatedCompositions;
    }

    @Override
    public String moveCompositionsToNewFolder(List<Composition> compositions,
                                              String fromPath,
                                              String toParentPath,
                                              String directoryName,
                                              List<FilePathComposition> updatedCompositions) {
        String newFolderPath = toParentPath + '/' + directoryName;

        for (Composition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                String oldPath = getCompositionFilePath(storageId);
                String newPath = FileUtils.getChangedFilePath(oldPath, fromPath, newFolderPath);

                updatedCompositions.add(new FilePathComposition(composition.getId(),
                        composition.getStorageId(),
                        newPath)
                );
            }
        }
        storageMusicProvider.updateCompositionsRelativePath(updatedCompositions);
        return FileUtils.getFileName(newFolderPath);
    }

    @Override
    public List<Composition> deleteCompositionFiles(List<Composition> compositions) {
        if (compositions.equals(latestCompositionsToDelete)) {//TODO not working, in second call is 0 size list
            List<Composition> listToReturn = latestCompositionsToDelete;
            latestCompositionsToDelete = null;
            return listToReturn;
        }
        latestCompositionsToDelete = compositions;
        storageMusicProvider.deleteCompositions(mapListNotNull(
                compositions,
                Composition::getStorageId)
        );
        return latestCompositionsToDelete;
    }

    @Override
    public void deleteCompositionFile(Composition composition) {
        deleteCompositionFiles(asList(composition));
    }

    //TODO adapt
    @Override
    public long getCompositionFileSize(FullComposition composition) {
        Long storageId = composition.getStorageId();
        if (storageId == null) {
            return -1;
        }
        String filePath = storageMusicProvider.getCompositionFilePath(storageId);
        if (filePath == null) {
            return -1;
        }
        return new File(filePath).length();
    }

    @Nonnull
    private String getCompositionFilePath(long storageId) {
        String filePath = storageMusicProvider.getCompositionRelativePath(storageId);
        if (filePath == null) {
            throw new RuntimeException("composition path not found in system media store");
        }
        return filePath;
    }
}
