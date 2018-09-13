package com.github.anrimian.musicplayer.data.database.dao;

import com.github.anrimian.musicplayer.data.utils.TestDataProvider;
import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.PlayQueueEntity;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 02.07.2018.
 */
public class PlayQueueDaoWrapperTest {

    private PlayQueueDao playQueueDao = mock(PlayQueueDao.class);
    private AppDatabase appDatabase = mock(AppDatabase.class);

    private PlayQueueDaoWrapper daoWrapper = new PlayQueueDaoWrapper(appDatabase, playQueueDao);

    @Test
    public void getPlayQueueWithUnexcitingCompositions() {
        ArrayList<PlayQueueEntity> entities = new ArrayList<>();
        PlayQueueEntity playQueueEntity = new PlayQueueEntity();
        playQueueEntity.setAudioId(Long.MAX_VALUE);
        playQueueEntity.setPosition(0);
        entities.add(playQueueEntity);

        when(playQueueDao.getPlayQueue()).thenReturn(entities);

        List<PlayQueueItem> items = daoWrapper.getPlayQueue(TestDataProvider::getFakeCompositionsMap);
        assertEquals(0, items.size());
        verify(playQueueDao).deleteItem(eq(Long.MAX_VALUE));

    }
}