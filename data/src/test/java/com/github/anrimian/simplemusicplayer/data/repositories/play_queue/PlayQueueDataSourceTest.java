package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.database.models.PlayQueueEntity;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositions;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlayQueueDataSourceTest {

    private final PlayQueueDao playQueueDao = mock(PlayQueueDao.class);
    private final StorageMusicDataSource storageMusicDataSource = mock(StorageMusicDataSource.class);
    private final SettingsPreferences settingsPreferences = mock(SettingsPreferences.class);
    private final Scheduler scheduler = Schedulers.trampoline();

    private PlayQueueDataSourceNew playQueueDataSource;

    @Before
    public void setUp() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(false);

        playQueueDataSource = new PlayQueueDataSourceNew(playQueueDao,
                storageMusicDataSource,
                settingsPreferences,
                scheduler);
    }

    @Test
    public void setPlayQueueInNormalMode() {
        playQueueDataSource.setPlayQueue(getFakeCompositions())
                .andThen(playQueueDataSource.getPlayQueue())
                .test()
                .assertValue(compositions -> {
                    assertEquals(getFakeCompositions(), compositions);
                    return true;
                });

        verify(playQueueDao).deletePlayQueue();
        verify(playQueueDao).setPlayQueue(anyListOf(PlayQueueEntity.class));
    }

    @Test
    public void setPlayQueueInShuffleMode() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(true);

        playQueueDataSource.setPlayQueue(getFakeCompositions())
                .andThen(playQueueDataSource.getPlayQueue())
                .test()
                .assertValue(compositions -> {
                    assertNotEquals(getFakeCompositions(), compositions);
                    assertEquals(getFakeCompositions().size(), compositions.size());
                    return true;
                });
    }

    @Test
    public void getPlayQueueInInitialState() {
        when(playQueueDao.getPlayQueue()).thenReturn(getPlayQueueEntities());

        playQueueDataSource.getPlayQueue()
                .test()
                .assertValue(compositions -> {
                    assertEquals(getPlayQueueEntities().size(), compositions.size());
                    return true;
                });
    }

    private List<PlayQueueEntity> getPlayQueueEntities() {
        List<PlayQueueEntity> playQueueEntities = new ArrayList<>();
        return playQueueEntities;
    }
}