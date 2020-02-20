package com.github.anrimian.musicplayer.data.repositories.library.comparators.folder;

import com.github.anrimian.musicplayer.data.repositories.library.comparators.DescComparator;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;

import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static utils.TestDataProvider.fakeComposition;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class CreateDateDescFileComparatorTest {

    @Test
    public void applyOrder() {
        Composition compositionOne = fakeComposition(1, 100L);
        Composition compositionTwo = fakeComposition(2, 200L);

        List<FileSource> fileSources = asList(
                new MusicFileSource(compositionOne),
                new FolderFileSource("basic", 1, new Date(101L), new Date(0)),
                new MusicFileSource(compositionTwo),
                new FolderFileSource("aby", 1, new Date(201L), new Date(0)),
                new FolderFileSource("nullabla", 1, null, null)
        );

        Collections.sort(fileSources, new FolderComparator(
                        new DescComparator<>(
                                new CreateDateFileComparator()
                        )
                )
        );


        assertEquals("aby", ((FolderFileSource) fileSources.get(0)).getPath());
        assertEquals("basic", ((FolderFileSource) fileSources.get(1)).getPath());
        assertEquals("nullabla", ((FolderFileSource) fileSources.get(2)).getPath());
        assertEquals(compositionTwo, ((MusicFileSource) fileSources.get(3)).getComposition());
        assertEquals(compositionOne, ((MusicFileSource) fileSources.get(4)).getComposition());
    }
}