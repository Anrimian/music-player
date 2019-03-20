package com.github.anrimian.musicplayer.data.repositories.music.comparators.folder;

import com.github.anrimian.musicplayer.data.repositories.music.comparators.DescComparator;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;

import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class AlphabeticalDescFileComparatorTest {

    @Test
    public void applyOrder() {
        Composition compositionOne = fakeComposition(1, "A");
        Composition compositionTwo = fakeComposition(2, "B");

        List<FileSource> fileSources = asList(
                new MusicFileSource(compositionOne),
                new FolderFileSource("basic", 1, new Date(0), new Date(0)),
                new MusicFileSource(compositionTwo),
                new FolderFileSource("aby", 1, new Date(0), new Date(0))
        );

        Collections.sort(fileSources, new FolderComparator(
                        new DescComparator<>(
                                new AlphabeticalFileComparator()
                        )
                )
        );


        assertEquals("basic", ((FolderFileSource) fileSources.get(0)).getFullPath());
        assertEquals("aby", ((FolderFileSource) fileSources.get(1)).getFullPath());
        assertEquals(compositionTwo, ((MusicFileSource) fileSources.get(2)).getComposition());
        assertEquals(compositionOne, ((MusicFileSource) fileSources.get(3)).getComposition());
    }
}