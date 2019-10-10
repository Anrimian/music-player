package com.github.anrimian.musicplayer.data.repositories.play_queue;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueCompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueLists;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.data.repositories.settings.SettingsRepositoryImpl;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.fakeComposition;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getFakeItems;
import static com.github.anrimian.musicplayer.data.utils.TestDataProvider.getReversedFakeItems;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlayQueueRepositoryImplTest {

    private final PlayQueueDaoWrapper playQueueDao = mock(PlayQueueDaoWrapper.class);
    private final SettingsRepositoryImpl settingsPreferences = mock(SettingsRepositoryImpl.class);
    private final UiStatePreferences uiStatePreferences = mock(UiStatePreferences.class);

    private final PublishSubject<List<PlayQueueCompositionEntity>> playQueueDaoSubject = PublishSubject.create();
    private final BehaviorSubject<Boolean> randomModeObservable = BehaviorSubject.createDefault(false);

    private PlayQueueRepository playQueueRepository = new PlayQueueRepositoryImpl(playQueueDao,
            settingsPreferences,
            uiStatePreferences,
            Schedulers.trampoline());

    @Before
    public void setUp() {
        when(settingsPreferences.isRandomPlayingEnabled()).thenReturn(false);
        when(settingsPreferences.getRandomPlayingObservable()).thenReturn(randomModeObservable);

        when(playQueueDao.insertNewPlayQueue(any())).thenReturn(
                new PlayQueueLists(getFakeItems(), getReversedFakeItems()));
        when(playQueueDao.getPlayQueueObservable())
                .thenReturn(playQueueDaoSubject);

        when(uiStatePreferences.getCurrentPlayQueueId()).thenReturn(NO_COMPOSITION);
        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(NO_COMPOSITION);
    }

    @Test
    public void setPlayQueueInNormalMode() {
        playQueueRepository.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(playQueueDao).insertNewPlayQueue(getFakeCompositions());

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
    }
}