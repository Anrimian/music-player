package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;

import java.util.LinkedList;
import java.util.List;

public class NodeTreeBuilder {

    public LocalFolderNode<Long> createTreeFromIdMap(List<StorageFolder> folders,
                                                     LongSparseArray<StorageComposition> filesMap) {
        LongSparseArray<List<Long>> folderFilesMap = buildFolderIdMap(filesMap);

        LongSparseArray<List<StorageFolder>> folderParentIdMap = new LongSparseArray<>();

        LocalFolderNode<Long> rootNode = new LocalFolderNode<>(null, null);
        List<Long> files = folderFilesMap.get(0);
        if (files != null) {
            rootNode.addFiles(files);
        }

        for (StorageFolder folder: folders) {
            Long parentId = folder.getParentId();
            if (parentId == null) {
                rootNode.addFolder(createLocalFolderWithFiles(folder, folderFilesMap));
            } else {
                List<StorageFolder> childList = folderParentIdMap.get(parentId);
                if (childList == null) {
                    childList = new LinkedList<>();
                    folderParentIdMap.put(parentId, childList);
                }
                childList.add(folder);
            }
        }

        for (LocalFolderNode<Long> childNode: rootNode.getFolders()) {
            fillNode(childNode, folderParentIdMap, folderFilesMap);
        }

        if (!folderParentIdMap.isEmpty()) {
            throw new IllegalStateException("found missed folders");
        }

        return rootNode;
    }

    private LongSparseArray<List<Long>> buildFolderIdMap(LongSparseArray<StorageComposition> filesMap) {
        LongSparseArray<List<Long>> folderIdMap = new LongSparseArray<>();

        for(int i = 0, size = filesMap.size(); i < size; i++) {
            StorageComposition file = filesMap.valueAt(i);
            Long folderId = file.getFolderId();
            if (folderId == null) {
                folderId = 0L;
            }
            List<Long> childList = folderIdMap.get(folderId);
            if (childList == null) {
                childList = new LinkedList<>();
                folderIdMap.put(folderId, childList);
            }
            childList.add(file.getStorageId());
        }
        return folderIdMap;
    }

    private void fillNode(LocalFolderNode<Long> targetNode,
                          LongSparseArray<List<StorageFolder>> folderParentIdMap,
                          LongSparseArray<List<Long>> folderFilesMap) {
        Long id = targetNode.getId();
        if (id == null) {
            throw new IllegalStateException("try to access folder with id == null");
        }
        List<StorageFolder> childList = folderParentIdMap.get(id);
        if (childList != null) {
            for (StorageFolder folder: childList) {
                LocalFolderNode<Long> node = createLocalFolderWithFiles(folder, folderFilesMap);
                targetNode.addFolder(node);
                fillNode(node, folderParentIdMap, folderFilesMap);
            }
        }
        folderParentIdMap.remove(id);
    }

    private LocalFolderNode<Long> createLocalFolderWithFiles(StorageFolder folder,
                                                             LongSparseArray<List<Long>> folderFilesMap) {
        long id = folder.getId();
        LocalFolderNode<Long> node = new LocalFolderNode<>(folder.getName(), id);
        List<Long> files = folderFilesMap.get(id);
        if (files != null) {
            node.addFiles(files);
        }
        return node;
    }

}
