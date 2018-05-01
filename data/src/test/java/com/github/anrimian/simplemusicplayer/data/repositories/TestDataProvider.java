package com.github.anrimian.simplemusicplayer.data.repositories;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

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
}
