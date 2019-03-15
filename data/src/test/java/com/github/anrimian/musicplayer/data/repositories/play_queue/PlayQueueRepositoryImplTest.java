package com.github.anrimian.musicplayer.data.repositories.play_queue;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueLists;
import com.github.anrimian.musicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.currentItem;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeItem;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeCompositionsMap;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeItems;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getReversedFakeItems;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static com.github.anrimian.musicplayer.domain.utils.changes.ChangeType.DELETED;
import static com.github.anrimian.musicplayer.domain.utils.changes.ChangeType.MODIFY;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
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

    private PlayQueueRepository playQueueRepository = new PlayQueueRepositoryImpl(playQueueDao,
            storageMusicDataSource,
            settingsPreferences,
            uiStatePreferences,
            Schedulers.trampoline());

    @Before
    public void setUp() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(false);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(getFakeCompositionsMap());
        when(storageMusicDataSource.getChangeObservable()).thenReturn(changeSubject);

        when(playQueueDao.insertNewPlayQueue(any())).thenReturn(
                new PlayQueueLists(getFakeItems(), getReversedFakeItems()));

        when(uiStatePreferences.getCurrentPlayQueueId()).thenReturn(NO_COMPOSITION);
        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(NO_COMPOSITION);
    }

    @Test
    public void setPlayQueueInNormalMode() {
        playQueueRepository.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(playQueueDao).insertNewPlayQueue(getFakeCompositions());
        verify(storageMusicDataSource).getChangeObservable();

        verify(uiStatePreferences).setCurrentCompositionId(0L);

        playQueueRepository.getCurrentQueueItemObservable()
                .test()
                .assertValue(new PlayQueueEvent(new PlayQueueItem(0, fakeComposition(0))));
    }

    @Test
    public void setPlayQueueInNormalModeWithStartPosition() {
        playQueueRepository.setPlayQueue(getFakeCompositions(), 1000)
                .test()
                .assertComplete();

        verify(playQueueDao).insertNewPlayQueue(getFakeCompositions());
        verify(storageMusicDataSource).getChangeObservable();

        verify(uiStatePreferences).setCurrentCompositionId(1000L);

        playQueueRepository.getCurrentQueueItemObservable()
                .test()
                .assertValue(new PlayQueueEvent(new PlayQueueItem(1000, fakeComposition(1000))));
    }

    @Test
    public void setPlayQueueInShuffleMode() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueRepository.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(uiStatePreferences).setCurrentCompositionId(anyLong());

        playQueueRepository.getCurrentQueueItemObservable()
                .test()
                .assertValueCount(1);

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getReversedFakeItems(), compositions);
                    return true;
                });
    }

    @Test
    public void setPlayQueueInShuffleModeWithStartPosition() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueRepository.setPlayQueue(getFakeCompositions(), 1000)
                .test()
                .assertComplete();

        verify(uiStatePreferences).setCurrentCompositionId(1000L);

        playQueueRepository.getCurrentQueueItemObservable()
                .test()
                .assertValueCount(1);

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getReversedFakeItems(), compositions);
                    return true;
                });
    }

    @Test
    public void setPlayQueueInShuffleModeAndThenSwitchMode() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueRepository.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(uiStatePreferences).setCurrentCompositionId(99999L);

        playQueueRepository.getCurrentQueueItemObservable()
                .test()
                .assertValue(currentItem(99999));

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getReversedFakeItems(), compositions);
                    return true;
                });

        playQueueRepository.setRandomPlayingEnabled(false);

        playQueueRepository.getCurrentQueueItemObservable()
                .test()
                .assertValue(currentItem(99999));
    }

    @Test
    public void getEmptyPlayQueueInInitialState() {
        when(playQueueDao.getPlayQueue(any())).thenReturn(new PlayQueueLists(emptyList(), emptyList()));

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(0, compositions.size());
                    return true;
                });

        verify(storageMusicDataSource, never()).getChangeObservable();
    }

    @Test
    public void getPlayQueueObservableInInitialState() {
        when(playQueueDao.getPlayQueue(any())).thenReturn(new PlayQueueLists(getFakeItems(), getFakeItems()));

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(items -> {
                    assertEquals(getFakeItems(), items);
                    return true;
                });

        verify(storageMusicDataSource).getChangeObservable();
    }


    @Test
    public void getCurrentCompositionInInitialState() {
        when(uiStatePreferences.getCurrentPlayQueueId()).thenReturn(1L);
        when(uiStatePreferences.getTrackPosition()).thenReturn(4L);
        when(storageMusicDataSource.getCompositionById(anyLong())).thenReturn(fakeComposition(1));

        playQueueRepository.getCurrentQueueItemObservable()
                .test()
                .assertValue(event -> {
                    assertEquals(fakeItem(1), event.getPlayQueueItem());
                    assertEquals(4L, event.getTrackPosition());
                    return true;
                });
    }

    @Test
    public void getPlayQueueInInitialStateWithShuffledMode() {
        when(playQueueDao.getPlayQueue(any())).thenReturn(new PlayQueueLists(getFakeItems(), getReversedFakeItems()));
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getReversedFakeItems(), compositions);
                    return true;
                });

        verify(storageMusicDataSource).getChangeObservable();
    }

    @Test
    public void getPlayQueueInInitialStateAndSetNewQueue() {
        when(playQueueDao.getPlayQueue(any())).thenReturn(new PlayQueueLists(getFakeItems(), getReversedFakeItems()));

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(items -> {
                    assertEquals(getFakeItems(), items);
                    return true;
                });

        TestObserver<List<PlayQueueItem>> playQueueObserver = playQueueRepository
                .getPlayQueueObservable()
                .test();

        playQueueRepository.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(playQueueDao).insertNewPlayQueue(getFakeCompositions());
        verify(storageMusicDataSource, times(1)).getChangeObservable();

        playQueueObserver.assertValueAt(1, getFakeItems());
    }


    @Test
    public void setRandomPlayingDisabledTest() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<List<PlayQueueItem>> playQueueObserver = playQueueRepository
                .getPlayQueueObservable()
                .test();

        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(false);

        playQueueRepository.setRandomPlayingEnabled(false);

        playQueueObserver.assertValueCount(2);
    }

    @Test
    public void setRandomPlayingEnabledTest() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<List<PlayQueueItem>> playQueueObserver = playQueueRepository
                .getPlayQueueObservable()
                .test();

        TestObserver<PlayQueueEvent> currentCompositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueRepository.setRandomPlayingEnabled(true);

        verify(playQueueDao).moveShuffledPositionToTop(new PlayQueueItem(0, fakeComposition(0)));
        playQueueObserver.assertValueCount(2);
        currentCompositionObserver.assertValueCount(1);
    }

    @Test
    public void skipToNext() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<PlayQueueEvent> compositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        playQueueRepository.skipToNext().subscribe();

        compositionObserver.assertValues(currentItem(0), currentItem(1));
    }

    @Test
    public void skipToNextFromInitialState() {
        when(storageMusicDataSource.getCompositionById(anyLong())).thenReturn(fakeComposition(1));
        when(playQueueDao.getPlayQueue(any())).thenReturn(new PlayQueueLists(getFakeItems(), getReversedFakeItems()));
        when(uiStatePreferences.getCurrentPlayQueueId()).thenReturn(1L);
        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(1L);

        TestObserver<PlayQueueEvent> compositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        playQueueRepository.skipToNext().subscribe();

        compositionObserver.assertValues(currentItem(1), currentItem(2));
    }

    @Test
    public void skipToPrevious() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<PlayQueueEvent> compositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        playQueueRepository.skipToNext()
                .flatMap(pos -> playQueueRepository.skipToPrevious())
                .subscribe();

        compositionObserver.assertValues(currentItem(0), currentItem(1), currentItem(0));
    }

    @Test
    public void skipToPositionTest() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<PlayQueueEvent> compositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        playQueueRepository.skipToPosition(1000).subscribe();

        compositionObserver.assertValues(currentItem(0), currentItem(1000));
    }

    @Test
    public void testDeletedChanges() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<PlayQueueEvent> compositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        Composition unexcitedComposition = new Composition();
        unexcitedComposition.setId(2000000);
        List<Composition> deletedCompositions = Arrays.asList(fakeComposition(0),
                fakeComposition(1),
                unexcitedComposition);

        changeSubject.onNext(new Change<>(DELETED, deletedCompositions));

        List<PlayQueueItem> expectedList = getFakeItems();
        expectedList.remove(0);
        expectedList.remove(0);

        inOrder.verify(playQueueDao).deleteCompositionsFromQueue(eq(deletedCompositions));

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(expectedList);

        compositionObserver.assertValues(currentItem(0), currentItem(2));
    }

    @Test
    public void testDeletedChangesWithManyItems() {
        List<PlayQueueItem> items = asList(
                new PlayQueueItem(0, fakeComposition(0)),
                new PlayQueueItem(1, fakeComposition(1)),
                new PlayQueueItem(2, fakeComposition(1)),
                new PlayQueueItem(3, fakeComposition(2)),
                new PlayQueueItem(4, fakeComposition(3)));

        when(playQueueDao.insertNewPlayQueue(any())).thenReturn(
                new PlayQueueLists(items, items));

        List<PlayQueueItem> expectedList = new ArrayList<>(items);
        expectedList.remove(1);
        expectedList.remove(1);
        expectedList.remove(1);

        playQueueRepository.setPlayQueue(Arrays.asList(
                fakeComposition(0),
                fakeComposition(1),
                fakeComposition(1),
                fakeComposition(2),
                fakeComposition(3)), 2).subscribe();

        TestObserver<PlayQueueEvent> compositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        List<Composition> deletedCompositions = asList(fakeComposition(1), fakeComposition(2));

        changeSubject.onNext(new Change<>(DELETED, deletedCompositions));

        inOrder.verify(playQueueDao).deleteCompositionsFromQueue(eq(deletedCompositions));

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(expectedList);

        compositionObserver.assertValues(currentItem(2, 1), currentItem(4, 3));
    }

    @Test
    public void testDeleteChangeWithShuffledPlayQueue() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        playQueueRepository.setRandomPlayingEnabled(true);

        List<PlayQueueItem> items = playQueueRepository.getPlayQueueObservable().blockingFirst();

        List<PlayQueueItem> expectedList = new ArrayList<>(items);
        expectedList.remove(0);

        changeSubject.onNext(new Change<>(DELETED, singletonList(fakeComposition(0))));

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(expectedList);
    }

    @Test
    public void testAllDeletedChange() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<PlayQueueEvent> compositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        changeSubject.onNext(new Change<>(DELETED, getFakeCompositions()));

        compositionObserver.assertValues(currentItem(0), new PlayQueueEvent(null));

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(emptyList());
    }

    @Test
    public void testModifyChanges() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<PlayQueueEvent> compositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        Composition changedComposition = fakeComposition(0);
        changedComposition.setTitle("changed title");

        Composition unexcitedComposition = new Composition();
        unexcitedComposition.setId(2000000);
        changeSubject.onNext(new Change<>(MODIFY, Arrays.asList(changedComposition,
                fakeComposition(1),
                unexcitedComposition)
        ));

        playQueueRepository
                .getPlayQueueObservable()
                .test()
                .assertValue(list -> {
                    assertEquals("changed title", list.get(0).getComposition().getTitle());
                    return true;
                });

        compositionObserver.assertValues(currentItem(0), currentItem(0));

        compositionObserver.assertValueAt(1, event -> {
            assert event.getPlayQueueItem() != null;
            assertEquals("changed title", event.getPlayQueueItem().getComposition().getTitle());
            return true;
        });
    }

    @Test
    public void testDeleteItem() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<PlayQueueEvent> compositionObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        TestObserver<List<PlayQueueItem>> playQueueObserver = playQueueRepository
                .getPlayQueueObservable()
                .test();

        playQueueRepository.removeQueueItem(fakeItem(0)).subscribe();

        verify(playQueueDao).deleteItem(0L);

        compositionObserver.assertValues(currentItem(0), currentItem(1));

        playQueueObserver.assertValueAt(1, list -> {
            assertEquals(getFakeCompositions().size() - 1, list.size());
            assertEquals(fakeItem(1), list.get(0));
            return true;
        });

    }

    @Test
    public void swapItemsTest() {
        when(playQueueDao.getPlayQueue(any())).thenReturn(new PlayQueueLists(getFakeItems(), getReversedFakeItems()));
        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(list -> {
                    assertEquals(fakeItem(0), list.get(0));
                    assertEquals(fakeItem(1), list.get(1));
                    return true;
                });
        playQueueRepository.swapItems(fakeItem(0), 0, fakeItem(1), 1)
                .subscribe();

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(list -> {
                    assertEquals(fakeItem(0), list.get(0));
                    assertEquals(fakeItem(1), list.get(1));
                    return true;
                });
    }

    @Test//not clear data(doesn't have unique ids)
    public void addCompositionsToNextTest() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<PlayQueueEvent> itemObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        itemObserver.assertValueAt(0, item -> {
            assertEquals(fakeItem(0), item.getPlayQueueItem());
            return true;
        });

        when(playQueueDao.addCompositionsToQueue(any(), anyInt(), anyInt()))
                .thenReturn(asList(
                        fakeItem(2),
                        fakeItem(3)
                        )
                );

        playQueueRepository.addCompositionsToPlayNext(asList(fakeComposition(2), fakeComposition(3))).subscribe();
        playQueueRepository.skipToNext().subscribe();

        itemObserver.assertValueAt(1, item -> {
            assertEquals(fakeComposition(2), requireNonNull(item.getPlayQueueItem()).getComposition());
            return true;
        });
    }

    @Test
    public void addCompositionsToEndTest() {
        playQueueRepository.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<PlayQueueEvent> itemObserver = playQueueRepository.getCurrentQueueItemObservable()
                .test();

        itemObserver.assertValueAt(0, item -> {
            assertEquals(fakeItem(0), item.getPlayQueueItem());
            return true;
        });

        when(playQueueDao.addCompositionsToQueue(any()))
                .thenReturn(asList(
                        fakeItem(2),
                        fakeItem(3)
                        )
                );

        playQueueRepository.addCompositionsToEnd(asList(fakeComposition(2), fakeComposition(3))).subscribe();
        playQueueRepository.skipToPrevious().subscribe();

        itemObserver.assertValueAt(1, item -> {
            assertEquals(fakeComposition(3), requireNonNull(item.getPlayQueueItem()).getComposition());
            return true;
        });
    }
}