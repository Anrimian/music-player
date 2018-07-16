package com.github.anrimian.simplemusicplayer.data.repositories.music.comparators.composition;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.fakeComposition;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

public class AlphabeticalCompositionComparatorTest {

    @Test
    public void applyOrder() {
        Composition compositionOne = fakeComposition(1, "A");
        Composition compositionTwo = fakeComposition(2, "B");
        Composition compositionThree = fakeComposition(3, "C");

        List<Composition> compositions = asList(compositionOne, compositionTwo, compositionThree);

        Collections.sort(compositions, new AlphabeticalCompositionComparator());

        assertEquals(compositionOne, compositions.get(0));
        assertEquals(compositionTwo, compositions.get(1));
        assertEquals(compositionThree, compositions.get(2));
    }
}