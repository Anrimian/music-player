package com.github.anrimian.musicplayer.domain.interactors.player;

import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.fakeCompositionSource;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.GAIN;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS_SHORTLY;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS_TRANSIENT;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.IGNORED;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class PlayerInteractorTest {

    private final MusicPlayerController musicPlayerController = mock(MusicPlayerController.class);
    private final SettingsRepository settingsRepository = mock(SettingsRepository.class);
    private final SystemMusicController systemMusicController = mock(SystemMusicController.class);
    private final SystemServiceController systemServiceController = mock(SystemServiceController.class);

    private PlayerInteractor musicPlayerInteractor;

    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private final PublishSubject<AudioFocusEvent> audioFocusSubject = PublishSubject.create();
    private final PublishSubject<Object> noisyAudioSubject = PublishSubject.create();
    private final PublishSubject<Integer> volumeSubject = PublishSubject.create();

    private TestObserver<PlayerState> playerStateSubscriber;

    private final InOrder inOrder = Mockito.inOrder(
            musicPlayerController,
            systemServiceController);

    @BeforeEach
    public void setUp() {
        when(musicPlayerController.getEventsObservable()).thenReturn(playerEventSubject);

        when(systemMusicController.requestAudioFocus()).thenReturn(audioFocusSubject);
        when(systemMusicController.getAudioBecomingNoisyObservable()).thenReturn(noisyAudioSubject);
        when(systemMusicController.getVolumeObservable()).thenReturn(volumeSubject);

        when(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()).thenReturn(true);
        when(settingsRepository.isPauseOnAudioFocusLossEnabled()).thenReturn(true);

        musicPlayerInteractor = new PlayerInteractor(musicPlayerController,
                settingsRepository,
                systemMusicController,
                systemServiceController);

        playerStateSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();
    }

    @Test
    public void prepareToPlayTest() {
        musicPlayerInteractor.prepareToPlay(fakeCompositionSource(0));

        verify(musicPlayerController).prepareToPlay(eq(fakeCompositionSource(0)));
        verify(musicPlayerController, never()).resume();
        playerStateSubscriber.assertValues(IDLE);
    }

    @Test
    public void pauseTest() {
        musicPlayerInteractor.play();
        musicPlayerInteractor.pause();

        verify(musicPlayerController).resume(anyInt());
        verify(systemServiceController).stopMusicService();
        verify(musicPlayerController).pause();
        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onCompositionIgnoredErrorTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.startPlaying(composition);

        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition));

        playerEventSubject.onNext(new PreparedEvent(composition));
        playerEventSubject.onNext(new ErrorEvent(IGNORED, composition));

        verify(systemServiceController).stopMusicService();
        inOrder.verify(musicPlayerController).pause();

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition));

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void onAudioFocusNotGainedTest() {
        when(systemMusicController.requestAudioFocus()).thenReturn(null);

        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.startPlaying(composition);

        verify(musicPlayerController, never()).resume();
        playerStateSubscriber.assertValues(IDLE);
    }

    @Test
    public void onAudioFocusLossTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume(anyInt());

        audioFocusSubject.onNext(LOSS);

        inOrder.verify(systemServiceController).stopMusicService();
        inOrder.verify(musicPlayerController).pause();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onAudioFocusLossTransientAndGainTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume(anyInt());

        audioFocusSubject.onNext(LOSS_TRANSIENT);

        inOrder.verify(musicPlayerController).pause();

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void onAudioFocusLossTransientAndPauseAndGainTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume(anyInt());

        audioFocusSubject.onNext(LOSS_TRANSIENT);

        inOrder.verify(musicPlayerController).pause();

        musicPlayerInteractor.pause();

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(systemServiceController).stopMusicService();
        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onAudioFocusLossShortlyAndGainTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume(anyInt());

        audioFocusSubject.onNext(LOSS_SHORTLY);

        inOrder.verify(musicPlayerController).setVolume(0.5f);

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).setVolume(1f);

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioFocusLossShortlyAndGainWithoutDecreaseVolumeTest() {
        when(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()).thenReturn(false);

        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume(anyInt());

        audioFocusSubject.onNext(LOSS_SHORTLY);

        inOrder.verify(musicPlayerController, never()).setVolume(0.5f);

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).setVolume(1f);

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioFocusLossAndGainWithoutDecreaseVolumeTest() {
        when(settingsRepository.isPauseOnAudioFocusLossEnabled()).thenReturn(false);

        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        audioFocusSubject.onNext(LOSS_TRANSIENT);

        inOrder.verify(musicPlayerController, never()).pause();

        audioFocusSubject.onNext(GAIN);

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioBecomingNoisyTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume(anyInt());

        noisyAudioSubject.onNext(new Object());

        inOrder.verify(musicPlayerController).pause();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onAudioFocusLossThenBecomingNoisyAndGainFocusTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume(anyInt());

        audioFocusSubject.onNext(LOSS_TRANSIENT);

        inOrder.verify(musicPlayerController).pause();

        noisyAudioSubject.onNext(new Object());
        audioFocusSubject.onNext(GAIN);

        inOrder.verify(systemServiceController, times(1)).stopMusicService();
        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onAudioBecomingNoisyThenLossFocusAndGainFocusTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume(anyInt());

        noisyAudioSubject.onNext(new Object());

        inOrder.verify(musicPlayerController).pause();

        audioFocusSubject.onNext(LOSS_TRANSIENT);
        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void testVolumeSetToSilentWhilePlay() {
        when(settingsRepository.isPauseOnZeroVolumeLevelEnabled()).thenReturn(true);

        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume(anyInt());

        volumeSubject.onNext(0);

        inOrder.verify(systemServiceController).stopMusicService();
        inOrder.verify(musicPlayerController).pause();
    }

}