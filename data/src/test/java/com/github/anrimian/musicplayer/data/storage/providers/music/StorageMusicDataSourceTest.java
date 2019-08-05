package com.github.anrimian.musicplayer.data.storage.providers.music;

import com.github.anrimian.musicplayer.data.storage.files.FileManager;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeCompositionsMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageMusicDataSourceTest {

    private StorageMusicProvider musicProvider = mock(StorageMusicProvider.class);
    private FileManager fileManager = mock(FileManager.class);

    private PublishSubject<Map<Long, Composition>> changeSubject = PublishSubject.create();

    private StorageMusicDataSource storageMusicDataSource;

    @Before
    public void setUp() {
        when(musicProvider.getCompositions()).thenReturn(getFakeCompositionsMap());
        when(musicProvider.getChangeObservable()).thenReturn(changeSubject);

        storageMusicDataSource = new StorageMusicDataSource(musicProvider,
                fileManager,
                Schedulers.trampoline());
    }

    @Test
    public void removeChangeTest() {
        TestObserver<Change<Composition>> changeTestObserver = storageMusicDataSource.getChangeObservable().test();
        storageMusicDataSource.getCompositionsMap();

        Map<Long, Composition> changedCompositions = getFakeCompositionsMap();
        changedCompositions.remove(changedCompositions.size() - 1L);
        changedCompositions.remove(3L);
        changedCompositions.remove(1L);
        changedCompositions.remove(0L);

        changeSubject.onNext(changedCompositions);

        changeTestObserver.assertValue(change -> {
            Change.DeleteChange deleteChange = (Change.DeleteChange) change;
            assertEquals(getFakeCompositionsMap().get(0L), deleteChange.getData().get(0));
            assertEquals(getFakeCompositionsMap().get(1L), deleteChange.getData().get(1));
            assertEquals(getFakeCompositionsMap().get(3L), deleteChange.getData().get(2));
            assertEquals(getFakeCompositionsMap().get(getFakeCompositionsMap().size() - 1L), deleteChange.getData().get(3));
            return true;
        });

        storageMusicDataSource.getCompositions()
                .test()
                .assertValue(compositions -> {
                    assertNull(compositions.get(0L));
                    assertNull(compositions.get(1L));
                    assertNull(compositions.get(3L));
                    assertNull(compositions.get(getFakeCompositionsMap().size() - 1L));
                    return true;
                });
    }

    @Test
    public void addChangeTest() {
        TestObserver<Change<Composition>> changeTestObserver = storageMusicDataSource.getChangeObservable().test();
        storageMusicDataSource.getCompositionsMap();

        Map<Long, Composition> changedCompositions = getFakeCompositionsMap();
        Composition addedComposition = new Composition();
        addedComposition.setId(-1L);
        changedCompositions.put(-1L, addedComposition);

        changeSubject.onNext(changedCompositions);

        changeTestObserver.assertValue(change -> {
            Change.AddChange addChange = (Change.AddChange) change;
            assertEquals(addedComposition, addChange.getData().get(0));
            return true;
        });

        storageMusicDataSource.getCompositions()
                .test()
                .assertValue(compositions -> {
                    assertEquals(addedComposition, compositions.get(-1L));
                    return true;
                });
    }


    @Test
    public void modifyChangeTest() {
        TestObserver<Change<Composition>> changeTestObserver = storageMusicDataSource.getChangeObservable().test();
        storageMusicDataSource.getCompositionsMap();

        Map<Long, Composition> changedCompositions = getFakeCompositionsMap();
        Composition changedComposition = changedCompositions.get(0L);
        changedComposition.setFilePath("test path");

        changeSubject.onNext(changedCompositions);

        changeTestObserver.assertValue(change -> {
            Change.ModifyChange<Composition> modifyChange = (Change.ModifyChange) change;
            assertEquals(changedComposition, modifyChange.getData().get(0).getNewData());
            return true;
        });

        storageMusicDataSource.getCompositions()
                .test()
                .assertValue(compositions -> {
                    assertEquals(changedComposition, compositions.get(0L));
                    assertEquals("test path", compositions.get(0L).getFilePath());
                    return true;
                });
    }

    @Test
    public void deleteCompositionTest() {
        TestObserver<Change<Composition>> changeTestObserver = storageMusicDataSource.getChangeObservable().test();
        TestObserver<Map<Long, Composition>> compositionObserver = storageMusicDataSource.getCompositions().test();

        storageMusicDataSource.deleteComposition(getFakeCompositionsMap().get(0L))
                .test()
                .assertComplete();

        changeTestObserver.assertValue(change -> {
            Change.DeleteChange deleteChange = (Change.DeleteChange) change;
            assertEquals(getFakeCompositionsMap().get(0L), deleteChange.getData().get(0));
            return true;
        });

        compositionObserver.assertValue(compositions -> {
            assertNull(compositions.get(0L));
            return true;
        });
    }

    @Test
    public void getCompositionById() {
        storageMusicDataSource.getCompositionById(1L);

        verify(musicProvider).getComposition(eq(1L));
    }
}