package com.github.anrimian.musicplayer.domain.interactors.player;

import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.currentItem;
import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.fakeCompositionSource;
import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.getFakeCompositions;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.NOT_FOUND;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.UNKNOWN;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class LibraryPlayerInteractorTest {

    private final PlayerCoordinatorInteractor playerCoordinatorInteractor = mock(PlayerCoordinatorInteractor.class);
    private final SettingsRepository settingsRepository = mock(SettingsRepository.class);
    private final PlayQueueRepository playQueueRepository = mock(PlayQueueRepository.class);
    private final LibraryRepository musicProviderRepository = mock(LibraryRepository.class);
    private final UiStateRepository uiStateRepository = mock(UiStateRepository.class);
    private final Analytics analytics = mock(Analytics.class);

    private LibraryPlayerInteractor libraryPlayerInteractor;

    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private final BehaviorSubject<PlayQueueEvent> currentCompositionSubject = BehaviorSubject.createDefault(currentItem(0));

    private final InOrder inOrder = Mockito.inOrder(playQueueRepository,
            playerCoordinatorInteractor,
            musicProviderRepository);

    @Before
    public void setUp() {
        when(playQueueRepository.setPlayQueue(any())).thenReturn(Completable.complete());
        when(playQueueRepository.setPlayQueue(any(), anyInt())).thenReturn(Completable.complete());
        when(playQueueRepository.getCurrentQueueItemObservable())
                .thenReturn(currentCompositionSubject);
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(1));

        when(playerCoordinatorInteractor.getPlayerEventsObservable(any())).thenReturn(playerEventSubject);
        when(playerCoordinatorInteractor.getActualTrackPosition(any())).thenReturn(Single.just(0L));

        when(musicProviderRepository.writeErrorAboutComposition(any(), any()))
                .thenReturn(Completable.complete());
        when(musicProviderRepository.deleteComposition(any())).thenReturn(Completable.complete());

        when(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()).thenReturn(true);
        when(settingsRepository.isPauseOnAudioFocusLossEnabled()).thenReturn(true);

        libraryPlayerInteractor = new LibraryPlayerInteractor(
                playerCoordinatorInteractor,
                settingsRepository,
                playQueueRepository,
                musicProviderRepository,
                uiStateRepository,
                analytics
        );
    }

    @Test
    public void startPlayingTest() {
        libraryPlayerInteractor.startPlaying(getFakeCompositions(), 0);

        verify(playQueueRepository).setPlayQueue(getFakeCompositions(), 0);
        verify(playerCoordinatorInteractor).prepareToPlay(eq(fakeCompositionSource(0)), any());
    }

    @Test
    public void playWithoutPreparingTest() {
        libraryPlayerInteractor.play();

        verify(playerCoordinatorInteractor).prepareToPlay(any(), any());
    }

    @Test
    public void onPlayFinishedTest() {
        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(composition), any());

        playerEventSubject.onNext(new FinishedEvent(composition));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        CompositionSource nextComposition = fakeCompositionSource(1);
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(nextComposition), any());

        playerEventSubject.onNext(new PreparedEvent(nextComposition));
    }

    @Test
    public void onPlayToEndTest() {
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(0));

        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(composition), any());

        playerEventSubject.onNext(new FinishedEvent(composition));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        inOrder.verify(playerCoordinatorInteractor).stop(any());
    }

    @Test
    public void onPlayToEndWithRepeatPlayListModeTest() {
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(0));
        when(settingsRepository.getRepeatMode()).thenReturn(RepeatMode.REPEAT_PLAY_LIST);

        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(composition), any());

        playerEventSubject.onNext(new FinishedEvent(composition));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        CompositionSource nextComposition = fakeCompositionSource(1);
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(nextComposition), any());
    }

    @Test
    public void onCompositionPlayFinishedInStopState() {
        libraryPlayerInteractor.play();
        libraryPlayerInteractor.stop();

        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(any(), any());
        inOrder.verify(playerCoordinatorInteractor).stop(any());

        CompositionSource composition = fakeCompositionSource(0);
        playerEventSubject.onNext(new FinishedEvent(composition));

        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));

        CompositionSource nextComposition = fakeCompositionSource(1);
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(nextComposition), any());
    }

    @Test
    public void onCompositionUnknownErrorTest() {
        libraryPlayerInteractor.play();

        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(composition), any());

        playerEventSubject.onNext(new ErrorEvent(UNKNOWN, composition));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(CorruptionType.UNKNOWN, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));

        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(fakeCompositionSource(1)), any());
    }

    @Test
    public void onCompositionUnknownErrorInEndTest() {
        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(composition), any());

        playerEventSubject.onNext(new ErrorEvent(UNKNOWN, composition));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(CorruptionType.UNKNOWN, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();

        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(true));

        currentCompositionSubject.onNext(currentItem(1));
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(fakeCompositionSource(1)), any());

        playerEventSubject.onNext(new ErrorEvent(UNKNOWN, composition));
        inOrder.verify(playerCoordinatorInteractor, never()).play(any(), anyInt());
    }

    @Test
    public void onCompositionDeletedErrorTest() {
        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(composition), any());

        playerEventSubject.onNext(new ErrorEvent(NOT_FOUND, composition));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(CorruptionType.NOT_FOUND, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();
    }

    @Test
    public void onCurrentCompositionDeletedChangeTest() {
        libraryPlayerInteractor.play();

        currentCompositionSubject.onNext(new PlayQueueEvent(null));

        verify(playerCoordinatorInteractor).reset(eq(PlayerType.LIBRARY));
    }

    @Test
    public void skipToPreviousTest() {
        libraryPlayerInteractor.play();

        inOrder.verify(playerCoordinatorInteractor).prepareToPlay(eq(fakeCompositionSource(0)), any());

        when(settingsRepository.getSkipConstraintMillis()).thenReturn(15);
        when(playerCoordinatorInteractor.getActualTrackPosition(any())).thenReturn(Single.just(10L));
        libraryPlayerInteractor.skipToPrevious();
        inOrder.verify(playQueueRepository).skipToPrevious();

        when(settingsRepository.getSkipConstraintMillis()).thenReturn(15);
        when(playerCoordinatorInteractor.getActualTrackPosition(any())).thenReturn(Single.just(30L));
        libraryPlayerInteractor.skipToPrevious();
        inOrder.verify(playerCoordinatorInteractor).onSeekFinished(eq(0L), any());
    }

}