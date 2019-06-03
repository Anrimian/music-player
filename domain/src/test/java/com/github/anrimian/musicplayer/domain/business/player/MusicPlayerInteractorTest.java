package com.github.anrimian.musicplayer.domain.business.player;

import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.business.TestBusinessDataProvider.currentItem;
import static com.github.anrimian.musicplayer.domain.business.TestBusinessDataProvider.getFakeCompositions;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.GAIN;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS_SHORTLY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.STOP;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.DELETED;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.UNKNOWN;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 17.04.2018.
 */
public class MusicPlayerInteractorTest {

    private MusicPlayerController musicPlayerController = mock(MusicPlayerController.class);
    private SettingsRepository settingsRepository = mock(SettingsRepository.class);
    private PlayQueueRepository playQueueRepository = mock(PlayQueueRepository.class);
    private MusicProviderRepository musicProviderRepository = mock(MusicProviderRepository.class);
    private SystemMusicController systemMusicController = mock(SystemMusicController.class);
    private SystemServiceController systemServiceController = mock(SystemServiceController.class);
    private Analytics analytics = mock(Analytics.class);
    private PlayerErrorParser playerErrorParser = mock(PlayerErrorParser.class);

    private MusicPlayerInteractor musicPlayerInteractor;

    private PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private BehaviorSubject<PlayQueueEvent> currentCompositionSubject = BehaviorSubject.createDefault(currentItem(0));
    private PublishSubject<AudioFocusEvent> audioFocusSubject = PublishSubject.create();
    private PublishSubject<Object> noisyAudioSubject = PublishSubject.create();
    private PublishSubject<Integer> volumeSubject = PublishSubject.create();

    private TestObserver<PlayerState> playerStateSubscriber;

    private InOrder inOrder = Mockito.inOrder(playQueueRepository,
            musicPlayerController,
            musicProviderRepository,
            systemServiceController);

    @Before
    public void setUp() {
        when(playQueueRepository.setPlayQueue(any())).thenReturn(Completable.complete());
        when(playQueueRepository.setPlayQueue(any(), anyInt())).thenReturn(Completable.complete());
        when(playQueueRepository.getCurrentQueueItemObservable())
                .thenReturn(currentCompositionSubject);
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(1));
        when(playQueueRepository.skipToPrevious()).thenReturn(Single.just(1));

        when(musicPlayerController.getEventsObservable()).thenReturn(playerEventSubject);

        when(musicProviderRepository.writeErrorAboutComposition(any(), any()))
                .thenReturn(Completable.complete());
        when(musicProviderRepository.deleteComposition(any())).thenReturn(Completable.complete());

        when(systemMusicController.requestAudioFocus()).thenReturn(audioFocusSubject);
        when(systemMusicController.getAudioBecomingNoisyObservable()).thenReturn(noisyAudioSubject);
        when(systemMusicController.getVolumeObservable()).thenReturn(volumeSubject);

        when(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()).thenReturn(true);

        musicPlayerInteractor = new MusicPlayerInteractor(musicPlayerController,
                settingsRepository,
                systemMusicController,
                systemServiceController,
                playQueueRepository,
                musicProviderRepository,
                analytics,
                playerErrorParser);

        playerStateSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();
    }

    @Test
    public void startPlayingTest() {
        musicPlayerInteractor.startPlaying(getFakeCompositions(), 0);

        verify(playQueueRepository).setPlayQueue(getFakeCompositions(), 0);
        verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        verify(musicPlayerController).resume();
        playerStateSubscriber.assertValues(IDLE, LOADING, PLAY);
    }

    @Test
    public void playWithoutPreparingTest() {
        musicPlayerInteractor.play();

        verify(musicPlayerController).prepareToPlay(any(), anyLong());
        verify(musicPlayerController).resume();
        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onPlayFinishedTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        playerEventSubject.onNext(new FinishedEvent());
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(1)), anyLong());

        playerEventSubject.onNext(new PreparedEvent());

        inOrder.verify(musicPlayerController).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onPlayToEndTest() {
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(0));
        
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        playerEventSubject.onNext(new FinishedEvent());
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        inOrder.verify(musicPlayerController, never()).resume();
        inOrder.verify(musicPlayerController).pause();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onPlayToEndWithRepeatPlayListModeTest() {
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(0));
        when(settingsRepository.getRepeatMode()).thenReturn(RepeatMode.REPEAT_PLAY_LIST);

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        playerEventSubject.onNext(new FinishedEvent());
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(1)), anyLong());

        playerEventSubject.onNext(new PreparedEvent());
        inOrder.verify(musicPlayerController).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void pauseTest() {
        musicPlayerInteractor.play();
        musicPlayerInteractor.pause();

        verify(musicPlayerController).resume();
        verify(musicPlayerController).prepareToPlay(any(), anyLong());
        verify(musicPlayerController).pause();
        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onCompositionPlayFinishedInStopState() {
        musicPlayerInteractor.play();
        musicPlayerInteractor.stop();

        inOrder.verify(musicPlayerController).prepareToPlay(any(), anyLong());
        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(musicPlayerController).stop();

        playerEventSubject.onNext(new FinishedEvent());

        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(1)), anyLong());
        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, STOP);
    }

    @Test
    public void onCompositionUnknownErrorTest() {
        when(playerErrorParser.getErrorType(any())).thenReturn(UNKNOWN);

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());

        Throwable throwable = new IllegalStateException();
        playerEventSubject.onNext(new ErrorEvent(throwable));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(UNKNOWN, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(1)), anyLong());

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onCompositionUnknownErrorInEndTest() {
        when(playerErrorParser.getErrorType(any())).thenReturn(UNKNOWN);
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(0));

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        Throwable throwable = new IllegalStateException();
        playerEventSubject.onNext(new ErrorEvent(throwable));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(UNKNOWN, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(1)), anyLong());
        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, STOP);
    }

    @Test
    public void onCompositionDeletedErrorTest() {
        when(playerErrorParser.getErrorType(any())).thenReturn(DELETED);

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        Throwable throwable = new IllegalStateException();
        playerEventSubject.onNext(new ErrorEvent(throwable));
        currentCompositionSubject.onNext(currentItem(1));

        inOrder.verify(musicProviderRepository).deleteComposition(getFakeCompositions().get(0));

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioFocusNotGainedTest() {
        when(systemMusicController.requestAudioFocus()).thenReturn(null);

        musicPlayerInteractor.play();

        verify(musicPlayerController, never()).resume();
        playerStateSubscriber.assertValues(IDLE);
    }

    @Test
    public void onAudioFocusLossAndGainTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        audioFocusSubject.onNext(LOSS);

        inOrder.verify(musicPlayerController).pause();

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(systemServiceController).startMusicService();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void onAudioFocusLossShortlyAndGainTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        audioFocusSubject.onNext(LOSS_SHORTLY);

        inOrder.verify(musicPlayerController).setVolume(0.5f);

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).setVolume(1f);

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioFocusLossShortlyAndGainWithoutDecreaseVolumeTest() {
        when(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()).thenReturn(false);

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        audioFocusSubject.onNext(LOSS_SHORTLY);

        inOrder.verify(musicPlayerController, never()).setVolume(0.5f);

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).setVolume(1f);

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioBecomingNoisyTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        noisyAudioSubject.onNext(new Object());

        inOrder.verify(musicPlayerController).pause();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onAudioFocusLossThenBecomingNoisyAndGainFocusTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        audioFocusSubject.onNext(LOSS);

        inOrder.verify(musicPlayerController).pause();

        noisyAudioSubject.onNext(new Object());
        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onAudioBecomingNoisyThenLossFocusAndGainFocusTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        noisyAudioSubject.onNext(new Object());

        inOrder.verify(musicPlayerController).pause();

        audioFocusSubject.onNext(LOSS);
        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onCurrentCompositionDeletedChangeTest() {
        musicPlayerInteractor.play();

        currentCompositionSubject.onNext(new PlayQueueEvent(null));

        verify(musicPlayerController).stop();
        playerStateSubscriber.assertValues(IDLE, PLAY, STOP);
    }

    @Test
    public void skipToPreviousTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        when(settingsRepository.getSkipConstraintMillis()).thenReturn(15);
        when(musicPlayerController.getTrackPosition()).thenReturn(10L);

        musicPlayerInteractor.skipToPrevious();

        inOrder.verify(playQueueRepository).skipToPrevious();

        when(settingsRepository.getSkipConstraintMillis()).thenReturn(15);
        when(musicPlayerController.getTrackPosition()).thenReturn(30L);

        musicPlayerInteractor.skipToPrevious();

        inOrder.verify(musicPlayerController).seekTo(0);
    }

    @Test
    public void testVolumeSetToSilentWhilePlay() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        volumeSubject.onNext(0);

        inOrder.verify(musicPlayerController).pause();
    }
}