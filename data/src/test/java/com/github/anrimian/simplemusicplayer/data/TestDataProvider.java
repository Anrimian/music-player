package com.github.anrimian.simplemusicplayer.data;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CompositionEvent;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;

import static java.util.Arrays.asList;

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
        return new CompositionEvent(composition, 0L);
    }

    public static Composition fakeComposition(long id, String filePath, long createDate) {
        Composition composition = new Composition();
        composition.setId(id);
        composition.setFilePath(filePath);
        composition.setDateAdded(new Date(createDate));
        return composition;
    }

    public static Composition fakeComposition(long id, String name) {
        Composition composition = new Composition();
        composition.setId(id);
        composition.setDisplayName(name);
        composition.setFilePath(name);
        return composition;
    }

    public static Composition fakeComposition(long id, long createDate) {
        Composition composition = new Composition();
        composition.setId(id);
        composition.setFilePath(String.valueOf(id));
        composition.setDateAdded(new Date(createDate * 1000L));
        return composition;
    }

    public static Single<Folder> getTestFolderSingle(FileSource... fileSources) {
        return Single.just(getTestFolder(fileSources));
    }

    public static Folder getTestFolder(FileSource... fileSources) {
        return new Folder(Observable.just(asList(fileSources)), Observable.never());
    }
}
