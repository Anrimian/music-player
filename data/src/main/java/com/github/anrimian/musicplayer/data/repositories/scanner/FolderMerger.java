package com.github.anrimian.musicplayer.data.repositories.scanner;

import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.Node;

import java.util.List;

public class FolderMerger {

    public void mergeFolderTrees(FolderNode<Long> actualFolderNode,
                                  Node<String, StorageFolder> existsFoldersNode,
                                  List<Long> foldersToDelete,
                                  List<AddedNode> foldersToInsert) {
        for (Node<String, StorageFolder> existFolder : existsFoldersNode.getNodes()) {
            String key = existFolder.getKey();
            if (key == null) {
                continue;//not a folder
            }

            FolderNode<Long> actualFolder = actualFolderNode.getFolder(key);
            if (actualFolder == null) {
                foldersToDelete.add(existFolder.getData().getId());
            }
        }
        for (FolderNode<Long> actualFolder : actualFolderNode.getFolders()) {
            String key = actualFolder.getKeyPath();

            Node<String, StorageFolder> existFolder = existsFoldersNode.getChild(key);
            if (existFolder == null) {
                Long parentId = null;
                StorageFolder entity = existsFoldersNode.getData();
                if (entity != null) {
                    parentId = entity.getId();
                }

                AddedNode addedNode = new AddedNode(parentId, actualFolder);
                foldersToInsert.add(addedNode);//we add unnecessary child folders, hm
            } else {
                mergeFolderTrees(actualFolder, existsFoldersNode, foldersToDelete, foldersToInsert);
            }
        }
    }


}
