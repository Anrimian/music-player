package com.github.anrimian.musicplayer.data.repositories.music.edit;

import com.github.anrimian.musicplayer.data.utils.files.ResourceFile;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

public class CompositionSourceEditorTest {

    @Rule
    public ResourceFile res = new ResourceFile("/VersuS - Warriors (Instrumental Kizomba).mp3");

    private CompositionSourceEditor sourceEditor = new CompositionSourceEditor();

    @Test
    public void testEditor() throws IOException {
        System.out.println("title: " + sourceEditor.getCompositionAuthor(res.getFile().getPath()).blockingGet());
        System.out.println("author: " + sourceEditor.getCompositionAuthor(res.getFile().getPath()).blockingGet());
    }
}