package com.github.anrimian.simplemusicplayer.data.storage;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeableMap;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositionsMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StorageMusicDataSourceTest {

    private StorageMusicProvider musicProvider = mock(StorageMusicProvider.class);

    private PublishSubject<Map<Long, Composition>> changeSubject = PublishSubject.create();

    private StorageMusicDataSource storageMusicDataSource;

    @Before
    public void setUp() {
        when(musicProvider.getCompositions()).thenReturn(getFakeCompositionsMap());
        when(musicProvider.getChangeObservable()).thenReturn(changeSubject);

        storageMusicDataSource = new StorageMusicDataSource(musicProvider, Schedulers.trampoline());
    }

    @Test
    public void removeChangeTest() {
        ChangeableMap<Long, Composition> list = storageMusicDataSource.getCompositions().blockingGet();

        TestObserver<Change<Composition>> changeTestObserver = list.getChangeObservable().test();

        Map<Long, Composition> changedCompositions = getFakeCompositionsMap();
        changedCompositions.remove(changedCompositions.size() - 1L);
        changedCompositions.remove(3L);
        changedCompositions.remove(1L);
        changedCompositions.remove(0L);

        changeSubject.onNext(changedCompositions);

        changeTestObserver.assertValue(change -> {
            assertEquals(ChangeType.DELETED, change.getChangeType());
            assertEquals(getFakeCompositionsMap().get(0L), change.getData().get(0));
            assertEquals(getFakeCompositionsMap().get(1L), change.getData().get(1));
            assertEquals(getFakeCompositionsMap().get(3L), change.getData().get(2));
            assertEquals(getFakeCompositionsMap().get(getFakeCompositionsMap().size() - 1L), change.getData().get(3));
            return true;
        });

        storageMusicDataSource.getCompositions()
                .test()
                .assertValue(compositions -> {
                    assertEquals(null, compositions.getHashMap().get(0L));
                    assertEquals(null, compositions.getHashMap().get(1L));
                    assertEquals(null, compositions.getHashMap().get(3L));
                    assertEquals(null, compositions.getHashMap().get(getFakeCompositionsMap().size() - 1L));
                    return true;
                });
    }

    @Test
    public void addChangeTest() {
        ChangeableMap<Long, Composition> list = storageMusicDataSource.getCompositions().blockingGet();

        TestObserver<Change<Composition>> changeTestObserver = list.getChangeObservable().test();

        Map<Long, Composition> changedCompositions = getFakeCompositionsMap();
        Composition addedComposition = new Composition();
        addedComposition.setId(-1L);
        changedCompositions.put(-1L, addedComposition);

        changeSubject.onNext(changedCompositions);

        changeTestObserver.assertValue(change -> {
            assertEquals(ChangeType.ADDED, change.getChangeType());
            assertEquals(addedComposition, change.getData().get(0));
            return true;
        });

        storageMusicDataSource.getCompositions()
                .test()
                .assertValue(compositions -> {
                    assertEquals(addedComposition, compositions.getHashMap().get(-1L));
                    return true;
                });
    }
}