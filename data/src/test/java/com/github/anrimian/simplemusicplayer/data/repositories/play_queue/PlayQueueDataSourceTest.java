package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.database.models.PlayQueueEntity;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeableMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositionsMap;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.DELETED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.MODIFY;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.mockito.internal.verification.VerificationModeFactory.atMost;

public class PlayQueueDataSourceTest {

    private final PlayQueueDao playQueueDao = mock(PlayQueueDao.class);
    private final StorageMusicDataSource storageMusicDataSource = mock(StorageMusicDataSource.class);
    private final SettingsPreferences settingsPreferences = mock(SettingsPreferences.class);
    private final Scheduler scheduler = Schedulers.trampoline();

    private final PublishSubject<Change<List<Composition>>> changeSubject = PublishSubject.create();

    private InOrder inOrder = Mockito.inOrder(playQueueDao);

    private PlayQueueDataSource playQueueDataSource = new PlayQueueDataSource(playQueueDao,
            storageMusicDataSource,
            settingsPreferences,
            scheduler);

    private TestObserver<Change<List<Composition>>> changeObserver = playQueueDataSource
            .getChangeObservable()
            .test();

    @Before
    public void setUp() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(false);

        when(storageMusicDataSource.getCompositionsMap()).thenReturn(getFakeCompositionsMap());
        when(storageMusicDataSource.getChangeObservable()).thenReturn(changeSubject);
    }

    @Test
    public void setPlayQueueInNormalMode() {
        playQueueDataSource.setPlayQueue(getFakeCompositions())
                .test()
                .assertValue(compositions -> {
                    assertEquals(getFakeCompositions(), compositions);
                    return true;
                });

        verify(playQueueDao).deletePlayQueue();
        verify(playQueueDao).setPlayQueue(anyListOf(PlayQueueEntity.class));
        verify(storageMusicDataSource).getChangeObservable();
    }

    @Test
    public void setPlayQueueInShuffleMode() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueDataSource.setPlayQueue(getFakeCompositions())
                .test()
                .assertValue(compositions -> {
                    assertNotEquals(getFakeCompositions(), compositions);
                    assertEquals(getFakeCompositions().size(), compositions.size());
                    return true;
                });

        verify(storageMusicDataSource).getChangeObservable();
    }

    @Test
    public void getEmptyPlayQueueInInitialState() {
        when(playQueueDao.getPlayQueue()).thenReturn(emptyList());

        playQueueDataSource.getPlayQueue()
                .test()
                .assertValue(compositions -> {
                    assertEquals(0, compositions.size());
                    return true;
                });

        verify(storageMusicDataSource, never()).getChangeObservable();
    }

    @Test
    public void getPlayQueueObservableInInitialState() {
        when(playQueueDao.getPlayQueue()).thenReturn(getPlayQueueEntities());

        playQueueDataSource.getPlayQueueObservable()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getPlayQueueEntities().size(), compositions.size());
                    assertEquals(getFakeCompositions(), compositions);
                    return true;
                });

        verify(storageMusicDataSource).getChangeObservable();
    }

    @Test
    public void getPlayQueueInInitialState() {
        when(playQueueDao.getPlayQueue()).thenReturn(getPlayQueueEntities());

        playQueueDataSource.getPlayQueue()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getPlayQueueEntities().size(), compositions.size());
                    assertEquals(getFakeCompositions(), compositions);
                    return true;
                });

        verify(storageMusicDataSource).getChangeObservable();
    }

    @Test
    public void getPlayQueueInInitialStateAndSetNewQueue() {
        when(playQueueDao.getPlayQueue()).thenReturn(getPlayQueueEntities());

        playQueueDataSource.getPlayQueue()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getPlayQueueEntities().size(), compositions.size());
                    assertEquals(getFakeCompositions(), compositions);
                    return true;
                });

        TestObserver<List<Composition>> playQueueObserver = playQueueDataSource
                .getPlayQueueObservable()
                .test();

        playQueueDataSource.setPlayQueue(getFakeCompositions())
                .test()
                .assertValue(compositions -> {
                    assertEquals(getFakeCompositions(), compositions);
                    return true;
                });

        verify(playQueueDao).deletePlayQueue();
        verify(playQueueDao).setPlayQueue(anyListOf(PlayQueueEntity.class));

        verify(storageMusicDataSource, times(1)).getChangeObservable();

        playQueueObserver.assertValueAt(1, getFakeCompositions());
    }

    @Test
    public void getPlayQueueWithUnexcitingCompositions() {
        PlayQueueEntity playQueueEntity = new PlayQueueEntity();
        playQueueEntity.setId(Long.MAX_VALUE);
        playQueueEntity.setPosition(0);
        playQueueEntity.setShuffledPosition(0);
        when(playQueueDao.getPlayQueue()).thenReturn(singletonList(playQueueEntity));

        playQueueDataSource.getPlayQueue()
                .test()
                .assertValue(compositions -> {
                    assertEquals(0, compositions.size());
                    return true;
                });

        verify(playQueueDao).deletePlayQueueEntity(eq(Long.MAX_VALUE));
        verify(storageMusicDataSource, never()).getChangeObservable();
    }

    @Test
    public void setRandomPlayingDisabledTest() {
        playQueueDataSource.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<List<Composition>> playQueueObserver = playQueueDataSource
                .getPlayQueueObservable()
                .test();

        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(false);

        playQueueDataSource.setRandomPlayingEnabled(false, getFakeCompositions().get(1))
                .test()
                .assertValue(1);

        playQueueObserver.assertValueCount(2);
    }

    @Test
    public void setRandomPlayingEnabledTest() {
        playQueueDataSource.setPlayQueue(getFakeCompositions()).subscribe();

        TestObserver<List<Composition>> playQueueObserver = playQueueDataSource
                .getPlayQueueObservable()
                .test();

        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        Composition composition = getFakeCompositions().get(1);
        playQueueDataSource.setRandomPlayingEnabled(true, composition)
                .test()
                .assertValue(0);

        playQueueDataSource.getPlayQueue()
                .flatMapObservable(Observable::fromIterable)
                .test()
                .assertComplete();

        verify(playQueueDao).updatePlayQueue(anyListOf(PlayQueueEntity.class));
        playQueueObserver.assertValueCount(2);
    }

    @Test
    public void testDeletedChanges() {
        playQueueDataSource.setPlayQueue(getFakeCompositions()).subscribe();

        Composition unexcitedComposition = new Composition();
        unexcitedComposition.setId(2000000);
        changeSubject.onNext(new Change<>(DELETED, Arrays.asList(getFakeCompositions().get(0),
                getFakeCompositions().get(1),
                unexcitedComposition)
        ));
        verify(playQueueDao).deletePlayQueueEntity(eq(0L));
        verify(playQueueDao, atLeastOnce()).updateShuffledPosition(anyLong(), anyInt());
        verify(playQueueDao, atLeastOnce()).updatePosition(anyLong(), anyInt());
        verify(playQueueDao).deletePlayQueueEntity(eq(1L));
        verify(playQueueDao, atLeastOnce()).updateShuffledPosition(anyLong(), anyInt());
        verify(playQueueDao, atLeastOnce()).updatePosition(anyLong(), anyInt());

        changeObserver.assertValue(change -> {
            assertEquals(DELETED, change.getChangeType());
            assertEquals(getFakeCompositions().get(0), change.getData().get(0));
            assertEquals(getFakeCompositions().get(1), change.getData().get(1));
            assertEquals(2, change.getData().size());
            return true;
        });

        playQueueDataSource.getPlayQueue()
                .test()
                .assertValue(list -> {
                    assertEquals(getFakeCompositions().get(2), list.get(0));
                    assertEquals(getFakeCompositions().size() - 2, list.size());
                    return true;
                });

        playQueueDataSource
                .getPlayQueueObservable()
                .test()
                .assertValue(list -> {
                    assertEquals(getFakeCompositions().get(2), list.get(0));
                    assertEquals(getFakeCompositions().size() - 2, list.size());
                    return true;
                });
    }

    @Test
    public void testModifyChanges() {
        playQueueDataSource.setPlayQueue(getFakeCompositions()).subscribe();

        Composition changedComposition = getFakeCompositions().get(0);
        changedComposition.setTitle("changed title");

        Composition unexcitedComposition = new Composition();
        unexcitedComposition.setId(2000000);
        changeSubject.onNext(new Change<>(MODIFY, Arrays.asList(changedComposition,
                getFakeCompositions().get(1),
                unexcitedComposition)
        ));

        changeObserver.assertValue(change -> {
            assertEquals(MODIFY, change.getChangeType());
            assertEquals("changed title", change.getData().get(0).getTitle());
            assertEquals(getFakeCompositions().get(0), change.getData().get(0));
            assertEquals(getFakeCompositions().get(1), change.getData().get(1));
            assertEquals(2, change.getData().size());
            return true;
        });

        playQueueDataSource.getPlayQueue()
                .test()
                .assertValue(list -> {
                    assertEquals("changed title", list.get(0).getTitle());
                    return true;
                });

        playQueueDataSource
                .getPlayQueueObservable()
                .test()
                .assertValue(list -> {
                    assertEquals("changed title", list.get(0).getTitle());
                    return true;
                });
    }

    private List<PlayQueueEntity> getPlayQueueEntities() {
        List<PlayQueueEntity> playQueueEntities = new ArrayList<>();
        List<Composition> compositions = getFakeCompositions();
        for (int i = 0; i < 100000; i++) {
            Composition composition = compositions.get(i);
            PlayQueueEntity playQueueEntity = new PlayQueueEntity();
            playQueueEntity.setId(composition.getId());
            playQueueEntity.setPosition(i);
            playQueueEntity.setShuffledPosition(compositions.size() - 1 - i);

            playQueueEntities.add(playQueueEntity);
        }
        return playQueueEntities;
    }
}