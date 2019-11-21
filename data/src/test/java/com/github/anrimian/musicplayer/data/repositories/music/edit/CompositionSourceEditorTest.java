package com.github.anrimian.musicplayer.data.repositories.music.edit;

import com.github.anrimian.musicplayer.data.utils.files.ResourceFile;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CompositionSourceEditorTest {

    @Rule
    public ResourceFile res = new ResourceFile("/VersuS - Warriors (Instrumental Kizomba).mp3");

    private CompositionSourceEditor sourceEditor = new CompositionSourceEditor();

    @Test
    public void testEditor() throws IOException {
        String filePath = res.getFile().getPath();
        System.out.println("title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());
        System.out.println("author: " + sourceEditor.getCompositionAuthor(filePath).blockingGet());
        System.out.println("album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());
    }

    @Test
    public void changeTitleTest() throws IOException {
        String filePath = res.getFile().getPath();
        System.out.println("title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());

        String testTitle = "Test title";
        sourceEditor.setCompositionTitle(filePath, testTitle).subscribe();
        String newTitle = sourceEditor.getCompositionTitle(filePath).blockingGet();
        System.out.println("new title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());
        assertEquals(testTitle, newTitle);
    }

    @Test
    public void changeAlbumTest() throws IOException {
        String filePath = res.getFile().getPath();
        System.out.println("album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());

        String testAlbum = "Test album";
        sourceEditor.setCompositionAlbum(filePath, testAlbum).subscribe();
        String newTitle = sourceEditor.getCompositionAlbum(filePath).blockingGet();
        System.out.println("new album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());
        assertEquals(testAlbum, newTitle);
    }
}