package com.github.anrimian.musicplayer.data.storage.files;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapListNotNull;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;

import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@RequiresApi(api = Build.VERSION_CODES.R)
public class StorageFilesDataSourceApi30 implements StorageFilesDataSource {

    private final StorageMusicProvider storageMusicProvider;

    @Nullable
    private List<Composition> latestCompositionsToDelete;
    @Nullable
    private Object tokenForDelete;

    public StorageFilesDataSourceApi30(StorageMusicProvider storageMusicProvider) {
        this.storageMusicProvider = storageMusicProvider;
    }

    //previous folder will be not deleted, but I didn't find solution for that
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

    //internal folders will stay, but I didn't find solution for that
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

    //previous folder will be not deleted, but I didn't find solution for that
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

    //empty root folder will be not deleted, but I didn't find solution for that
    @Override
    public List<Composition> deleteCompositionFiles(List<Composition> compositions,
                                                    Object tokenForDelete) {
        // From android 11 delete actions are started twice.
        // Moreover, files are deleting by system after dialog confirm.
        // So, on second attempt composition list can be null when it is received from folder by db query
        // So token for delete represent folder object that is not changed on second attempt

        if (tokenForDelete.equals(this.tokenForDelete)) {
            List<Composition> listToReturn = latestCompositionsToDelete;
            latestCompositionsToDelete = null;
            this.tokenForDelete = null;
            return listToReturn;
        }
        latestCompositionsToDelete = compositions;
        this.tokenForDelete = tokenForDelete;
        storageMusicProvider.deleteCompositions(mapListNotNull(
                compositions,
                Composition::getStorageId)
        );
        // we are always expecting exception from deleteCompositions() here. But
        // deleteCompositions() can be executed successfully on first attempt when we have created these files
        // in this case clean token and compositions
        var result = latestCompositionsToDelete;
        latestCompositionsToDelete = null;
        this.tokenForDelete = null;
        return result;
    }

    @Override
    public void deleteCompositionFile(Composition composition) {
        deleteCompositionFiles(asList(composition), composition);
    }

    @Override
    public void clearDeleteData() {
        latestCompositionsToDelete = null;
        tokenForDelete = null;
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
