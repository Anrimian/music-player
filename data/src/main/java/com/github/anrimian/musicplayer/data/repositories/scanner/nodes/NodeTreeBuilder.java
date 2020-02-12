package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;

import java.util.LinkedList;
import java.util.List;

public class NodeTreeBuilder {

    public Node<String, StorageFolder> createTreeFromIdMap(List<StorageFolder> folders) {
        LongSparseArray<List<StorageFolder>> idMap = new LongSparseArray<>();

        Node<String, StorageFolder> rootNode = new Node<>(null, null);

        for (StorageFolder entity: folders) {
            Long parentId = entity.getParentId();
            if (parentId == null) {
                rootNode.addNode(new Node<>(entity.getName(), entity));
            } else {
                List<StorageFolder> childList = idMap.get(parentId);
                if (childList == null) {
                    childList = new LinkedList<>();
                    idMap.put(parentId, childList);
                }
                childList.add(entity);
            }
        }

        for (Node<String, StorageFolder> childNode: rootNode.getNodes()) {
            fillNode(childNode, idMap);
        }

        if (!idMap.isEmpty()) {
            throw new IllegalStateException("found missed folders");
        }

        return rootNode;
    }

    private void fillNode(Node<String, StorageFolder> targetNode,
                          LongSparseArray<List<StorageFolder>> parentIdMap) {
        StorageFolder folderEntity = targetNode.getData();
        long id = folderEntity.getId();
        List<StorageFolder> childList = parentIdMap.get(id);
        if (childList != null) {
            for (StorageFolder entity: childList) {
                Node<String, StorageFolder> node = new Node<>(entity.getName(), entity);
                targetNode.addNode(node);
                fillNode(node, parentIdMap);
            }
        }
        parentIdMap.remove(id);
    }
}
