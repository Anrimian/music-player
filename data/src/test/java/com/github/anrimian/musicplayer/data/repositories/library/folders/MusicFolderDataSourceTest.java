package com.github.anrimian.musicplayer.data.repositories.library.folders;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;
import com.github.anrimian.musicplayer.domain.utils.changes.ModifiedData;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeCompositionWithSize;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MusicFolderDataSourceTest {

    private CompositionFoldersCache storageMusicDataSource = mock(CompositionFoldersCache.class);
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
        Composition composition = fakeComposition(1L, "simple path");

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
                    assertEquals("root/music/favorite", folderNode.getPath());
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
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd");
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
            assertEquals("root/music/basic", folderNode.getPath());
            assertEquals(0, folderNode.getFilesCount());
            assertNull(folderNode.getEarliestCreateDate());
            assertNull(folderNode.getLatestCreateDate());
            return true;
        });

        childrenObserver.assertValueAt(2, list -> {
            assertEquals(2, list.size());
            assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());

            FolderFileSource folderNode = ((FolderFileSource) list.get(1));
            assertEquals("root/music/basic", folderNode.getPath());
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
            assertEquals("root/music/basic", folderNode.getPath());
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

        selfChangeObserver.assertValueAt(1, data -> {
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
            assertEquals("root/music/basic", folderNode.getPath());
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
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd");
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeCompositionWithSize(2L, "root/music/two.dd", 2);
        compositions.put(2L, compositionTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Composition compositionTwoChanged = fakeCompositionWithSize(2L, "root/music/two.dd", 4);

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
        Composition compositionOne = fakeComposition(1L, "root/music/one.dd");
        compositions.put(1L, compositionOne);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> childrenObserver = observerFolder.getFilesObservable().test();

        Composition compositionTwo = fakeComposition(2L, "root/music/two.dd");
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
            assertEquals("root/music/basic", folderNode.getPath());
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
            assertEquals("root/music/ww3", folderNode.getPath());
            return true;
        });

        selfChangeObserver.assertValueAt(5, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(3, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void changeFolderNameTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/ww/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/ww/two.dd", 4L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/three.dd", 4L);
        compositions.put(3L, compositionThree);

        Composition compositionFour = fakeComposition(4L, "root/music/ww/etc/four.dd", 4L);
        compositions.put(4L, compositionFour);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder rootFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<List<FileSource>> childrenObserver = rootFolder.getFilesObservable().test();

        Folder changedFolder = musicFolderDataSource.getCompositionsInPath("root/music/ww").blockingGet();

        TestObserver<FileSource> changedFolderObserver = changedFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> changedFolderChildrenObserver = changedFolder.getFilesObservable().test();

        musicFolderDataSource.changeFolderName("root/music/ww", "root/music/zzz")
                .test()
                .assertValue(affectedCompositions -> {
                    assertEquals("root/music/zzz/etc/four.dd", affectedCompositions.get(0).getFilePath());
                    assertEquals("root/music/zzz/one.dd", affectedCompositions.get(1).getFilePath());
                    assertEquals("root/music/zzz/two.dd", affectedCompositions.get(2).getFilePath());
                    return true;
                })
                .assertComplete();

        childrenObserver.assertValueAt(1, data -> {
            FolderFileSource folderNode = (FolderFileSource) data.get(1);
            assertEquals("root/music/zzz", folderNode.getPath());
            return true;
        });

        changedFolderObserver.assertValueAt(0, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals("root/music/zzz", folderNode.getPath());
            return true;
        });

        changedFolderChildrenObserver.assertValueAt(changedFolderChildrenObserver.valueCount()-1, list -> {
            assertEquals(3, list.size());
            assertEquals("root/music/zzz/etc", ((FolderFileSource) list.get(0)).getPath());
            assertEquals("root/music/zzz/one.dd", ((MusicFileSource) list.get(1)).getComposition().getFilePath());
            assertEquals("root/music/zzz/two.dd", ((MusicFileSource) list.get(2)).getComposition().getFilePath());
            return true;
        });
    }

    @Test
    public void moveCompositionsFromRootToFolderTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/moveFolder/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/moveFolder/two.dd", 4L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/three.dd", 4L);
        compositions.put(3L, compositionThree);

        Composition compositionFour = fakeComposition(4L, "root/music/four.dd", 4L);
        compositions.put(4L, compositionFour);

        Composition compositionFive = fakeComposition(5L, "root/music/destFolder/five.dd", 4L);
        compositions.put(5L, compositionFive);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder rootFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<List<FileSource>> rootFolderChildrenObserver = rootFolder.getFilesObservable().test();

        Folder destFolder = musicFolderDataSource.getCompositionsInPath("root/music/destFolder").blockingGet();

        TestObserver<FileSource> destFolderObserver = destFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> destFolderChildrenObserver = destFolder.getFilesObservable().test();

        musicFolderDataSource.moveFileTo("root/music/destFolder",
                "root/music/destFolder/moveFolder",
                new FolderFileSource("root/music/moveFolder", 0, new Date(), new Date())

        ).test().assertValue(affectedCompositions -> {
            assertEquals("root/music/destFolder/moveFolder/one.dd", affectedCompositions.get(0).getFilePath());
            assertEquals("root/music/destFolder/moveFolder/two.dd", affectedCompositions.get(1).getFilePath());
            return true;
        }).assertComplete();

        musicFolderDataSource.moveFileTo("root/music/destFolder",
                "root/music/destFolder/three.dd",
                new MusicFileSource(compositionThree)
        ).test().assertValue(affectedCompositions -> {
            assertEquals("root/music/destFolder/three.dd", affectedCompositions.get(0).getFilePath());
            return true;
        }).assertComplete();

        rootFolderChildrenObserver.assertValueAt(rootFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(2, data.size());

            FolderFileSource folderNode = (FolderFileSource) data.get(0);
            assertEquals("root/music/destFolder", folderNode.getPath());
            assertEquals(4, folderNode.getFilesCount());

            MusicFileSource musicFileSource = (MusicFileSource) data.get(1);
            assertEquals(compositionFour, musicFileSource.getComposition());
            return true;
        });

        destFolderChildrenObserver.assertValueAt(destFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(3, data.size());

            MusicFileSource musicFileSource1 = (MusicFileSource) data.get(0);
            assertEquals(compositionFive, musicFileSource1.getComposition());

            FolderFileSource folderNode = (FolderFileSource) data.get(1);
            assertEquals("root/music/destFolder/moveFolder", folderNode.getPath());
            assertEquals(2, folderNode.getFilesCount());

            MusicFileSource musicFileSource = (MusicFileSource) data.get(2);
            assertEquals(compositionThree, musicFileSource.getComposition());
            return true;
        });

        destFolderObserver.assertValueAt(destFolderObserver.valueCount()-1, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;

            assertEquals("root/music/destFolder", folderNode.getPath());
            assertEquals(4, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void moveCompositionsFromFolderToFolderTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/folderTo/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/folderTo/two.dd", 4L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/three.dd", 4L);
        compositions.put(3L, compositionThree);

        Composition compositionFour = fakeComposition(4L, "root/music/folderFrom/four.dd", 4L);
        compositions.put(4L, compositionFour);

        Composition compositionFive = fakeComposition(5L, "root/music/folderFrom/five.dd", 4L);
        compositions.put(5L, compositionFive);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder fromFolder = musicFolderDataSource.getCompositionsInPath("root/music/folderFrom").blockingGet();
        TestObserver<List<FileSource>> fromFolderChildrenObserver = fromFolder.getFilesObservable().test();

        Folder destFolder = musicFolderDataSource.getCompositionsInPath("root/music/folderTo").blockingGet();

        TestObserver<FileSource> destFolderObserver = destFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> destFolderChildrenObserver = destFolder.getFilesObservable().test();

        musicFolderDataSource.moveFileTo("root/music/folderTo",
                "root/music/folderTo/five.dd",
                new MusicFileSource(compositionFive)
        ).test().assertValue(affectedCompositions -> {
            assertEquals("root/music/folderTo/five.dd", affectedCompositions.get(0).getFilePath());
            return true;
        }).assertComplete();

        fromFolderChildrenObserver.assertValueAt(fromFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(1, data.size());

            MusicFileSource musicFileSource = (MusicFileSource) data.get(0);
            assertEquals(compositionFour, musicFileSource.getComposition());
            return true;
        });

        destFolderChildrenObserver.assertValueAt(destFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(3, data.size());

            MusicFileSource musicFileSource2 = (MusicFileSource) data.get(0);
            assertEquals(compositionOne, musicFileSource2.getComposition());

            MusicFileSource musicFileSource3 = (MusicFileSource) data.get(1);
            assertEquals(compositionTwo, musicFileSource3.getComposition());

            MusicFileSource musicFileSource1 = (MusicFileSource) data.get(2);
            assertEquals(compositionFive, musicFileSource1.getComposition());
            return true;
        });

        destFolderObserver.assertValueAt(destFolderObserver.valueCount()-1, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;

            assertEquals("root/music/folderTo", folderNode.getPath());
            assertEquals(3, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void moveCompositionsFromFolderToFolderWithTheSameFileNameTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/folderTo/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/folderTo/five.dd", 4L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/three.dd", 4L);
        compositions.put(3L, compositionThree);

        Composition compositionFour = fakeComposition(4L, "root/music/folderFrom/four.dd", 4L);
        compositions.put(4L, compositionFour);

        Composition compositionFive = fakeComposition(5L, "root/music/folderFrom/five.dd", 4L);
        compositions.put(5L, compositionFive);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder fromFolder = musicFolderDataSource.getCompositionsInPath("root/music/folderFrom").blockingGet();
        TestObserver<List<FileSource>> fromFolderChildrenObserver = fromFolder.getFilesObservable().test();

        Folder destFolder = musicFolderDataSource.getCompositionsInPath("root/music/folderTo").blockingGet();

        TestObserver<FileSource> destFolderObserver = destFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> destFolderChildrenObserver = destFolder.getFilesObservable().test();

        musicFolderDataSource.moveFileTo("root/music/folderTo",
                "root/music/folderTo/five(1).dd",
                new MusicFileSource(compositionFive)
        ).test().assertValue(affectedCompositions -> {
            assertEquals("root/music/folderTo/five(1).dd", affectedCompositions.get(0).getFilePath());
            return true;
        }).assertComplete();

        fromFolderChildrenObserver.assertValueAt(fromFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(1, data.size());

            MusicFileSource musicFileSource = (MusicFileSource) data.get(0);
            assertEquals(compositionFour, musicFileSource.getComposition());
            return true;
        });

        destFolderChildrenObserver.assertValueAt(destFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(3, data.size());

            MusicFileSource musicFileSource2 = (MusicFileSource) data.get(0);
            assertEquals(compositionOne, musicFileSource2.getComposition());

            MusicFileSource musicFileSource3 = (MusicFileSource) data.get(1);
            assertEquals(compositionTwo, musicFileSource3.getComposition());

            MusicFileSource musicFileSource1 = (MusicFileSource) data.get(2);
            assertEquals(compositionFive, musicFileSource1.getComposition());
            return true;
        });

        destFolderObserver.assertValueAt(destFolderObserver.valueCount()-1, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;

            assertEquals("root/music/folderTo", folderNode.getPath());
            assertEquals(3, folderNode.getFilesCount());
            return true;
        });
    }

    @Test
    public void moveFolderFromFolderToFolderWithTheSameFileNameTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = fakeComposition(1L, "root/music/gg(to)/folder/one.dd", 4L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = fakeComposition(2L, "root/music/gg(to)/folder/two.dd", 4L);
        compositions.put(2L, compositionTwo);

        Composition compositionThree = fakeComposition(3L, "root/music/three.dd", 4L);
        compositions.put(3L, compositionThree);

        Composition compositionOneTwo = fakeComposition(4L, "root/music/gg(from)/folder/one.dd", 4L);
        compositions.put(4L, compositionOneTwo);

        Composition compositionTwoTwo = fakeComposition(5L, "root/music/gg(from)/folder/two.dd", 4L);
        compositions.put(5L, compositionTwoTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder rootFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<List<FileSource>> rootFolderChildrenObserver = rootFolder.getFilesObservable().test();

        Folder fromFolder = musicFolderDataSource.getCompositionsInPath("root/music/gg(from)").blockingGet();
        TestObserver<List<FileSource>> fromFolderChildrenObserver = fromFolder.getFilesObservable().test();

        Folder destFolder = musicFolderDataSource.getCompositionsInPath("root/music/gg(to)").blockingGet();

        TestObserver<FileSource> destFolderObserver = destFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> destFolderChildrenObserver = destFolder.getFilesObservable().test();

        musicFolderDataSource.moveFileTo("root/music/gg(to)",
                "root/music/gg(to)/folder(1)",
                new FolderFileSource("root/music/gg(from)/folder", 2, new Date(), new Date())
        ).test().assertValue(affectedCompositions -> {
            assertEquals("root/music/gg(to)/folder(1)/one.dd", affectedCompositions.get(0).getFilePath());
            assertEquals("root/music/gg(to)/folder(1)/two.dd", affectedCompositions.get(1).getFilePath());
            return true;
        }).assertComplete();

        fromFolderChildrenObserver.assertValueAt(fromFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(0, data.size());
            return true;
        });

        rootFolderChildrenObserver.assertValueAt(rootFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(2, data.size());

            assertEquals("root/music/gg(to)", data.get(0).getPath());
            assertEquals("root/music/three.dd", data.get(1).getPath());
            return true;
        });

        destFolderChildrenObserver.assertValueAt(destFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(2, data.size());

            assertEquals("root/music/gg(to)/folder", ((FolderFileSource) data.get(0)).getPath());
            assertEquals("root/music/gg(to)/folder(1)", ((FolderFileSource) data.get(1)).getPath());
            return true;
        });

        destFolderObserver.assertValueAt(destFolderObserver.valueCount()-1, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;

            assertEquals("root/music/gg(to)", folderNode.getPath());
            assertEquals(4, folderNode.getFilesCount());
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

    @Test
    public void moveCompositionsToNewFolderTest() {
        Map<Long, Composition> compositions = new HashMap<>();

        Composition compositionThree = fakeComposition(3L, "root/music/three.dd", 4L);
        compositions.put(3L, compositionThree);

        Composition compositionFour = fakeComposition(4L, "root/music/folderFrom/four.dd", 4L);
        compositions.put(4L, compositionFour);

        Composition compositionFive = fakeComposition(5L, "root/music/folderFrom/five.dd", 4L);
        compositions.put(5L, compositionFive);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder fromFolder = musicFolderDataSource.getCompositionsInPath("root/music/folderFrom").blockingGet();
        TestObserver<List<FileSource>> fromFolderChildrenObserver = fromFolder.getFilesObservable().test();

        Folder rootFolder = musicFolderDataSource.getCompositionsInPath(null).blockingGet();
        TestObserver<FileSource> rootFolderObserver = rootFolder.getSelfChangeObservable().test();
        TestObserver<List<FileSource>> rootFolderChildrenObserver = rootFolder.getFilesObservable().test();

        musicFolderDataSource.moveFileTo("root/music/folderTo",
                "root/music/folderTo/five.dd",
                new MusicFileSource(compositionFive)
        ).test().assertValue(affectedCompositions -> {
            assertEquals("root/music/folderTo/five.dd", affectedCompositions.get(0).getFilePath());
            return true;
        }).assertComplete();

        fromFolderChildrenObserver.assertValueAt(fromFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(1, data.size());

            MusicFileSource musicFileSource = (MusicFileSource) data.get(0);
            assertEquals(compositionFour, musicFileSource.getComposition());
            return true;
        });

        rootFolderChildrenObserver.assertValueAt(rootFolderChildrenObserver.valueCount() - 1, data -> {
            assertEquals(3, data.size());
            assertEquals("root/music/folderTo", data.get(2).getPath());
            return true;
        });

        rootFolderObserver.assertValueAt(rootFolderObserver.valueCount()-1, data -> {
            FolderFileSource folderNode = (FolderFileSource) data;
            assertEquals(3, (folderNode.getFilesCount()));
            return true;
        });
    }

    private static Map<Long, Composition> getManyCompositionsMap() {
        Map<Long, Composition> compositions = new HashMap<>();
        for (long i = 0; i < 100000; i++) {
            Composition composition = fakeComposition(i, "0/1/2/3/4/5/6/7/8/9/10/music-" + i);
            compositions.put(i, composition);
        }
        return compositions;
    }

    private static Map<Long, Composition> getFakeCompositionsMap() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition composition = fakeComposition(1L, "music-" + 1L);
        compositions.put(1L, composition);
        return compositions;
    }

    private Observable<List<FileSource>> getSourceObservable(String path) {
        return musicFolderDataSource.getCompositionsInPath(path).blockingGet().getFilesObservable();
    }
}