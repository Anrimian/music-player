package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.domain.business.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 17.04.2018.
 */
public class MusicPlayerInteractorNewTest {

    private MusicPlayerController musicPlayerController = mock(MusicPlayerController.class);
    private PlayQueueRepository playQueueRepository = mock(PlayQueueRepository.class);

    private MusicPlayerInteractorNew musicPlayerInteractor;

    private PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        when(playQueueRepository.setPlayQueue(any())).thenReturn(Completable.complete());
        when(playQueueRepository.getCurrentComposition())
                .thenReturn(Single.just(getFakeCompositions().get(0)));

        when(musicPlayerController.prepareToPlay(any())).thenReturn(Completable.complete());
        when(musicPlayerController.getEventsObservable()).thenReturn(playerEventSubject);

        musicPlayerInteractor = new MusicPlayerInteractorNew(musicPlayerController,
                playQueueRepository);
    }

    @Test
    public void startPlayingWithOneCompositionTest() {
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.startPlaying(getFakeCompositions().get(0))
                .test()
                .assertComplete();

        verify(playQueueRepository).setPlayQueue(singletonList(getFakeCompositions().get(0)));
        verify(playQueueRepository).getCurrentComposition();
        verify(musicPlayerController).prepareToPlay(getFakeCompositions().get(0));
        verify(musicPlayerController).resume();
        testSubscriber.assertValues(IDLE, LOADING, PLAY);
    }

    @Test
    public void startPlayingWithOneCompositionErrorTest() {
        when(musicPlayerController.prepareToPlay(any()))
                .thenReturn(Completable.error(new IllegalStateException()));

        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.startPlaying(getFakeCompositions().get(0))
                .test()
                .assertError(IllegalStateException.class);

        verify(playQueueRepository).setPlayQueue(singletonList(getFakeCompositions().get(0)));
        verify(playQueueRepository).getCurrentComposition();
        verify(musicPlayerController).prepareToPlay(getFakeCompositions().get(0));
        verify(musicPlayerController).stop();
        testSubscriber.assertValues(IDLE, LOADING, STOP);
    }

    @Test
    public void startPlayingTest() {
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.startPlaying(getFakeCompositions())
                .test()
                .assertComplete();

        verify(playQueueRepository).setPlayQueue(getFakeCompositions());
        verify(playQueueRepository).getCurrentComposition();
        verify(musicPlayerController).prepareToPlayIgnoreError(getFakeCompositions().get(0));
        verify(musicPlayerController).resume();
        testSubscriber.assertValues(IDLE, LOADING, PLAY);
    }

    @Test
    public void playTest() {
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.play()
                .test()
                .assertComplete();

        verify(playQueueRepository).getCurrentComposition();
        verify(musicPlayerController).prepareToPlayIgnoreError(any());
        verify(musicPlayerController).resume();
        testSubscriber.assertValues(IDLE, LOADING, PLAY);
    }

}