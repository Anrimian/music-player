package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.ADDED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.DELETED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.MODIFY;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
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
                    List<FileSource> list = folder.getFiles();
                    assertEquals(1, list.size());
                    assertEquals(composition, ((MusicFileSource) list.get(0)).getComposition());
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
                    List<FileSource> list = folder.getFiles();
                    assertEquals(3, list.size());
                    assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
                    assertEquals(compositionTwo, ((MusicFileSource) list.get(1)).getComposition());

                    FolderFileSource folderNode = (FolderFileSource) list.get(2);
                    assertEquals("root/music/favorite", folderNode.getPath());
                    assertEquals(1, folderNode.getFilesCount());
                    return true;
                });

        musicFolderDataSource.getMusicInPath("root/music/favorite")
                .test()
                .assertValue(folder -> {
                    List<FileSource> list = folder.getFiles();
                    assertEquals(1, list.size());
                    assertEquals(compositionThree, ((MusicFileSource) list.get(0)).getComposition());
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
        TestObserver<Change<FileSource>> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<Change<List<FileSource>>> childChangeObserver = observerFolder.getChildChangeObservable().test();

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/two.dd");
        compositionTwo.setId(2L);
        changeSubject.onNext(new Change<>(ADDED, singletonList(compositionTwo)));

        childChangeObserver.assertValue(change -> {
            assertEquals(ADDED, change.getChangeType());
            assertEquals(compositionTwo, ((MusicFileSource) change.getData().get(0)).getComposition());
            return true;
        });

        selfChangeObserver.assertValue(change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderFileSource folderNode = (FolderFileSource) change.getData();
            assertEquals(2, folderNode.getFilesCount());
            return true;
        });

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    List<FileSource> list = folder.getFiles();
                    assertEquals(2, list.size());
                    assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
                    assertEquals(compositionTwo, ((MusicFileSource) list.get(1)).getComposition());
                    return true;
                });
    }

    @Test
    public void newCompositionChangeInChildNodeTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = new Composition();
        compositionOne.setFilePath("root/music/one.dd");
        compositionOne.setId(1L);
        compositions.put(1L, compositionOne);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getMusicInPath(null).blockingGet();
        TestObserver<Change<FileSource>> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<Change<List<FileSource>>> childChangeObserver = observerFolder.getChildChangeObservable().test();

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/basic/two.dd");
        compositionTwo.setId(2L);
        changeSubject.onNext(new Change<>(ADDED, singletonList(compositionTwo)));

        childChangeObserver.assertValueAt(0, change -> {
            assertEquals(ADDED, change.getChangeType());
            FolderFileSource folderNode = ((FolderFileSource) change.getData().get(0));
            assertEquals("root/music/basic", folderNode.getPath());
            assertEquals(0, folderNode.getFilesCount());
            return true;
        });

        childChangeObserver.assertValueAt(1, change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderFileSource folderNode = ((FolderFileSource) change.getData().get(0));
            assertEquals("root/music/basic", folderNode.getPath());
            assertEquals(1, folderNode.getFilesCount());
            return true;
        });

        selfChangeObserver.assertValue(change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderFileSource folderNode = (FolderFileSource) change.getData();
            assertEquals(2, folderNode.getFilesCount());
            return true;
        });

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    List<FileSource> list = folder.getFiles();
                    assertEquals(2, list.size());
                    assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());

                    FolderFileSource folderNode = ((FolderFileSource) list.get(1));
                    assertEquals("root/music/basic", folderNode.getPath());
                    assertEquals(1, folderNode.getFilesCount());
                    return true;
                });
    }

    @Test
    public void deleteCompositionTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = new Composition();
        compositionOne.setFilePath("root/music/one.dd");
        compositionOne.setId(1L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/two.dd");
        compositionTwo.setId(2L);
        compositions.put(2L, compositionTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getMusicInPath(null).blockingGet();
        TestObserver<Change<FileSource>> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<Change<List<FileSource>>> childChangeObserver = observerFolder.getChildChangeObservable().test();

        changeSubject.onNext(new Change<>(DELETED, singletonList(compositionTwo)));

        childChangeObserver.assertValue(change -> {
            assertEquals(DELETED, change.getChangeType());
            assertEquals(compositionTwo, ((MusicFileSource) change.getData().get(0)).getComposition());
            return true;
        });

        selfChangeObserver.assertValue(change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderFileSource folderNode = (FolderFileSource) change.getData();
            assertEquals(1, folderNode.getFilesCount());
            return true;
        });

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    List<FileSource> list = folder.getFiles();
                    assertEquals(1, list.size());
                    assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
                    return true;
                });
    }

    @Test
    public void deleteCompositionInChildNodeTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = new Composition();
        compositionOne.setFilePath("root/music/one.dd");
        compositionOne.setId(1L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/basic/two.dd");
        compositionTwo.setId(2L);
        compositions.put(2L, compositionTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getMusicInPath(null).blockingGet();
        TestObserver<Change<FileSource>> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<Change<List<FileSource>>> childChangeObserver = observerFolder.getChildChangeObservable().test();

        Folder basicFolder = musicFolderDataSource.getMusicInPath("root/music/basic").blockingGet();
        TestObserver<Change<FileSource>> basicFolderSelfObserver = basicFolder.getSelfChangeObservable().test();
        TestObserver<Change<List<FileSource>>> basicFolderChildObserver = basicFolder.getChildChangeObservable().test();

        changeSubject.onNext(new Change<>(DELETED, singletonList(compositionTwo)));

        basicFolderChildObserver.assertValue(change -> {
            assertEquals(DELETED, change.getChangeType());
            MusicFileSource MusicFileSource = ((MusicFileSource) change.getData().get(0));
            assertEquals(compositionTwo, MusicFileSource.getComposition());
            return true;
        });

        basicFolderSelfObserver.assertValueAt(0, change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderFileSource folderNode = ((FolderFileSource) change.getData());
            assertEquals("root/music/basic", folderNode.getPath());
            assertEquals(0, folderNode.getFilesCount());
            return true;
        });

        basicFolderSelfObserver.assertValueAt(1, change -> {
            assertEquals(DELETED, change.getChangeType());
            FolderFileSource folderNode = ((FolderFileSource) change.getData());
            assertEquals("root/music/basic", folderNode.getPath());
            return true;
        });

        childChangeObserver.assertValue(change -> {
            assertEquals(DELETED, change.getChangeType());
            FolderFileSource folderNode = ((FolderFileSource) change.getData().get(0));
            assertEquals("root/music/basic", folderNode.getPath());
            return true;
        });

        selfChangeObserver.assertValue(change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderFileSource folderNode = (FolderFileSource) change.getData();
            assertEquals(1, folderNode.getFilesCount());
            return true;
        });

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    List<FileSource> list = folder.getFiles();
                    assertEquals(1, list.size());
                    assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
                    return true;
                });
    }

    @Test
    public void modifyFileTest() {//TODO modify path test
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

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    MusicFileSource MusicFileSource = ((MusicFileSource) folder.getFiles().get(1));
                    assertEquals(2, MusicFileSource.getComposition().getSize());
                    return true;
                });

        Folder observerFolder = musicFolderDataSource.getMusicInPath(null).blockingGet();
        TestObserver<Change<List<FileSource>>> childChangeObserver = observerFolder.getChildChangeObservable().test();

        Composition compositionTwoChanged = new Composition();
        compositionTwoChanged.setFilePath("root/music/two.dd");
        compositionTwoChanged.setSize(4);
        compositionTwoChanged.setId(2L);

        changeSubject.onNext(new Change<>(MODIFY, singletonList(compositionTwoChanged)));

        childChangeObserver.assertValue(change -> {
            assertEquals(MODIFY, change.getChangeType());
            MusicFileSource MusicFileSource = ((MusicFileSource) change.getData().get(0));
            assertEquals(4, MusicFileSource.getComposition().getSize());
            return true;
        });

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    MusicFileSource MusicFileSource = ((MusicFileSource) folder.getFiles().get(1));
                    assertEquals(4, MusicFileSource.getComposition().getSize());
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

        Folder observerFolder = musicFolderDataSource.getMusicInPath(null).blockingGet();
        TestObserver<Change<FileSource>> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<Change<List<FileSource>>> childChangeObserver = observerFolder.getChildChangeObservable().test();

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/two.dd");
        compositionTwo.setId(2L);
        changeSubject.onNext(new Change<>(MODIFY, singletonList(compositionTwo)));

        childChangeObserver.assertValue(change -> {
            assertEquals(ADDED, change.getChangeType());
            assertEquals(compositionTwo, ((MusicFileSource) change.getData().get(0)).getComposition());
            return true;
        });

        selfChangeObserver.assertValue(change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderFileSource folderNode = (FolderFileSource) change.getData();
            assertEquals(2, folderNode.getFilesCount());
            return true;
        });

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    List<FileSource> list = folder.getFiles();
                    assertEquals(2, list.size());
                    assertEquals(compositionOne, ((MusicFileSource) list.get(0)).getComposition());
                    assertEquals(compositionTwo, ((MusicFileSource) list.get(1)).getComposition());
                    return true;
                });
    }

    @Test
    public void changeFilePathTest() {
        Map<Long, Composition> compositions = new HashMap<>();
        Composition compositionOne = new Composition();
        compositionOne.setFilePath("root/music/one.dd");
        compositionOne.setId(1L);
        compositions.put(1L, compositionOne);

        Composition compositionTwo = new Composition();
        compositionTwo.setFilePath("root/music/two.dd");
        compositionTwo.setId(2L);
        compositions.put(2L, compositionTwo);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(compositions);

        Folder observerFolder = musicFolderDataSource.getMusicInPath(null).blockingGet();
        TestObserver<Change<FileSource>> selfChangeObserver = observerFolder.getSelfChangeObservable().test();
        TestObserver<Change<List<FileSource>>> childChangeObserver = observerFolder.getChildChangeObservable().test();

        Composition compositionTwoChanged = new Composition();
        compositionTwoChanged.setFilePath("root/music/basic/two.dd");
        compositionTwoChanged.setId(2L);

        changeSubject.onNext(new Change<>(MODIFY, singletonList(compositionTwoChanged)));

        childChangeObserver.assertValueAt(0, change -> {
            assertEquals(DELETED, change.getChangeType());
            assertEquals(compositionTwo, ((MusicFileSource) change.getData().get(0)).getComposition());
            return true;
        });

        childChangeObserver.assertValueAt(1, change -> {
            assertEquals(ADDED, change.getChangeType());
            FolderFileSource folderNode = (FolderFileSource) change.getData().get(0);
            assertEquals("root/music/basic", folderNode.getPath());
            return true;
        });

        selfChangeObserver.assertValueAt(0, change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderFileSource folderNode = (FolderFileSource) change.getData();
            assertEquals(1, folderNode.getFilesCount());
            return true;
        });

        selfChangeObserver.assertValueAt(1, change -> {
            assertEquals(MODIFY, change.getChangeType());
            FolderFileSource folderNode = (FolderFileSource) change.getData();
            assertEquals(2, folderNode.getFilesCount());
            return true;
        });

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    List<FileSource> list = folder.getFiles();
                    assertEquals(2, list.size());

                    FolderFileSource folderNode = (FolderFileSource) list.get(1);
                    assertEquals("root/music/basic", folderNode.getPath());
                    return true;
                });
    }

    @Test
    public void getManyCompositionTest() {
        when(storageMusicDataSource.getCompositionsMap()).thenReturn(getManyCompositionsMap());

        musicFolderDataSource.getMusicInPath(null)
                .test()
                .assertValue(folder -> {
                    List<FileSource> list = folder.getFiles();
                    assertEquals(getManyCompositionsMap().size(), list.size());
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

}