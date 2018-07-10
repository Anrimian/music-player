package com.github.anrimian.simplemusicplayer.data;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CompositionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Map<Long, Composition> getFakeCompositionsMap() {
        Map<Long, Composition> compositions = new HashMap<>();
        for (long i = 0; i < 100000; i++) {
            Composition composition = new Composition();

            composition.setFilePath("music-" + i);
            composition.setId(i);
            compositions.put(i, composition);
        }
        return compositions;
    }

    public static CompositionEvent currentComposition(Composition composition) {
        return new CompositionEvent(composition, 0, 0L);
    }

    public static Composition fakeComposition(long id, String name) {
        Composition composition = new Composition();
        composition.setId(id);
        composition.setDisplayName(name);
        return composition;
    }
}
