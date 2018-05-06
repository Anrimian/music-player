package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.dao.CompositionsDao;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;

import org.junit.Before;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.mock;

public class PlayQueueDataSourceTest {

    private final CompositionsDao compositionsDao = mock(CompositionsDao.class);
    private final SettingsPreferences settingsPreferences = mock(SettingsPreferences.class);
    private final Scheduler scheduler = Schedulers.trampoline();

    private PlayQueueDataSource playQueueDataSource;

    @Before
    public void setUp() {
        playQueueDataSource = new PlayQueueDataSource(compositionsDao,
                settingsPreferences,
                scheduler);
    }
}