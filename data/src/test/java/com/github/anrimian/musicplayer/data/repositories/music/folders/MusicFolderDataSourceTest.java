package com.github.anrimian.musicplayer.data.repositories.music.folders;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;
import com.github.anrimian.musicplayer.domain.utils.changes.ModifiedData;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MusicFolderDataSourceTest {

    private StorageMusicDataSource storageMusicDataSource = mock(StorageMusicDataSource.class);
    private PublishSubject<Change<Composition>> changeSubject = PublishSubject.create();

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

        getSourceObservable(null)
                .test()
                .assertValue(list -> {
                    assertEquals(1, list.size());
                    assertEquals(composition, ((MusicFileSource) list.get(0)).getComposition());
                    return true;
                });
    }

    @Test
    public void getCompositionsInTreeTest() {
        Map<Long, Composition> compositions = new HashMap<>();

        Composition compositionOne = fakeComposition(1L, "root/music/one.dd", 1L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/two.dd", 2L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/favorite/three.dd", 3L);
        compositions.put(3L, compositionThree);

        Composition compositionFour = fakeComposition(4L, "root/music/favorite/four.dd", 4L);
        compositions.put(4L, compositionFour);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        getSourceObservable(null)
                .test()
                .assertValue(list -> {
                    assertEquals(3, list.size());
                    assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
                    assertEquals(compositionTwo, ((MusicFileSource) list.get(1)).getComposition());

                    FolderFileSource folderNode = (FolderFileSource) list.get(2);
                    assertEquals("root/music/favorite", folderNode.getFullPath());
                    assertEquals(2, folderNode.getFilesCount());
                    assertEquals(4L, folderNode.getLatestCreateDate().getTime());
                    assertEquals(3L, folderNode.getEarliestCreateDate().getTime());
                    return true;
                });

        getSourceObservable("root/music/favorite")
                .test()
                .assertValue(list -> {
                    assertEquals(2, list.size());
                    assertEquals(compositionThree, ((MusicFileSource) list.get(0)).getComposition());
                    assertEquals(compositionFour, ((MusicFileSource) list.get(1)).getComposition());
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

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Composition compositionTwo = fakeComposition(2L, "root/music/two.dd");
        Composition compositionThree = fakeComposition(3L, "root/music/three.dd");
        changeSubject.onNext(new Change.AddChange<>(asList(compositionTwo, compositionThree)));

        childrenObserver.assertValueAt(1, list -> {
            assertEquals(3, list.size());
            assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
            assertEquals(compositionTwo, ((MusicFileSource) list.get(1)).getComposition());
            assertEquals(compositionThree, ((MusicFileSource) list.get(2)).getComposition());
            return true;
        });

        selfChangeObserver.assertValue(data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(3, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void newCompositionChangeInChildNodeTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd", 4L);
        compositions.put(1L, compositionOne);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Composition compositionTwo = fakeComposition(2L, "root/music/basic/two.dd", 4L);
        changeSubject.onNext(new Change.AddChange<>(singletonList(compositionTwo)));

        childrenObserver.assertValueAt(1, list -> {
            assertEquals(2, list.size());
            assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());

            FolderFileSource folderNode = ((FolderFileSource) list.get(1));
            assertEquals("root/music/basic", folderNode.getFullPath());
            assertEquals(0, folderNode.getFilesCount());
            assertNull(folderNode.getEarliestCreateDate());
            assertNull(folderNode.getLatestCreateDate());
            return true;
        });

        childrenObserver.assertValueAt(2, list -> {
            assertEquals(2, list.size());
            assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());

            FolderFileSource folderNode = ((FolderFileSource) list.get(1));
            assertEquals("root/music/basic", folderNode.getFullPath());
            assertEquals(1, folderNode.getFilesCount());
            assertEquals(4L, folderNode.getEarliestCreateDate().getTime());
            assertEquals(4L, folderNode.getLatestCreateDate().getTime());
            return true;
        });

        selfChangeObserver.assertValueAt(1, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(2, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void deleteCompositionTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/two.dd", 4L);
        compositions.put(2L, compositionTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        changeSubject.onNext(new Change.DeleteChange<>(singletonList(compositionTwo)));

        childrenObserver.assertValueAt(1, list -> {
            assertEquals(1, list.size());
            assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
            return true;
        });

        selfChangeObserver.assertValue(data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(1, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void deleteLastCompositionInChildNodeTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd", 2L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/basic/two.dd", 3L);
        compositions.put(2L, compositionTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Folder basicFolder = musicFolderDataSource.getCompositionsInPath("root/music/basic").blockingGet();
        TestObserver<FileSource> basicFolderSelfObserver = basicFolder.getSelfChangeObservable().test();
        TestObserver<Object> basicFolderDeleteSelfObserver = basicFolder.getSelfDeleteObservable().test();
        TestObserver<List<FileSource>> basicFolderChildObserver = basicFolder.getFilesObservable().test();

        changeSubject.onNext(new Change.DeleteChange<>(singletonList(compositionTwo)));

        basicFolderChildObserver.assertValueAt(1, list -> {
            assertEquals(0, list.size());
            return true;
        });

        basicFolderSelfObserver.assertValueAt(0, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals("root/music/basic", folderNode.getFullPath());
            assertEquals(0, folderNode.getFilesCount());
            return true;
        });

        basicFolderDeleteSelfObserver.assertValueAt(0, o -> true);

        childrenObserver.assertValueAt(1, list -> {
            assertEquals(2, list.size());
            FolderFileSource folderNode = (FolderFileSource) list.get(1);
            assertEquals(0, folderNode.getFilesCount());//date values?
            assertNotNull(folderNode.getEarliestCreateDate());
            assertNotNull(folderNode.getLatestCreateDate());
            return true;
        });

        childrenObserver.assertValueAt(2, list -> {
            assertEquals(1, list.size());
            assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
            return true;
        });

        selfChangeObserver.assertValueAt(0, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(1, folderNode.getFilesCount());
            assertEquals(3L, folderNode.getLatestCreateDate().getTime());
            assertEquals(2L, folderNode.getEarliestCreateDate().getTime());
            return true;
        });

        selfChangeObserver.assertValueAt(1,data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(1, folderNode.getFilesCount());
            assertEquals(2L, folderNode.getLatestCreateDate().getTime());
            assertEquals(2L, folderNode.getEarliestCreateDate().getTime());
            return true;
        });
    }

    @Test
    public void deleteCompositionInChildNodeTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/basic/two.dd", 3L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/basic/three.dd", 5L);
        compositions.put(3L, compositionThree);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Folder basicFolder = musicFolderDataSource.getCompositionsInPath("root/music/basic").blockingGet();
        TestObserver<FileSource> basicFolderSelfObserver = basicFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> basicFolderChildObserver = basicFolder.getFilesObservable().test();

        changeSubject.onNext(new Change.DeleteChange<>(singletonList(compositionTwo)));

        basicFolderChildObserver.assertValueAt(1, list -> {
            assertEquals(1, list.size());
            return true;
        });

        basicFolderSelfObserver.assertValueAt(0, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals("root/music/basic", folderNode.getFullPath());
            assertEquals(1, folderNode.getFilesCount());
            assertEquals(5L, folderNode.getEarliestCreateDate().getTime());
            assertEquals(5L, folderNode.getLatestCreateDate().getTime());
            return true;
        });

        childrenObserver.assertValueAt(1, list -> {
            assertEquals(2, list.size());
            FolderFileSource folderNode = (FolderFileSource) list.get(1);
            assertEquals(1, folderNode.getFilesCount());
            return true;
        });

        selfChangeObserver.assertValue(data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(2, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void modifyFileTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = new Composition();
        compositionOne.setFilePath("root/music/one.dd");
        compositionOne.setId(1L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/two.dd");
        compositionTwo.setSize(2);
        compositionTwo.setId(2L);
        compositions.put(2L, compositionTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Composition compositionTwoChanged = new Composition();
        compositionTwoChanged.setFilePath("root/music/two.dd");
        compositionTwoChanged.setSize(4);
        compositionTwoChanged.setId(2L);

        changeSubject.onNext(new Change.ModifyChange<>(asList(new ModifiedData<>(compositionTwo, compositionTwoChanged))));

        childrenObserver.assertValueAt(1, list -> {
            assertEquals(2, list.size());

            MusicFileSource MusicFileSource = ((MusicFileSource) list.get(1));
            assertEquals(4, MusicFileSource.getComposition().getSize());
            return true;
        });
    }

    @Test
    public void modifyFileAndNotifyFoldersTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/basic/two.dd", 3L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/basic/three.dd", 5L);
        compositions.put(3L, compositionThree);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Folder basicFolder = musicFolderDataSource.getCompositionsInPath("root/music/basic").blockingGet();
        TestObserver<FileSource> basicFolderSelfObserver = basicFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> basicFolderChildObserver = basicFolder.getFilesObservable().test();

        Composition compositionTwoChanged = fakeComposition(2L, "root/music/basic/two.dd", 7L);
        changeSubject.onNext(new Change.ModifyChange<>(asList(new ModifiedData<>(compositionTwo, compositionTwoChanged))));

        basicFolderChildObserver.assertValueAt(1, list -> {
            assertEquals(compositionTwoChanged, ((MusicFileSource) list.get(0)).getComposition());
            return true;
        });

        basicFolderSelfObserver.assertValue(data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(7L, folderNode.getLatestCreateDate().getTime());
            assertEquals(5L, folderNode.getEarliestCreateDate().getTime());
            return true;
        });

        childrenObserver.assertValueAt(1, list -> {
            FolderFileSource folderNode = ((FolderFileSource) list.get(1));
            assertEquals(7L, folderNode.getLatestCreateDate().getTime());
            assertEquals(5L, folderNode.getEarliestCreateDate().getTime());
            return true;
        });

        selfChangeObserver.assertValue(data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(7L, folderNode.getLatestCreateDate().getTime());
            assertEquals(4L, folderNode.getEarliestCreateDate().getTime());
            return true;
        });
    }

    @Test
    public void modifyUnexcitedFileTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = new Composition();
        compositionOne.setFilePath("root/music/one.dd");
        compositionOne.setId(1L);
        compositions.put(1L, compositionOne);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/two.dd");
        compositionTwo.setId(2L);
        changeSubject.onNext(new Change.ModifyChange<>(asList(new ModifiedData<>(compositionTwo, compositionTwo))));

        childrenObserver.assertValueAt(1, list -> {
            assertEquals(2, list.size());
            assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
            assertEquals(compositionTwo, ((MusicFileSource) list.get(1)).getComposition());
            return true;
        });

        selfChangeObserver.assertValue(data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(2, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void changeFilePathTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/two.dd", 4L);
        compositions.put(2L, compositionTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Composition compositionTwoChanged = fakeComposition(2L, "root/music/basic/two.dd", 4L);

        changeSubject.onNext(new Change.ModifyChange<>(asList(new ModifiedData<>(compositionTwo, compositionTwoChanged))));

        childrenObserver.assertValueAt(1, list -> {
            assertEquals(1, list.size());
            return true;
        });

        childrenObserver.assertValueAt(2, list -> {
            assertEquals(2, list.size());

            FolderFileSource folderNode = (FolderFileSource) list.get(1);
            assertEquals("root/music/basic", folderNode.getFullPath());
            return true;
        });

        selfChangeObserver.assertValueAt(0, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(1, folderNode.getFilesCount());
            return true;
        });

        selfChangeObserver.assertValueAt(2, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(2, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void changeFileNameTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/two.dd", 4L);
        compositions.put(2L, compositionTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Composition compositionTwoChanged = fakeComposition(2L, "root/music/two(2).dd", 4L);

        changeSubject.onNext(new Change.ModifyChange<>(asList(new ModifiedData<>(compositionTwo, compositionTwoChanged))));

        childrenObserver.assertValueAt(1, list -> {
            assertEquals(1, list.size());
            return true;
        });

        childrenObserver.assertValueAt(2, list -> {
            assertEquals(2, list.size());

            MusicFileSource folderNode = (MusicFileSource) list.get(1);
            assertEquals("root/music/two(2).dd", folderNode.getComposition().getFilePath());
            return true;
        });

        selfChangeObserver.assertValueAt(0, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(1, folderNode.getFilesCount());
            return true;
        });

        selfChangeObserver.assertValueAt(1, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(2, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void changeFolderPathTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/ww/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/ww/two.dd", 4L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/three.dd", 4L);
        compositions.put(3L, compositionThree);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Composition compositionOneChanged = fakeComposition(1L, "root/music/ww3/one.dd", 4L);
        Composition compositionTwoChanged = fakeComposition(2L, "root/music/ww3/two.dd", 4L);

        changeSubject.onNext(new Change.ModifyChange<>(asList(
                new ModifiedData<>(compositionOne, compositionOneChanged),
                new ModifiedData<>(compositionTwo, compositionTwoChanged)
        )));

        childrenObserver.assertValueAt(5, list -> {
            assertEquals(2, list.size());
            return true;
        });

        childrenObserver.assertValueAt(5, list -> {
            assertEquals(2, list.size());

            FolderFileSource folderNode = (FolderFileSource) list.get(1);
            assertEquals("root/music/ww3", folderNode.getFullPath());
            return true;
        });

        selfChangeObserver.assertValueAt(5, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(3, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void getManyCompositionTest() {
        when(storageMusicDataSource.getCompositionsMap()).thenReturn(getManyCompositionsMap());

        getSourceObservable(null)
                .test()
                .assertValue(list -> {
                    assertEquals(getManyCompositionsMap().size(), list.size());
                    return true;
                });
    }

    @Test
    public void getAvailablePathsTest() {
        Map<Long, Composition> compositions = new HashMap<>();

        Composition compositionOne = fakeComposition(1L, "root/music/one.dd", 1L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/two.dd", 2L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/favorite/three.dd", 3L);
        compositions.put(3L, compositionThree);

        Composition compositionFour = fakeComposition(4L, "root/music/favorite/kiz/four.dd", 4L);
        compositions.put(4L, compositionFour);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        musicFolderDataSource.getAvailablePathsForPath("root/music/favorite/kiz")
                .test()
                .assertValue(list -> {
                    assertNull(list.get(0));
                    assertEquals("root/music/favorite", list.get(1));
                    assertEquals("root/music/favorite/kiz", list.get(2));
                    return true;
                });
    }

    private static Map<Long, Composition> getManyCompositionsMap() {
        Map<Long, Composition> compositions = new HashMap<>();
        for (long i = 0; i < 100000; i++) {
            Composition composition = new Composition();
            composition.setFilePath("0/1/2/3/4/5/6/7/8/9/10/music-" + i);
            composition.setId(i);
            compositions.put(i, composition);
        }
        return compositions;
    }

    private static Map<Long, Composition> getFakeCompositionsMap() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition composition = new Composition();

        composition.setFilePath("music-" + 1L);
        composition.setId(1L);
        compositions.put(1L, composition);
        return compositions;
    }

    private Observable<List<FileSource>> getSourceObservable(String path) {
        return musicFolderDataSource.getCompositionsInPath(path).blockingGet().getFilesObservable();
    }

}