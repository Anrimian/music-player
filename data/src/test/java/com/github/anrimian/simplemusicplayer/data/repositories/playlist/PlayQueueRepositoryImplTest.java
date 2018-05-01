package com.github.anrimian.simplemusicplayer.data.repositories.playlist;

import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static com.github.anrimian.simplemusicplayer.data.repositories.TestDataProvider.getFakeCompositions;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 16.04.2018.
 */
public class PlayQueueRepositoryImplTest {

    private PlayQueueRepository playQueueRepository;

    private UiStatePreferences uiStatePreferences = mock(UiStatePreferences.class);
    private PlayQueueDataSource playQueueDataSource = mock(PlayQueueDataSource.class);

    @Before
    public void setUp() {
        when(playQueueDataSource.getPlayQueue()).thenReturn(emptyList());
        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(NO_COMPOSITION);

        playQueueRepository = new PlayQueueRepositoryImpl(playQueueDataSource,
                uiStatePreferences,
                Schedulers.trampoline());
    }

    @Test
    public void testPlayQueueObserver() {
        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(emptyList());
    }

    @Test
    public void testPlayQueueObserverWithInitialState() {
        when(playQueueDataSource.getPlayQueue()).thenReturn(getFakeCompositions());

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(getFakeCompositions());
    }

    @Test
    public void testCurrentCompositionObserver() {
        when(playQueueDataSource.getPlayQueue()).thenReturn(getFakeCompositions());
        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(1L);
        when(uiStatePreferences.getCurrentCompositionPosition()).thenReturn(1);

        playQueueRepository.getCurrentCompositionObservable()
                .test()
                .assertValue(getFakeCompositions().get(1));
    }

    @Test
    public void testCurrentCompositionObserverWithInitialState() {
        playQueueRepository.getCurrentCompositionObservable()
                .test()
                .assertNoValues();
    }

    @Test
    public void setPlayQueueTest() {
        TestObserver<List<Composition>> playListObserver = playQueueRepository.getPlayQueueObservable()
                .test();
        TestObserver<Composition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        when(playQueueDataSource.getPlayQueue()).thenReturn(getFakeCompositions());

        playQueueRepository.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(playQueueDataSource).setPlayQueue(getFakeCompositions());
        //noinspection unchecked
        playListObserver.assertValues(emptyList(), getFakeCompositions());
        verify(uiStatePreferences).setCurrentCompositionId(0L);
        verify(uiStatePreferences).setCurrentCompositionPosition(0);
        compositionObserver.assertValue(getFakeCompositions().get(0));
    }

    @Test
    public void setRandomPlayingEnabledTest() {
        when(playQueueDataSource.setRandomPlayingEnabled(anyBoolean(), any())).thenReturn(0);

        setPlayQueueTest();

        TestObserver<List<Composition>> playListObserver = playQueueRepository.getPlayQueueObservable()
                .test();

        playQueueRepository.setRandomPlayingEnabled(true);

        verify(playQueueDataSource).setRandomPlayingEnabled(true, getFakeCompositions().get(0));
        //noinspection unchecked
        playListObserver.assertValues(getFakeCompositions(), getFakeCompositions());
        verify(uiStatePreferences, times(2)).setCurrentCompositionPosition(0);
    }

    @Test
    public void skipToNext() {
        setPlayQueueTest();

        TestObserver<Composition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        playQueueRepository.skipToNext();

        compositionObserver.assertValues(getFakeCompositions().get(0),
                getFakeCompositions().get(1));
    }

    @Test
    public void skipToPrevious() {
        setPlayQueueTest();

        TestObserver<Composition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        playQueueRepository.skipToNext();
        playQueueRepository.skipToPrevious();

        compositionObserver.assertValues(getFakeCompositions().get(0),
                getFakeCompositions().get(1),
                getFakeCompositions().get(0));
    }

    @Test
    public void skipToPositionTest() {
        setPlayQueueTest();

        TestObserver<Composition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        playQueueRepository.skipToPosition(1000);

        compositionObserver.assertValues(getFakeCompositions().get(0),
                getFakeCompositions().get(1000));
    }
}