package com.github.anrimian.musicplayer.data.repositories.scanner.folders;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.reactivex.rxjava3.core.Observable;


public class FolderTreeBuilderTest {

    private final FolderTreeBuilder<String, String> folderTreeBuilder = new FolderTreeBuilder<>(
            s -> s,
            s -> s
    );

    @Test
    public void createFileTreeTest() {
        FolderNode<String> root = folderTreeBuilder.createFileTree(Observable.fromArray(
                "music",
                "music/new",
                ""
        ));
        assert root.getFiles().contains("");

        FolderNode<String> musicFolder = root.getFolder("music");
        assertNotNull(musicFolder);
        assert musicFolder.getFiles().contains("music");

        FolderNode<String> newFolder = musicFolder.getFolder("new");
        assertNotNull(newFolder);
        assert newFolder.getFiles().contains("music/new");
    }
}