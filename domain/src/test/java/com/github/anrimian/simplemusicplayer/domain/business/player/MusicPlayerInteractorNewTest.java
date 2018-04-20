package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.domain.business.TestDataProvider.getFakeCompositions;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 17.04.2018.
 */
public class MusicPlayerInteractorNewTest {

    private MusicPlayerController musicPlayerController = mock(MusicPlayerController.class);
    private SettingsRepository settingsRepository = mock(SettingsRepository.class);
    private PlayQueueRepository playQueueRepository = mock(PlayQueueRepository.class);

    private MusicPlayerInteractorNew musicPlayerInteractor;

    private PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private BehaviorSubject<Composition> currentCompositionSubject = BehaviorSubject.createDefault(getFakeCompositions().get(0));

    private InOrder inOrder = Mockito.inOrder(playQueueRepository, musicPlayerController);

    @Before
    public void setUp() {
        when(playQueueRepository.setPlayQueue(any())).thenReturn(Completable.complete());
        when(playQueueRepository.getCurrentComposition())
                .thenReturn(Single.just(getFakeCompositions().get(0)));
        when(playQueueRepository.getCurrentCompositionObservable()).thenReturn(currentCompositionSubject);
        when(playQueueRepository.skipToNext()).thenReturn(1);

        when(musicPlayerController.prepareToPlay(any())).thenReturn(Completable.complete());
        when(musicPlayerController.getEventsObservable()).thenReturn(playerEventSubject);

        musicPlayerInteractor = new MusicPlayerInteractorNew(musicPlayerController,
                settingsRepository,
                playQueueRepository);
    }

    @Test
    public void startPlayingTest() {
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.startPlaying(getFakeCompositions())
                .test()
                .assertComplete();

        verify(playQueueRepository).setPlayQueue(getFakeCompositions());
        verify(musicPlayerController).prepareToPlayIgnoreError(getFakeCompositions().get(0));
        verify(musicPlayerController).resume();
        testSubscriber.assertValues(IDLE, LOADING, PLAY);
    }

    @Test
    public void playWithoutPreparingTest() {
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.play();

        verify(musicPlayerController).prepareToPlayIgnoreError(any());
        verify(musicPlayerController).resume();
        testSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onPlayFinishedTest() {
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(musicPlayerController).prepareToPlayIgnoreError(getFakeCompositions().get(0));

        playerEventSubject.onNext(new FinishedEvent());
        currentCompositionSubject.onNext(getFakeCompositions().get(1));

        inOrder.verify(playQueueRepository).skipToNext();
        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(musicPlayerController).prepareToPlayIgnoreError(getFakeCompositions().get(1));

        testSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onPlayToEndTest() {
        when(playQueueRepository.skipToNext()).thenReturn(0);
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(musicPlayerController).prepareToPlayIgnoreError(getFakeCompositions().get(0));

        playerEventSubject.onNext(new FinishedEvent());
        currentCompositionSubject.onNext(getFakeCompositions().get(1));

        inOrder.verify(playQueueRepository).skipToNext();
        inOrder.verify(musicPlayerController, never()).resume();
        inOrder.verify(musicPlayerController).stop();

        testSubscriber.assertValues(IDLE, PLAY, STOP);
    }

    @Test
    public void onPlayToEndWithInfiniteModeTest() {
        when(playQueueRepository.skipToNext()).thenReturn(0);
        when(settingsRepository.isInfinitePlayingEnabled()).thenReturn(true);
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(musicPlayerController).prepareToPlayIgnoreError(getFakeCompositions().get(0));

        playerEventSubject.onNext(new FinishedEvent());
        currentCompositionSubject.onNext(getFakeCompositions().get(1));

        inOrder.verify(playQueueRepository).skipToNext();
        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(musicPlayerController).prepareToPlayIgnoreError(getFakeCompositions().get(1));

        testSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void pauseTest() {
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.play();
        musicPlayerInteractor.pause();

        verify(musicPlayerController).resume();
        verify(musicPlayerController).prepareToPlayIgnoreError(any());
        verify(musicPlayerController).pause();
        testSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onCompositionPlayFinishedInStopState() {
        TestObserver<PlayerState> testSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();

        musicPlayerInteractor.play();
        musicPlayerInteractor.stop();

        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(musicPlayerController).prepareToPlayIgnoreError(any());
        inOrder.verify(musicPlayerController).stop();

        playerEventSubject.onNext(new FinishedEvent());
        currentCompositionSubject.onNext(getFakeCompositions().get(1));
//
        inOrder.verify(playQueueRepository, never()).skipToNext();
        inOrder.verify(musicPlayerController, never()).resume();
        inOrder.verify(musicPlayerController, never()).prepareToPlayIgnoreError(getFakeCompositions().get(1));
        testSubscriber.assertValues(IDLE, PLAY, STOP);
    }
}