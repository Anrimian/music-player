package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.CurrentPlayListInfo;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.UiStateRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;
import static io.reactivex.Single.just;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 12.11.2017.
 */
public class MusicPlayerInteractorTest {

    private MusicPlayerInteractor musicPlayerInteractor;
    private MusicPlayerController musicPlayerController;
    private SystemMusicController systemMusicController;

    private SettingsRepository settingsRepository;
    private UiStateRepository uiStateRepository;
    private PlayListRepository playQueueRepository;

    private TestObserver<PlayerState> playerStateTestObserver = new TestObserver<>();
    private TestObserver<Composition> currentCompositionTestObserver = new TestObserver<>();
    private TestObserver<List<Composition>> currentPlayListTestObserver = new TestObserver<>();
    private TestObserver<Long> trackStateTestObserver = new TestObserver<>();

    private Composition one = new Composition();
    private Composition two = new Composition();
    private Composition three = new Composition();
    private Composition four = new Composition();

    private List<Composition> fakeCompositions = new ArrayList<>();

    private PublishSubject<InternalPlayerState> internalPlayerStateObservable = PublishSubject.create();
    private PublishSubject<Long> trackStateObservable = PublishSubject.create();
    private PublishSubject<AudioFocusEvent> audioFocusSubject = PublishSubject.create();

    @Before
    public void setUp() throws Exception {
        one.setFilePath("root/music/one");
        one.setId(1);
        fakeCompositions.add(one);

        two.setFilePath("root/music/two");
        two.setId(2);
        fakeCompositions.add(two);

        three.setFilePath("root/music/old/three");
        three.setId(3);
        fakeCompositions.add(three);

        four.setFilePath("root/music/old/to delete/four");
        four.setId(4);
        fakeCompositions.add(four);

        musicPlayerController = mock(MusicPlayerController.class);
        when(musicPlayerController.getPlayerStateObservable()).thenReturn(internalPlayerStateObservable);
        when(musicPlayerController.getTrackPositionObservable()).thenReturn(trackStateObservable);
        when(musicPlayerController.prepareToPlay(any())).thenReturn(Completable.complete());

        settingsRepository = mock(SettingsRepository.class);
        when(settingsRepository.isInfinitePlayingEnabled()).thenReturn(false);
        when(settingsRepository.isRandomPlayingEnabled()).thenReturn(false);

        uiStateRepository = mock(UiStateRepository.class);
        when(uiStateRepository.getPlayListPosition()).thenReturn(0);
        when(uiStateRepository.getTrackPosition()).thenReturn(0L);

        playQueueRepository = mock(PlayListRepository.class);
        when(playQueueRepository.getCurrentPlayList()).thenReturn(just(new CurrentPlayListInfo(new ArrayList<>(), new ArrayList<>())));
        when(playQueueRepository.setCurrentPlayList(any())).thenReturn(Completable.complete());

        systemMusicController = mock(SystemMusicController.class);
        when(systemMusicController.getAudioFocusObservable()).thenReturn(audioFocusSubject);
        when(systemMusicController.requestAudioFocusOld()).thenReturn(true);

        musicPlayerInteractor = new MusicPlayerInteractor(musicPlayerController,
                systemMusicController,
                settingsRepository,
                uiStateRepository,
                playQueueRepository);
        musicPlayerInteractor.getPlayerStateObservable().subscribe(playerStateTestObserver);
        musicPlayerInteractor.getCurrentCompositionObservable().subscribe(currentCompositionTestObserver);
        musicPlayerInteractor.getCurrentPlayListObservable().subscribe(currentPlayListTestObserver);
        musicPlayerInteractor.getTrackPositionObservable().subscribe(trackStateTestObserver);
    }

