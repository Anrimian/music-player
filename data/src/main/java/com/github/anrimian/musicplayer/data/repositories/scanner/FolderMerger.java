package com.github.anrimian.musicplayer.data.repositories.scanner;

import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.LocalFolderNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FolderMerger {

    //deleted folders case
    public void mergeFolderTrees(FolderNode<Long> actualFolderNode,
                                 LocalFolderNode<Long> existsFoldersNode,
                                 List<Long> outFoldersToDelete,
                                 List<AddedNode> outFoldersToInsert,
                                 Set<Long> outAddedFiles) {
        for (Long file: actualFolderNode.getFiles()) {
            if (!existsFoldersNode.containsFile(file)) {
                outAddedFiles.add(file);
            }
        }
        for (LocalFolderNode<Long> existFolder : existsFoldersNode.getFolders()) {
            String key = existFolder.getKeyPath();

            FolderNode<Long> actualFolder = actualFolderNode.getFolder(key);
            if (actualFolder == null) {
                outFoldersToDelete.add(existFolder.getId());
            }
        }
        for (FolderNode<Long> actualFolder : actualFolderNode.getFolders()) {
            String key = actualFolder.getKeyPath();

            LocalFolderNode<Long> existFolder = existsFoldersNode.getFolder(key);
            if (existFolder == null) {
                Long parentId = existsFoldersNode.getId();

                AddedNode addedNode = new AddedNode(parentId, actualFolder);
                outFoldersToInsert.add(addedNode);

                List<Long> affectedFiles = getAllFilesInNode(actualFolder);
                outAddedFiles.addAll(affectedFiles);
            } else {
                mergeFolderTrees(actualFolder, existFolder, outFoldersToDelete, outFoldersToInsert, outAddedFiles);
            }
        }
    }

    private List<Long> getAllFilesInNode(FolderNode<Long> parentNode) {
        LinkedList<Long> result = new LinkedList<>(parentNode.getFiles());
        for (FolderNode<Long> node: parentNode.getFolders()) {
            result.addAll(getAllFilesInNode(node));
        }
        return result;
    }

}
