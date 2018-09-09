package com.github.anrimian.musicplayer.domain.business;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CompositionEvent;

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

    public static CompositionEvent currentComposition(Composition composition) {
        return new CompositionEvent(composition, 0L);
    }
}
