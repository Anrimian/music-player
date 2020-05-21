package com.github.anrimian.musicplayer.domain.interactors.player;

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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.currentItem;
import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.fakeCompositionSource;
import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.getFakeCompositions;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.NOT_FOUND;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.UNKNOWN;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LibraryPlayerInteractorTest {

    private PlayerInteractor musicPlayerInteractor = mock(PlayerInteractor.class);
    private SettingsRepository settingsRepository = mock(SettingsRepository.class);
    private PlayQueueRepository playQueueRepository = mock(PlayQueueRepository.class);
    private LibraryRepository musicProviderRepository = mock(LibraryRepository.class);
    private Analytics analytics = mock(Analytics.class);

    private LibraryPlayerInteractor libraryPlayerInteractor;

    private PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private BehaviorSubject<PlayQueueEvent> currentCompositionSubject = BehaviorSubject.createDefault(currentItem(0));

    private InOrder inOrder = Mockito.inOrder(playQueueRepository,
            musicPlayerInteractor,
            musicProviderRepository);

    @Before
    public void setUp() {
        when(playQueueRepository.setPlayQueue(any())).thenReturn(Completable.complete());
        when(playQueueRepository.setPlayQueue(any(), anyInt())).thenReturn(Completable.complete());
        when(playQueueRepository.getCurrentQueueItemObservable())
                .thenReturn(currentCompositionSubject);
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(1));

        when(musicPlayerInteractor.getPlayerEventsObservable()).thenReturn(playerEventSubject);

        when(musicProviderRepository.writeErrorAboutComposition(any(), any()))
                .thenReturn(Completable.complete());
        when(musicProviderRepository.deleteComposition(any())).thenReturn(Completable.complete());

        when(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()).thenReturn(true);

        libraryPlayerInteractor = new LibraryPlayerInteractor(
                musicPlayerInteractor,
                settingsRepository,
                playQueueRepository,
                musicProviderRepository,
                analytics
        );
    }

    @Test
    public void startPlayingTest() {
        libraryPlayerInteractor.startPlaying(getFakeCompositions(), 0);

        verify(playQueueRepository).setPlayQueue(getFakeCompositions(), 0);
        verify(musicPlayerInteractor).prepareToPlay(eq(fakeCompositionSource(0)), anyLong());
    }

    @Test
    public void playWithoutPreparingTest() {
        libraryPlayerInteractor.play();

        verify(musicPlayerInteractor).prepareToPlay(any(), anyLong());
    }

    @Test
    public void onPlayFinishedTest() {
        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(composition), anyLong());

        playerEventSubject.onNext(new FinishedEvent(composition));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        CompositionSource nextComposition = fakeCompositionSource(1);
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(nextComposition), anyLong());

        playerEventSubject.onNext(new PreparedEvent(nextComposition));
    }

    @Test
    public void onPlayToEndTest() {
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(0));

        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(composition), anyLong());

        playerEventSubject.onNext(new FinishedEvent(composition));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        inOrder.verify(musicPlayerInteractor).stop();
    }

    @Test
    public void onPlayToEndWithRepeatPlayListModeTest() {
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(0));
        when(settingsRepository.getRepeatMode()).thenReturn(RepeatMode.REPEAT_PLAY_LIST);

        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(composition), anyLong());

        playerEventSubject.onNext(new FinishedEvent(composition));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        CompositionSource nextComposition = fakeCompositionSource(1);
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(nextComposition), anyLong());
    }

    @Test
    public void onCompositionPlayFinishedInStopState() {
        libraryPlayerInteractor.play();
        libraryPlayerInteractor.stop();

        inOrder.verify(musicPlayerInteractor).prepareToPlay(any(), anyLong());
        inOrder.verify(musicPlayerInteractor).stop();

        CompositionSource composition = fakeCompositionSource(0);
        playerEventSubject.onNext(new FinishedEvent(composition));

        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));

        CompositionSource nextComposition = fakeCompositionSource(1);
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(nextComposition), anyLong());
    }

    @Test
    public void onCompositionUnknownErrorTest() {
        libraryPlayerInteractor.play();

        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(composition), anyLong());

        playerEventSubject.onNext(new ErrorEvent(UNKNOWN, composition));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(CorruptionType.UNKNOWN, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));

        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(fakeCompositionSource(1)), anyLong());
    }

    @Test
    public void onCompositionUnknownErrorInEndTest() {
        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(composition), anyLong());

        playerEventSubject.onNext(new ErrorEvent(UNKNOWN, composition));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(CorruptionType.UNKNOWN, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();

        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(true));

        currentCompositionSubject.onNext(currentItem(1));
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(fakeCompositionSource(1)), anyLong());

        playerEventSubject.onNext(new ErrorEvent(UNKNOWN, composition));
        inOrder.verify(musicPlayerInteractor, never()).play();
    }

    @Test
    public void onCompositionDeletedErrorTest() {
        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        libraryPlayerInteractor.play();

        CompositionSource composition = fakeCompositionSource(0);
        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(composition), anyLong());

        playerEventSubject.onNext(new ErrorEvent(NOT_FOUND, composition));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(CorruptionType.NOT_FOUND, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();
    }

    @Test
    public void onCurrentCompositionDeletedChangeTest() {
        libraryPlayerInteractor.play();

        currentCompositionSubject.onNext(new PlayQueueEvent(null));

        verify(musicPlayerInteractor).stop();
    }

    @Test
    public void skipToPreviousTest() {
        libraryPlayerInteractor.play();

        inOrder.verify(musicPlayerInteractor).prepareToPlay(eq(fakeCompositionSource(0)), anyLong());

        when(settingsRepository.getSkipConstraintMillis()).thenReturn(15);
        when(musicPlayerInteractor.getTrackPosition()).thenReturn(10L);

        libraryPlayerInteractor.skipToPrevious();

        inOrder.verify(playQueueRepository).skipToPrevious();

        when(settingsRepository.getSkipConstraintMillis()).thenReturn(15);
        when(musicPlayerInteractor.getTrackPosition()).thenReturn(30L);

        libraryPlayerInteractor.skipToPrevious();

        inOrder.verify(musicPlayerInteractor).seekTo(0);
    }

}