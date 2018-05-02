package com.github.anrimian.simplemusicplayer.domain.business;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CurrentComposition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 16.04.2018.
 */
public class TestDataProvider {

    public static List<Composition> getFakeCompositions() {
        List<Composition> compositions = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Composition composition = new Composition();

            composition.setFilePath("music-" + i);
            composition.setId(i);
            compositions.add(composition);
        }
        return compositions;
    }

    public static CurrentComposition currentComposition(Composition composition) {
        return new CurrentComposition(composition, 0, 0L);
    }
}
