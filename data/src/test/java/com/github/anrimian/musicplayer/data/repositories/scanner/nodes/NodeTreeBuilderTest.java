package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NodeTreeBuilderTest {

    private final NodeTreeBuilder nodeTreeBuilder = new NodeTreeBuilder();

    @Test
    public void createTreeFromIdMap() {
        List<StorageFolder> folderEntities = new LinkedList<>();
        folderEntities.add(new StorageFolder(1, null, "1"));
        folderEntities.add(new StorageFolder(2, null, "2"));
        folderEntities.add(new StorageFolder(3, null, "3"));
        folderEntities.add(new StorageFolder(4, 1L, "4"));
        folderEntities.add(new StorageFolder(5, 4L, "5"));
        folderEntities.add(new StorageFolder(6, 1L, "6"));
        LocalFolderNode<Long> rootNode = nodeTreeBuilder.createTreeFromIdMap(folderEntities, new LongSparseArray<>());
        assertEquals(3, rootNode.getFolders().size());
        LocalFolderNode<Long> nodeThree = rootNode.getFolder("1");
        assertNotNull(nodeThree);
        LocalFolderNode<Long> nodeFour = nodeThree.getFolder("4");
        assertNotNull(nodeFour);
        LocalFolderNode<Long> nodeFive = nodeFour.getFolder("5");
        assertNotNull(nodeFive);
        LocalFolderNode<Long> nodeSix = nodeThree.getFolder("6");
        assertNotNull(nodeSix);
    }
}