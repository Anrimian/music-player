package com.github.anrimian.musicplayer.data.database.dao;

import com.github.anrimian.musicplayer.data.TestDataProvider;
import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.PlayQueueEntity;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

import static com.github.anrimian.musicplayer.data.TestDataProvider.getFakeCompositionsMap;
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

        when(playQueueDao.getPlayQueueObservable()).thenReturn(Flowable.just(entities));

        daoWrapper.getPlayQueueObservable(TestDataProvider::getFakeCompositionsMap)
                .test()
                .assertValue(compositions -> {
                    assertEquals(0, compositions.size());
                    verify(playQueueDao).deleteComposition(eq(Long.MAX_VALUE));
                    return true;
                });

    }
}