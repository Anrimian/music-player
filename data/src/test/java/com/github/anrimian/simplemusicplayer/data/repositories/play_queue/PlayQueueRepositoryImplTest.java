package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDaoWrapper;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CompositionEvent;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.compositionEvent;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.currentComposition;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.fakeComposition;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositionsMap;
import static com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.DELETED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.MODIFY;
import static java.util.Collections.emptyList;
import static java.util.Collections.shuffle;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlayQueueRepositoryImplTest {

    private final PlayQueueDaoWrapper playQueueDao = mock(PlayQueueDaoWrapper.class);
    private final StorageMusicDataSource storageMusicDataSource = mock(StorageMusicDataSource.class);
    private final SettingsPreferences settingsPreferences = mock(SettingsPreferences.class);
    private final UiStatePreferences uiStatePreferences = mock(UiStatePreferences.class);

    private final PublishSubject<Change<List<Composition>>> changeSubject = PublishSubject.create();

    private InOrder inOrder = Mockito.inOrder(playQueueDao);

    private PlayQueueRepositoryImpl playQueueRepositoryImpl = new PlayQueueRepositoryImpl(playQueueDao,
            storageMusicDataSource,
            settingsPreferences,
            uiStatePreferences,
            Schedulers.trampoline());

    @Before
    public void setUp() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(false);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(getFakeCompositionsMap());
        when(storageMusicDataSource.getChangeObservable()).thenReturn(changeSubject);

        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(NO_COMPOSITION);
    }

    @Test
    public void setPlayQueueInNormalMode() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(playQueueDao).setPlayQueue(getFakeCompositions());
        verify(storageMusicDataSource).getChangeObservable();

        verify(uiStatePreferences).setCurrentCompositionId(0L);

        playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test()
                .assertValue(new CompositionEvent(fakeComposition(0)));
    }

    @Test
    public void setPlayQueueInShuffleMode() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(uiStatePreferences).setCurrentCompositionId(anyLong());

        playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test()
                .assertValueCount(1);

        playQueueRepositoryImpl.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertNotEquals(getFakeCompositions(), compositions);
                    assertEquals(getFakeCompositions().size(), compositions.size());
                    return true;
                });
    }

    @Test
    public void getEmptyPlayQueueInInitialState() {
        when(playQueueDao.getPlayQueue(any())).thenReturn(emptyList());

        playQueueRepositoryImpl.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(0, compositions.size());
                    return true;
                });

        verify(storageMusicDataSource, never()).getChangeObservable();
    }

    @Test
    public void getPlayQueueObservableInInitialState() {
        when(playQueueDao.getPlayQueue(any())).thenReturn(getFakeCompositions());
        when(playQueueDao.getShuffledPlayQueue(any())).thenReturn(getFakeCompositions());

        playQueueRepositoryImpl.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getFakeCompositions(), compositions);
                    return true;
                });

        verify(storageMusicDataSource).getChangeObservable();
    }

    @Test
    public void getCurrentCompositionInInitialState() {
        when(uiStatePreferences.getTrackPosition()).thenReturn(4L);
        when(storageMusicDataSource.getCompositionById(anyLong())).thenReturn(fakeComposition(1));

        playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test()
                .assertValue(event -> {
                    assertEquals(fakeComposition(1), event.getComposition());
                    assertEquals(4L, event.getTrackPosition());
                    return true;
                });
    }

    @Test
    public void getPlayQueueInInitialState() {
        when(playQueueDao.getPlayQueue(any())).thenReturn(getFakeCompositions());
        when(playQueueDao.getShuffledPlayQueue(any())).thenReturn(getFakeCompositions());

        playQueueRepositoryImpl.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getFakeCompositions(), compositions);
                    return true;
                });

        verify(storageMusicDataSource).getChangeObservable();
    }

    @Test
    public void getPlayQueueInInitialStateWithShuffledMode() {
        List<Composition> shuffledCompositions = getFakeCompositions();
        shuffle(shuffledCompositions);
        when(playQueueDao.getPlayQueue(any())).thenReturn(getFakeCompositions());
        when(playQueueDao.getShuffledPlayQueue(any())).thenReturn(shuffledCompositions);
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueRepositoryImpl.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(shuffledCompositions, compositions);
                    return true;
                });

        verify(storageMusicDataSource).getChangeObservable();
    }

    @Test
    public void getPlayQueueInInitialStateAndSetNewQueue() {
        when(playQueueDao.getPlayQueue(any())).thenReturn(getFakeCompositions());
        when(playQueueDao.getShuffledPlayQueue(any())).thenReturn(getFakeCompositions());

        playQueueRepositoryImpl.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getFakeCompositions(), compositions);
                    return true;
                });

        TestObserver<List<Composition>> playQueueObserver = playQueueRepositoryImpl
                .getPlayQueueObservable()
                .test();

        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(playQueueDao).setPlayQueue(getFakeCompositions());

        verify(storageMusicDataSource, times(1)).getChangeObservable();

        playQueueObserver.assertValueAt(1, getFakeCompositions());
    }

    @Test
    public void setRandomPlayingDisabledTest() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<List<Composition>> playQueueObserver = playQueueRepositoryImpl
                .getPlayQueueObservable()
                .test();

        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(false);

        playQueueRepositoryImpl.setRandomPlayingEnabled(false);

        playQueueObserver.assertValueCount(2);
    }

    @Test
    public void setRandomPlayingEnabledTest() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<List<Composition>> playQueueObserver = playQueueRepositoryImpl
                .getPlayQueueObservable()
                .test();

        TestObserver<CompositionEvent> currentCompositionObserver = playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test();

        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueRepositoryImpl.setRandomPlayingEnabled(true);

        verify(playQueueDao).setPlayQueue(anyListOf(Composition.class));
        playQueueObserver.assertValueCount(2);
        currentCompositionObserver.assertValueCount(1);
    }

    @Test
    public void skipToNext() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<CompositionEvent> compositionObserver = playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test();

        playQueueRepositoryImpl.skipToNext().subscribe();

        compositionObserver.assertValues(currentComposition(fakeComposition(0)),
                currentComposition(fakeComposition(1)));
    }

    @Test
    public void skipToNextFromInitialState() {
        when(storageMusicDataSource.getCompositionById(anyLong())).thenReturn(fakeComposition(1));
        when(playQueueDao.getPlayQueue(any())).thenReturn(getFakeCompositions());
        when(playQueueDao.getShuffledPlayQueue(any())).thenReturn(getFakeCompositions());
        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(1L);

        TestObserver<CompositionEvent> compositionObserver = playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test();

        playQueueRepositoryImpl.skipToNext().subscribe();

        compositionObserver.assertValues(currentComposition(fakeComposition(1)),
                currentComposition(fakeComposition(2)));
    }

    @Test
    public void skipToPrevious() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<CompositionEvent> compositionObserver = playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test();

        playQueueRepositoryImpl.skipToNext()
                .flatMap(pos -> playQueueRepositoryImpl.skipToPrevious())
                .subscribe();

        compositionObserver.assertValues(currentComposition(fakeComposition(0)),
                currentComposition(fakeComposition(1)),
                currentComposition(fakeComposition(0)));
    }

    @Test
    public void skipToPositionTest() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<CompositionEvent> compositionObserver = playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test();

        playQueueRepositoryImpl.skipToPosition(1000).subscribe();

        compositionObserver.assertValues(currentComposition(fakeComposition(0)),
                currentComposition(fakeComposition(1000)));
    }

    @Test
    public void testDeletedChanges() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<CompositionEvent> compositionObserver = playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test();

        Composition unexcitedComposition = new Composition();
        unexcitedComposition.setId(2000000);
        changeSubject.onNext(new Change<>(DELETED, Arrays.asList(fakeComposition(0),
                fakeComposition(1),
                unexcitedComposition)
        ));

        List<Composition> expectedList = getFakeCompositions();
        expectedList.remove(0);
        expectedList.remove(0);

        inOrder.verify(playQueueDao).setShuffledPlayQueue(anyListOf(Composition.class));
        inOrder.verify(playQueueDao).setPlayQueue(expectedList);

        playQueueRepositoryImpl.getPlayQueueObservable()
                .test()
                .assertValue(expectedList);

        compositionObserver.assertValues(compositionEvent(fakeComposition(0)),
                compositionEvent(fakeComposition(2)));
    }

    @Test
    public void testDeleteChangeWithShuffledPlayQueue() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions()).subscribe();

        playQueueRepositoryImpl.setRandomPlayingEnabled(true);

        List<Composition> compositions = playQueueRepositoryImpl.getPlayQueueObservable().blockingFirst();

        List<Composition> expectedList = new ArrayList<>(compositions);
        expectedList.remove(0);

        changeSubject.onNext(new Change<>(DELETED, singletonList(fakeComposition(0))));

        playQueueRepositoryImpl.getPlayQueueObservable()
                .test()
                .assertValue(expectedList);
    }

    @Test
    public void testAllDeletedChange() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<CompositionEvent> compositionObserver = playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test();

        changeSubject.onNext(new Change<>(DELETED, getFakeCompositions()));

        compositionObserver.assertValues(compositionEvent(fakeComposition(0)),
                compositionEvent(null));

        playQueueRepositoryImpl.getPlayQueueObservable()
                .test()
                .assertValue(emptyList());
    }

    @Test
    public void testModifyChanges() {
        playQueueRepositoryImpl.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<CompositionEvent> compositionObserver = playQueueRepositoryImpl.getCurrentCompositionObservable()
                .test();

        Composition changedComposition = fakeComposition(0);
        changedComposition.setTitle("changed title");

        Composition unexcitedComposition = new Composition();
        unexcitedComposition.setId(2000000);
        changeSubject.onNext(new Change<>(MODIFY, Arrays.asList(changedComposition,
                fakeComposition(1),
                unexcitedComposition)
        ));

        playQueueRepositoryImpl
                .getPlayQueueObservable()
                .test()
                .assertValue(list -> {
                    assertEquals("changed title", list.get(0).getTitle());
                    return true;
                });

        compositionObserver.assertValues(compositionEvent(fakeComposition(0)),
                compositionEvent(fakeComposition(0)));

        compositionObserver.assertValueAt(1, event -> {
            assertEquals("changed title", event.getComposition().getTitle());
            return true;
        });
    }
}