package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import org.junit.Test;

import io.reactivex.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("ConstantConditions")
public class FolderTreeBuilderTest {

    private final FolderTreeBuilder<String, String> folderTreeBuilder = new FolderTreeBuilder<>(
            s -> s,
            s -> s
    );

    @Test
    public void createFileTree() {
        Node<String, String> root = folderTreeBuilder.createFileTree(Observable.fromArray(
                "music",
                "music/new",
                ""
        ));
        assertEquals("", root.getChild(null).getData());

        Node<String, String> musicFolder = root.getChild("music");
        assertNotNull(musicFolder);
        assertNotNull(musicFolder.getChild("new"));
        assertEquals("music/new", musicFolder.getChild("new").getChild(null).getData());
    }
}