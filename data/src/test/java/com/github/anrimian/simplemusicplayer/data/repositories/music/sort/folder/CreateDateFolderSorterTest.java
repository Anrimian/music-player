package com.github.anrimian.simplemusicplayer.data.repositories.music.sort.folder;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;

import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.fakeComposition;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getTestFolder;
import static org.junit.Assert.assertEquals;

public class CreateDateFolderSorterTest {

    private CreateDateFolderSorter folderSorter = new CreateDateFolderSorter();

    @Test
    public void applyOrder() {
        Composition compositionOne = fakeComposition(1, 100L);
        Composition compositionTwo = fakeComposition(2, 200L);

        Folder folder = getTestFolder(
                new MusicFileSource(compositionOne),
                new FolderFileSource("basic", 1, new Date(101L), new Date(0)),
                new MusicFileSource(compositionTwo),
                new FolderFileSource("aby", 1, new Date(201L), new Date(0))
        );

        folderSorter.applyOrder(folder);

//        List<FileSource> files = folder.getFiles();
//
//        assertEquals("basic", ((FolderFileSource) files.get(0)).getFullPath());
//        assertEquals("aby", ((FolderFileSource) files.get(1)).getFullPath());
//        assertEquals(compositionOne, ((MusicFileSource) files.get(2)).getComposition());
//        assertEquals(compositionTwo, ((MusicFileSource) files.get(3)).getComposition());
    }
}