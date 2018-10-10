package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDao;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntityNew;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueLists;
import com.github.anrimian.musicplayer.data.utils.TestDataProvider;
import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.queueEntity;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 02.07.2018.
 */
public class PlayQueueDaoWrapperTest {

    private final PlayQueueDao playQueueDao = mock(PlayQueueDao.class);
    private final AppDatabase appDatabase = mock(AppDatabase.class);

    private final PlayQueueDaoWrapper daoWrapper = new PlayQueueDaoWrapper(
            appDatabase, playQueueDao);

    @Test
    public void getPlayQueueNewTest() {
        ArrayList<PlayQueueEntityNew> entities = new ArrayList<>();
        entities.add(queueEntity(1, Long.MAX_VALUE, 0, 2));
        entities.add(queueEntity(2, 1, 1, 3));
        entities.add(queueEntity(3, 2, 30, 8));
        entities.add(queueEntity(4, 3, 2, 1));
        when(playQueueDao.getPlayQueueNew()).thenReturn(entities);

        PlayQueueLists lists = daoWrapper.getPlayQueueNew(TestDataProvider::getFakeCompositionsMap);
        verify(playQueueDao).deleteItemNew(eq(Long.MAX_VALUE));

        List<PlayQueueItem> items = lists.getQueue();
        assertEquals(3, items.size());
        assertEquals(2, items.get(0).getId());
        assertEquals(4, items.get(1).getId());
        assertEquals(3, items.get(2).getId());

        List<PlayQueueItem> shuffledItems = lists.getShuffledQueue();
        assertEquals(3, shuffledItems.size());
        assertEquals(4, shuffledItems.get(0).getId());
        assertEquals(2, shuffledItems.get(1).getId());
        assertEquals(3, shuffledItems.get(2).getId());
    }

    @Test
    public void insertNewPlayQueue() {
        ArrayList<Composition> list = new ArrayList<>();
        list.add(fakeComposition(0));
        list.add(fakeComposition(1));
        list.add(fakeComposition(2));

        ArrayList<Composition> shuffledList = new ArrayList<>(list);
        Random rnd = new Random();
        Collections.shuffle(shuffledList, rnd);

        when(playQueueDao.insertPlayQueueNew(any())).thenReturn(new long[]{1L, 2L, 3L});

        PlayQueueLists queueLists = daoWrapper.insertNewPlayQueue(list, shuffledList, rnd);

        List<PlayQueueItem> items = queueLists.getQueue();
        assertEquals(list.get(0), items.get(0).getComposition());
        assertEquals(list.get(1), items.get(1).getComposition());
        assertEquals(list.get(2), items.get(2).getComposition());

        List<PlayQueueItem> shuffledItems = queueLists.getShuffledQueue();
        assertEquals(shuffledList.get(0), shuffledItems.get(0).getComposition());
        assertEquals(shuffledList.get(1), shuffledItems.get(1).getComposition());
        assertEquals(shuffledList.get(2), shuffledItems.get(2).getComposition());
    }
}