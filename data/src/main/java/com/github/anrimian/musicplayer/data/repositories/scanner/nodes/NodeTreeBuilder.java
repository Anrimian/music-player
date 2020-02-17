package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;

import java.util.LinkedList;
import java.util.List;

public class NodeTreeBuilder {

    public LocalFolderNode<Long> createTreeFromIdMap(List<StorageFolder> folders,
                                                     LongSparseArray<StorageComposition> filesMap) {
        //create map <folderId, List<File>>

        LongSparseArray<List<StorageFolder>> idMap = new LongSparseArray<>();

        LocalFolderNode<Long> rootNode = new LocalFolderNode<>(null, null);
        //fill root node with files

        for (StorageFolder folder: folders) {
            Long parentId = folder.getParentId();
            if (parentId == null) {
                rootNode.addFolder(newLocalFolder(folder));
            } else {
                List<StorageFolder> childList = idMap.get(parentId);
                if (childList == null) {
                    childList = new LinkedList<>();
                    idMap.put(parentId, childList);
                }
                childList.add(folder);
            }
        }

        for (LocalFolderNode<Long> childNode: rootNode.getFolders()) {
            fillNode(childNode, idMap);
        }

        if (!idMap.isEmpty()) {
            throw new IllegalStateException("found missed folders");
        }

        return rootNode;
    }

    private void fillNode(LocalFolderNode<Long> targetNode,
                          LongSparseArray<List<StorageFolder>> parentIdMap) {
        Long id = targetNode.getId();
        if (id == null) {
            throw new IllegalStateException("try to access folder with null value");
        }
        List<StorageFolder> childList = parentIdMap.get(id);
        if (childList != null) {
            for (StorageFolder folder: childList) {
                LocalFolderNode<Long> node = newLocalFolder(folder);
                targetNode.addFolder(node);
                fillNode(node, parentIdMap);
            }
        }
        parentIdMap.remove(id);
    }

    private LocalFolderNode<Long> newLocalFolder(StorageFolder folder) {
        return new LocalFolderNode<>(folder.getName(), folder.getId());//and fill with files
    }

}
