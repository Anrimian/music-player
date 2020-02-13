package com.github.anrimian.musicplayer.data.repositories.scanner;

import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.Node;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FolderMergerTest {

    private final FolderMerger folderMerger = new FolderMerger();

    @Test
    public void mergeFolderTreesTest() {
        FolderNode<Long> actualFolderTree = new FolderNode<>(null);
        FolderNode<Long> folder1 = new FolderNode<>("folder 1");
        folder1.addFile(1L);

        FolderNode<Long> folder2 = new FolderNode<>("folder 2");
        folder2.addFile(2L);
        folder1.addFolder(folder2);

        actualFolderTree.addFolder(folder1);
        actualFolderTree.addFile(3L);

        Node<String, StorageFolder> existsFolders = new Node<>(null, null);

        List<Long> foldersToDelete = new LinkedList<>();
        List<AddedNode> foldersToInsert = new LinkedList<>();
        Set<Long> movedFiles = new LinkedHashSet<>();
        folderMerger.mergeFolderTrees(actualFolderTree, existsFolders, foldersToDelete, foldersToInsert, movedFiles);

        AddedNode addedNode = foldersToInsert.get(0);
        FolderNode<Long> testFolder1 = addedNode.getNode();
        assertEquals(folder1, testFolder1);
        assert testFolder1.getFiles().contains(1L);

        FolderNode<Long> testFolder2 = testFolder1.getFolder("folder 2");
        assertNotNull(testFolder2);
        assertEquals(folder2, testFolder2);
        assert testFolder2.getFiles().contains(2L);

        assert movedFiles.contains(1L);
        assert movedFiles.contains(2L);
        assert movedFiles.contains(3L);
    }
}