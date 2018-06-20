package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.ADDED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.MODIFY;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MusicFolderDataSourceTest {

    private StorageMusicDataSource storageMusicDataSource = mock(StorageMusicDataSource.class);
    private PublishSubject<Change<List<Composition>>> changeSubject = PublishSubject.create();

    private MusicFolderDataSource musicFolderDataSource = new MusicFolderDataSource(
            storageMusicDataSource
    );

    @Before
    public void setUp() {
        when(storageMusicDataSource.getCompositionsMap()).thenReturn(getFakeCompositionsMap());
        when(storageMusicDataSource.getChangeObservable()).thenReturn(changeSubject);
    }

    @Test
    public void getSingleCompositionTest() {
        Composition composition = new Composition();
        composition.setFilePath("simple path");
        composition.setId(1L);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(singletonMap(1L, composition));

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    List<NodeData> list = folder.getFiles();
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
                .assertValue(folder -> {
                    List<NodeData> list = folder.getFiles();
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
                .assertValue(folder -> {
                    List<NodeData> list = folder.getFiles();
                    assertEquals(1, list.size());
                    assertEquals(compositionThree, ((CompositionNode) list.get(0)).getComposition());
                    return true;
                });
    }

    @Test
    public void newCompositionChangeTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = new Composition();
        compositionOne.setFilePath("root/music/one.dd");
        compositionOne.setId(1L);
        compositions.put(1L, compositionOne);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getMusicInPath(null).blockingGet();
        TestObserver<Change<NodeData>> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<Change<List<NodeData>>> childChangeObserver = observerFolder.getChildChangeObservable().test();

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/two.dd");
        compositionTwo.setId(2L);
        changeSubject.onNext(new Change<>(ADDED, singletonList(compositionTwo)));

        childChangeObserver.assertValue(change -> {
            assertEquals(ADDED, change.getChangeType());
            assertEquals(compositionTwo, ((CompositionNode) change.getData().get(0)).getComposition());
            return true;
        });

        selfChangeObserver.assertValue(change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderNode folderNode = (FolderNode) change.getData();
            assertEquals(2, folderNode.getCompositionsCount());
            return true;
        });

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    List<NodeData> list = folder.getFiles();
                    assertEquals(2, list.size());
                    assertEquals(compositionOne, ((CompositionNode) list.get(0)).getComposition());
                    assertEquals(compositionTwo, ((CompositionNode) list.get(1)).getComposition());
                    return true;
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