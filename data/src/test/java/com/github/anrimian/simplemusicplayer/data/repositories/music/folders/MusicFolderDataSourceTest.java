package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
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
        Composition composition = new Composition();
        composition.setFilePath("simple path");
        composition.setId(1L);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(singletonMap(1L, composition));

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(list -> {
                    assertEquals(1, list.size());
                    assertEquals(composition, ((CompositionNode) list.get(0)).getComposition());
                    return true;
                });
    }

    @Test
    public void getCompositionsInTreeTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = new Composition();
        compositionOne.setFilePath("root/music/one.dd");
        compositionOne.setId(1L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/two.dd");
        compositionTwo.setId(2L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = new Composition();
        compositionThree.setFilePath("root/music/favorite/three.dd");
        compositionThree.setId(3L);
        compositions.put(3L, compositionThree);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(list -> {
                    assertEquals(3, list.size());
                    assertEquals(compositionOne, ((CompositionNode) list.get(0)).getComposition());
                    assertEquals(compositionTwo, ((CompositionNode) list.get(1)).getComposition());

                    FolderNode folderNode = (FolderNode) list.get(2);
                    assertEquals("root/music/favorite", folderNode.getFullPath());
                    assertEquals(1, folderNode.getCompositionsCount());
                    return true;
                });

        musicFolderDataSource.getMusicInPath("root/music/favorite")
                .test()
                .assertValue(list -> {
                    assertEquals(1, list.size());
                    assertEquals(compositionThree, ((CompositionNode) list.get(0)).getComposition());
                    return true;
                });

//        musicFolderDataSource.getMusicInPath(null)
//                .test()
//                .assertValue(list -> {
//                    FolderNode folderNode = (FolderNode) list.get(0);
//                    assertEquals("root", folderNode.getFullPath());
//                    assertEquals(3, folderNode.getCompositionsCount());
//                    return true;
//                });
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