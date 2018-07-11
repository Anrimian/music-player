package com.github.anrimian.simplemusicplayer.data.repositories.music.sort;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;

import org.junit.Test;

import java.util.List;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.fakeComposition;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getTestFolder;
import static org.junit.Assert.*;

public class AlphabeticalFolderSorterTest {

    private AlphabeticalFolderSorter folderSorter = new AlphabeticalFolderSorter();

    @Test
    public void applyOrder() {
        Composition compositionOne = fakeComposition(1, "A");
        Composition compositionTwo = fakeComposition(2, "B");

        Folder folder = getTestFolder(
                new MusicFileSource(compositionOne),
                new FolderFileSource("basic", 1),
                new MusicFileSource(compositionTwo),
                new FolderFileSource("aby", 1)
        );

        folderSorter.applyOrder(folder);

        List<FileSource> files = folder.getFiles();

        assertEquals("aby", ((FolderFileSource) files.get(0)).getPath());
        assertEquals("basic", ((FolderFileSource) files.get(1)).getPath());
        assertEquals(compositionOne, ((MusicFileSource) files.get(2)).getComposition());
        assertEquals(compositionTwo, ((MusicFileSource) files.get(3)).getComposition());
    }
}