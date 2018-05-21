package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.currentComposition;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.simplemusicplayer.data.preferences.UiStatePreferences.NO_COMPOSITION;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
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
        when(playQueueDataSource.getPlayQueue()).thenReturn(Single.just(emptyList()));
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
        when(playQueueDataSource.getPlayQueue()).thenReturn(Single.just(getFakeCompositions()));

        playQueueRepository.getPlayQueueObservable()
                .test()
                .assertValue(getFakeCompositions());
    }

    @Test
    public void testCurrentCompositionObserver() {
        when(playQueueDataSource.getPlayQueue()).thenReturn(Single.just(getFakeCompositions()));
        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(1L);
        when(uiStatePreferences.getCurrentCompositionPosition()).thenReturn(1);

        playQueueRepository.getCurrentCompositionObservable()
                .test()
                .assertValue(currentComposition(getFakeCompositions().get(1)));
    }

    @Test
    public void testCurrentCompositionObserverWithEmptyInitialState() {
        playQueueRepository.getCurrentCompositionObservable()
                .test()
                .assertNoValues();
    }

    @Test
    public void testCurrentCompositionObserverWithInitialState() {
        when(playQueueDataSource.getPlayQueue()).thenReturn(Single.just(getFakeCompositions()));
        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(1L);

        playQueueRepository.getCurrentCompositionObservable()
                .test()
                .assertValue(currentComposition(getFakeCompositions().get(1)));
    }

    @Test
    public void setPlayQueueTest() {
        TestObserver<List<Composition>> playListObserver = playQueueRepository.getPlayQueueObservable()
                .test();
        TestObserver<CurrentComposition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        when(playQueueDataSource.getPlayQueue()).thenReturn(Single.just(getFakeCompositions()));
        when(playQueueDataSource.setPlayQueue(any())).thenReturn(Single.just(getFakeCompositions()));


        playQueueRepository.setPlayQueue(getFakeCompositions())
                .test()
                .assertComplete();

        verify(playQueueDataSource).setPlayQueue(getFakeCompositions());
        //noinspection unchecked
        playListObserver.assertValues(emptyList(), getFakeCompositions());
        verify(uiStatePreferences).setCurrentCompositionId(0L);
        verify(uiStatePreferences).setCurrentCompositionPosition(0);
        compositionObserver.assertValue(currentComposition(getFakeCompositions().get(0)));
    }

    @Test
    public void setRandomPlayingEnabledTest() {
        when(playQueueDataSource.setRandomPlayingEnabled(anyBoolean(), any())).thenReturn(Single.just(0));

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
    public void setRandomPlayingEnabledAndSkipToNextTest() {
        when(playQueueDataSource.setRandomPlayingEnabled(anyBoolean(), any())).thenReturn(Single.just(3));

        setPlayQueueTest();

        TestObserver<List<Composition>> playListObserver = playQueueRepository.getPlayQueueObservable()
                .test();
        TestObserver<CurrentComposition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        playQueueRepository.setRandomPlayingEnabled(true);

        verify(playQueueDataSource).setRandomPlayingEnabled(true, getFakeCompositions().get(0));
        //noinspection unchecked
        playListObserver.assertValues(getFakeCompositions(), getFakeCompositions());
        verify(uiStatePreferences, times(2)).setCurrentCompositionPosition(anyInt());


        playQueueRepository.skipToNext().subscribe();

        compositionObserver.assertValues(currentComposition(getFakeCompositions().get(0)),
                currentComposition(getFakeCompositions().get(4)));
    }

    @Test
    public void skipToNext() {
        setPlayQueueTest();

        TestObserver<CurrentComposition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        playQueueRepository.skipToNext().subscribe();

        compositionObserver.assertValues(currentComposition(getFakeCompositions().get(0)),
                currentComposition(getFakeCompositions().get(1)));
    }

    @Test
    public void skipToNextFromInitialState() {
        when(playQueueDataSource.getPlayQueue()).thenReturn(Single.just(getFakeCompositions()));
        when(uiStatePreferences.getCurrentCompositionId()).thenReturn(1L);
        when(uiStatePreferences.getCurrentCompositionPosition()).thenReturn(1);

        TestObserver<CurrentComposition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        playQueueRepository.skipToNext().subscribe();

        compositionObserver.assertValues(currentComposition(getFakeCompositions().get(1)),
                currentComposition(getFakeCompositions().get(2)));
    }

    @Test
    public void skipToPrevious() {
        setPlayQueueTest();

        TestObserver<CurrentComposition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        playQueueRepository.skipToNext()
                .flatMap(pos -> playQueueRepository.skipToPrevious())
                .subscribe();

        compositionObserver.assertValues(currentComposition(getFakeCompositions().get(0)),
                currentComposition(getFakeCompositions().get(1)),
                currentComposition(getFakeCompositions().get(0)));
    }

    @Test
    public void skipToPositionTest() {
        setPlayQueueTest();

        TestObserver<CurrentComposition> compositionObserver = playQueueRepository.getCurrentCompositionObservable()
                .test();

        playQueueRepository.skipToPosition(1000).subscribe();

        compositionObserver.assertValues(currentComposition(getFakeCompositions().get(0)),
                currentComposition(getFakeCompositions().get(1000)));
    }
}