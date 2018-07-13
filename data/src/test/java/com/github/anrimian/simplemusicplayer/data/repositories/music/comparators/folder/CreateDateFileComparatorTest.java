package com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.folder;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;

import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.fakeComposition;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class CreateDateFileComparatorTest {

    @Test
    public void applyOrder() {
        Composition compositionOne = fakeComposition(1, 100L);
        Composition compositionTwo = fakeComposition(2, 200L);

        List<FileSource> fileSources = asList(
                new MusicFileSource(compositionOne),
                new FolderFileSource("basic", 1, new Date(101L), new Date(0)),
                new MusicFileSource(compositionTwo),
                new FolderFileSource("aby", 1, new Date(201L), new Date(0))
        );

        Collections.sort(fileSources, new CreateDateFileComparator());

        assertEquals("basic", ((FolderFileSource) fileSources.get(0)).getFullPath());
        assertEquals("aby", ((FolderFileSource) fileSources.get(1)).getFullPath());
        assertEquals(compositionOne, ((MusicFileSource) fileSources.get(2)).getComposition());
        assertEquals(compositionTwo, ((MusicFileSource) fileSources.get(3)).getComposition());
    }
}