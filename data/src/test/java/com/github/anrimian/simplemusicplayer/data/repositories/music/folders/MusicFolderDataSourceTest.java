package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MusicFolderDataSourceTest {

    private StorageMusicDataSource storageMusicDataSource = mock(StorageMusicDataSource.class);

    private MusicFolderDataSource musicFolderDataSource = new MusicFolderDataSource(
            storageMusicDataSource
    );

    @Before
    public void setUp() {
        when(storageMusicDataSource.getCompositionsMap()).thenReturn(getFakeCompositionsMap());
    }

    @Test
    public void getSingleCompositionTest() {
        when(storageMusicDataSource.getCompositionsMap()).thenReturn(getFakeCompositionsMap());

        musicFolderDataSource.getMusicInPath(null)
                .subscribe(list -> {
                    assertEquals(1, list.size());
//                    assertEquals(getFakeCompositionsMap().get(1L), list.get(0));
                });
    }

    private static Map<Long, Composition> getFakeCompositionsMap() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition composition = new Composition();

        composition.setFilePath("music-" + 1L);
        composition.setId(1L);
        compositions.put(1L, composition);
        return compositions;
    }

}