    @Test
    public void startPlayingTest() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);

        playerStateTestObserver.assertValues(IDLE, PLAY);
        currentCompositionTestObserver.assertValues(one);
        currentPlayListTestObserver.assertValue(fakeCompositions);
        verify(musicPlayerController).prepareToPlay(eq(one));
        verify(musicPlayerController).resume();
    }


    @Test
    public void startPlayingWithoutAudioFocusTest() throws Exception {
        when(systemMusicController.requestAudioFocusOld()).thenReturn(false);

        musicPlayerInteractor.startPlaying(fakeCompositions);

        playerStateTestObserver.assertValues(IDLE, PLAY);
        currentCompositionTestObserver.assertValues(one);
        currentPlayListTestObserver.assertValue(fakeCompositions);
        verify(musicPlayerController).prepareToPlay(eq(one));
        verify(musicPlayerController, never()).resume();
    }

    @Test
    public void emptyPlayTest() throws Exception {
        musicPlayerInteractor.play();

        verify(musicPlayerController, never()).resume();
        playerStateTestObserver.assertValues(IDLE);
    }

    @Test
    public void playAfterPauseTest() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);
        musicPlayerInteractor.pause();
        musicPlayerInteractor.play();

        verify(musicPlayerController, times(2)).resume();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void playAfterStopTest() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);
        musicPlayerInteractor.stop();
        musicPlayerInteractor.play();

        verify(musicPlayerController, times(2)).prepareToPlay(eq(one));
        playerStateTestObserver.assertValues(IDLE, PLAY, STOP, PLAY);
    }

    @Test
    public void stopTest() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);
        musicPlayerInteractor.stop();

        verify(musicPlayerController).stop();
        playerStateTestObserver.assertValues(IDLE, PLAY, STOP);
    }

    @Test
    public void pauseTest() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);
        musicPlayerInteractor.pause();

        verify(musicPlayerController).pause();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void testSkipToNext() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);

        musicPlayerInteractor.skipToNext();
        verify(musicPlayerController).prepareToPlay(eq(two));
    }

    @Test
    public void testSkipToPrevious() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);

        musicPlayerInteractor.skipToPrevious();
        verify(musicPlayerController).prepareToPlay(eq(four));
    }

    @Test
    public void testSkipToNextInPauseState() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);
        musicPlayerInteractor.pause();
        musicPlayerInteractor.skipToNext();
        musicPlayerInteractor.play();

        verify(musicPlayerController).prepareToPlay(eq(two));
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void testAutoPlayWithoutRepeat() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);

        internalPlayerStateObservable.onNext(InternalPlayerState.ENDED);
        internalPlayerStateObservable.onNext(InternalPlayerState.ENDED);
        internalPlayerStateObservable.onNext(InternalPlayerState.ENDED);
        internalPlayerStateObservable.onNext(InternalPlayerState.ENDED);
        internalPlayerStateObservable.onNext(InternalPlayerState.ENDED);
        internalPlayerStateObservable.onNext(InternalPlayerState.ENDED);
        internalPlayerStateObservable.onNext(InternalPlayerState.ENDED);

        verify(musicPlayerController, times(4)).prepareToPlay(any());
    }

    @Test
    public void testRandomPlaying() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);

        when(settingsRepository.isRandomPlayingEnabled()).thenReturn(true);
        musicPlayerInteractor.setRandomPlayingEnabled(true);

        musicPlayerInteractor.skipToNext();
        musicPlayerInteractor.skipToPrevious();
        verify(musicPlayerController, times(2)).prepareToPlay(eq(one));
    }

    @Test
    public void playOnlyOneTwoTimesTest() throws Exception {
        musicPlayerInteractor.startPlaying(Collections.singletonList(one));

        internalPlayerStateObservable.onNext(InternalPlayerState.ENDED);

        musicPlayerInteractor.play();

        verify(musicPlayerController, times(2)).prepareToPlay(eq(one));
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void testShuffleInitialPlayList() throws Exception {
        when(playQueueRepository.getCurrentPlayList()).thenReturn(just(new CurrentPlayListInfo(fakeCompositions, fakeCompositions)));
        when(playQueueRepository.setCurrentPlayList(any())).thenReturn(Completable.complete());

        when(settingsRepository.isRandomPlayingEnabled()).thenReturn(true);

        musicPlayerInteractor = new MusicPlayerInteractor(musicPlayerController,
                systemMusicController,
                settingsRepository,
                uiStateRepository,
                playQueueRepository);
        TestObserver<PlayerState> playerStateTestObserver = musicPlayerInteractor.getPlayerStateObservable().test();

        musicPlayerInteractor.setRandomPlayingEnabled(true);

        musicPlayerInteractor.play();
        verify(musicPlayerController).prepareToPlay(eq(one));

        playerStateTestObserver.assertValues(PAUSE, PLAY);
    }

    @Test
    public void testPlayCompositionWhenPlayingList() throws Exception {
        when(settingsRepository.isRandomPlayingEnabled()).thenReturn(true);

        musicPlayerInteractor.startPlaying(fakeCompositions);

        musicPlayerInteractor.skipToNext();
        musicPlayerInteractor.skipToNext();

        musicPlayerInteractor.startPlaying(Collections.singletonList(one));

        playerStateTestObserver.assertValues(IDLE, PLAY);
        verify(musicPlayerController, times(4)).prepareToPlay(any());
    }

    @Test
    public void audioFocusNormalEventsTest() throws Exception {
        startPlayingTest();

        InOrder inOrder = inOrder(musicPlayerController);

        audioFocusSubject.onNext(AudioFocusEvent.LOSS);
        inOrder.verify(musicPlayerController).pause();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE);

        audioFocusSubject.onNext(AudioFocusEvent.GAIN);
        inOrder.verify(musicPlayerController).resume();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE, PLAY);

        audioFocusSubject.onNext(AudioFocusEvent.LOSS_SHORTLY);
        inOrder.verify(musicPlayerController).pause();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE, PLAY, PAUSE);

        audioFocusSubject.onNext(AudioFocusEvent.GAIN);
        inOrder.verify(musicPlayerController).resume();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void audioFocusWithNoisyTestTest() throws Exception {//TODO think about this case
        startPlayingTest();

        InOrder inOrder = inOrder(musicPlayerController);

        audioFocusSubject.onNext(AudioFocusEvent.LOSS);
        inOrder.verify(musicPlayerController).pause();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE);

        musicPlayerInteractor.onAudioBecomingNoisy();

        audioFocusSubject.onNext(AudioFocusEvent.GAIN);
        inOrder.verify(musicPlayerController, never()).resume();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE);

        musicPlayerInteractor.play();
        inOrder.verify(musicPlayerController).resume();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE, PLAY);

        audioFocusSubject.onNext(AudioFocusEvent.LOSS_SHORTLY);
        inOrder.verify(musicPlayerController).pause();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE, PLAY, PAUSE);

        audioFocusSubject.onNext(AudioFocusEvent.GAIN);
        inOrder.verify(musicPlayerController).resume();
        playerStateTestObserver.assertValues(IDLE, PLAY, PAUSE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void moveToComposition() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);

        musicPlayerInteractor.moveToComposition(three);
        verify(musicPlayerController).prepareToPlay(eq(three));
    }

}
