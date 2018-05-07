package com.github.anrimian.simplemusicplayer.data.storage;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeableList;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositions;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StorageMusicDataSourceTest {

    private StorageMusicProvider musicProvider = mock(StorageMusicProvider.class);

    private PublishSubject<Object> publishSubject = PublishSubject.create();

    private StorageMusicDataSource storageMusicDataSource;

    @Before
    public void setUp() {
        when(musicProvider.getCompositions()).thenReturn(getFakeCompositions());
        when(musicProvider.getChangeObservable()).thenReturn(publishSubject);

        storageMusicDataSource = new StorageMusicDataSource(musicProvider);
    }

    @Test
    public void removeChangeTest() {
        ChangeableList<Composition> list = storageMusicDataSource.getCompositions().blockingGet();

        TestObserver<Change<Composition>> changeTestObserver = list.getChangeObservable().test();

        List<Composition> changedCompositions = getFakeCompositions();
        changedCompositions.remove(0);
        when(musicProvider.getCompositions()).thenReturn(changedCompositions);

        publishSubject.onNext(new Object());

        changeTestObserver.assertValue(change -> {
            assertEquals(ChangeType.DELETED, change.getChangeType());
            assertEquals(getFakeCompositions().get(0), change.getData());
            return true;
        });
    }
}