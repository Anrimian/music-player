package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.LocalFolderNode;

import java.util.List;

public class FolderMerger {

    public static final long UNKNOWN_CURRENT_FOLDER_ID = -1L;

    public void mergeFolderTrees(FolderNode<Long> actualFolderNode,
                                 LocalFolderNode<Long> currentFoldersNode,
                                 List<Long> outFoldersToDelete,
                                 List<AddedNode> outFoldersToInsert,
                                 LongSparseArray<Long> outAddedFilesFolderMap) {
        for (Long file: actualFolderNode.getFiles()) {
            if (!currentFoldersNode.containsFile(file)) {
                outAddedFilesFolderMap.put(file, currentFoldersNode.getId());
            }
        }
        for (LocalFolderNode<Long> existFolder : currentFoldersNode.getFolders()) {
            String key = existFolder.getKeyPath();

            FolderNode<Long> actualFolder = actualFolderNode.getFolder(key);
            if (actualFolder == null) {
                outFoldersToDelete.add(existFolder.getId());
            }
        }
        for (FolderNode<Long> actualFolder : actualFolderNode.getFolders()) {
            String key = actualFolder.getKeyPath();

            LocalFolderNode<Long> existFolder = currentFoldersNode.getFolder(key);
            if (existFolder == null) {
                Long parentId = currentFoldersNode.getId();

                AddedNode addedNode = new AddedNode(parentId, actualFolder);
                outFoldersToInsert.add(addedNode);
                collectAllNewFilesInNode(actualFolder, outAddedFilesFolderMap);
            } else {
                mergeFolderTrees(actualFolder, existFolder, outFoldersToDelete, outFoldersToInsert, outAddedFilesFolderMap);
            }
        }
    }

    private void collectAllNewFilesInNode(FolderNode<Long> parentNode,
                                          LongSparseArray<Long> outAddedFilesFolderMap) {
        for (Long file: parentNode.getFiles()) {
            outAddedFilesFolderMap.put(file, UNKNOWN_CURRENT_FOLDER_ID);
        }

        for (FolderNode<Long> node: parentNode.getFolders()) {
            collectAllNewFilesInNode(node, outAddedFilesFolderMap);
        }
    }

}
