package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueLists;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 02.07.2018.
 */
public class PlayListsDaoWrapperTest {

    private final PlayQueueDao playQueueDao = mock(PlayQueueDao.class);
    private final AppDatabase appDatabase = mock(AppDatabase.class);

    private final PlayQueueDaoWrapper daoWrapper = new PlayQueueDaoWrapper(
            appDatabase, playQueueDao);

    @Test
    public void insertNewPlayQueue() {
        ArrayList<Composition> list = new ArrayList<>();
        list.add(fakeComposition(0));
        list.add(fakeComposition(1));
        list.add(fakeComposition(2));

        ArrayList<Composition> shuffledList = new ArrayList<>(list);
        long randomSeed = System.nanoTime();
        Collections.shuffle(shuffledList, new Random(randomSeed));

        List<Long> ids = asList(1L, 2L, 3L);
        List<Long> shuffledIds = new ArrayList<>(ids);
        Collections.shuffle(shuffledIds, new Random(randomSeed));

        when(playQueueDao.insertItems(any())).thenReturn(ids);

        PlayQueueLists queueLists = daoWrapper.insertNewPlayQueue(list);

        List<PlayQueueItem> items = queueLists.getQueue();
        assertEquals(list.get(0), items.get(0).getComposition());
        assertEquals(list.get(1), items.get(1).getComposition());
        assertEquals(list.get(2), items.get(2).getComposition());
        assertEquals((long) ids.get(0), items.get(0).getId());
        assertEquals((long) ids.get(1), items.get(1).getId());
        assertEquals((long) ids.get(2), items.get(2).getId());

        List<PlayQueueItem> shuffledItems = queueLists.getShuffledQueue();
        assertEquals(shuffledList.get(0), shuffledItems.get(0).getComposition());
        assertEquals(shuffledList.get(1), shuffledItems.get(1).getComposition());
        assertEquals(shuffledList.get(2), shuffledItems.get(2).getComposition());
        assertEquals((long) shuffledIds.get(0), shuffledItems.get(0).getId());
        assertEquals((long) shuffledIds.get(1), shuffledItems.get(1).getId());
        assertEquals((long) shuffledIds.get(2), shuffledItems.get(2).getId());
    }
}