package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

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
        Node<String, StorageFolder> rootNode = nodeTreeBuilder.createTreeFromIdMap(folderEntities);
        assertEquals(3, rootNode.getNodes().size());
        Node<String, StorageFolder> nodeThree = rootNode.getChild("1");
        assertNotNull(nodeThree);
        Node<String, StorageFolder> nodeFour = nodeThree.getChild("4");
        assertNotNull(nodeFour);
        Node<String, StorageFolder> nodeFive = nodeFour.getChild("5");
        assertNotNull(nodeFive);
        Node<String, StorageFolder> nodeSix = nodeThree.getChild("6");
        assertNotNull(nodeSix);
    }
}