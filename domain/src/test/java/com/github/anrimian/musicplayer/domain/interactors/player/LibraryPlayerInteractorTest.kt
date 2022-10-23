package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.content.AcceptablePlayerException
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.kotlin.*

class LibraryPlayerInteractorTest {

    private val playerCoordinatorInteractor: PlayerCoordinatorInteractor = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val playQueueRepository: PlayQueueRepository = mock()
    private val musicProviderRepository: LibraryRepository = mock()
    private val uiStateRepository: UiStateRepository = mock()
    private val analytics: Analytics = mock()
    
    private lateinit var libraryPlayerInteractor: LibraryPlayerInteractor

    private val testComposition: Composition = mock()
    private val testLibrarySource: LibraryCompositionSource = mock {
        on { composition } doReturn testComposition
    }
    private val playQueueItem: PlayQueueItem = mock {
        on { composition } doReturn testComposition
    }
    private val testPlayQueueEvent: PlayQueueEvent = mock {
        on { playQueueItem } doReturn playQueueItem
    }

    private val playerEventSubject = PublishSubject.create<PlayerEvent>()
    private val playerStateSubject = PublishSubject.create<PlayerState>()
    private val currentCompositionSubject = BehaviorSubject.createDefault(testPlayQueueEvent)

    private val inOrder = Mockito.inOrder(
        playerCoordinatorInteractor,
        settingsRepository,
        playQueueRepository,
        musicProviderRepository,
        uiStateRepository,
        analytics
    )

    @BeforeEach
    fun setUp() {
        whenever(playQueueRepository.setPlayQueue(any())).thenReturn(Completable.complete())
        whenever(playQueueRepository.setPlayQueue(any(), anyInt())).thenReturn(Completable.complete())
        whenever(playQueueRepository.currentQueueItemObservable).thenReturn(currentCompositionSubject)
        whenever(playQueueRepository.skipToNext()).thenReturn(Single.just(1))

        whenever(playerCoordinatorInteractor.getPlayerEventsObservable(any())).thenReturn(playerEventSubject)
        whenever(playerCoordinatorInteractor.getPlayerStateObservable(any())).thenReturn(playerStateSubject)
        whenever(playerCoordinatorInteractor.getActualTrackPosition(any())).thenReturn(Single.just(0L))
        whenever(playerCoordinatorInteractor.getIsPlayingStateObservable(any()))
            .thenReturn(Observable.just(false))

        whenever(musicProviderRepository.writeErrorAboutComposition(any(), any()))
            .thenReturn(Completable.complete())
        whenever(musicProviderRepository.deleteComposition(any()))
            .thenReturn(Completable.complete())

        whenever(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled).thenReturn(true)
        whenever(settingsRepository.isPauseOnAudioFocusLossEnabled).thenReturn(true)

        libraryPlayerInteractor = LibraryPlayerInteractor(
            playerCoordinatorInteractor,
            settingsRepository,
            playQueueRepository,
            musicProviderRepository,
            uiStateRepository,
            analytics
        )
    }

    @Test
    fun `start playing test`() {
        val queue: List<Composition> = mock()
        libraryPlayerInteractor.startPlaying(queue, 0)

        playQueueRepository.setPlayQueue(eq(queue), eq(0))

        verify(playerCoordinatorInteractor).playAfterPrepare(eq(PlayerType.LIBRARY))
    }

    @Test
    fun `on finished event received`() {
        playerEventSubject.onNext(PlayerEvent.FinishedEvent(testLibrarySource))

        verify(playQueueRepository).skipToNext()
    }

    @Test
    fun `on finished event received with last item in queue`() {
        whenever(playQueueRepository.skipToNext()).thenReturn(Single.just(0))

        playerEventSubject.onNext(PlayerEvent.FinishedEvent(testLibrarySource))
        verify(playQueueRepository).skipToNext()
        verify(playerCoordinatorInteractor).stop(eq(PlayerType.LIBRARY))
    }

    @Test
    fun `on finished event received with last item in queue with repeat mode`() {
        whenever(playQueueRepository.skipToNext()).thenReturn(Single.just(0))
        whenever(settingsRepository.repeatMode).thenReturn(RepeatMode.REPEAT_PLAY_LIST)

        playerEventSubject.onNext(PlayerEvent.FinishedEvent(testLibrarySource))
        verify(playQueueRepository).skipToNext()
        verify(playerCoordinatorInteractor, never()).stop(eq(PlayerType.LIBRARY))
    }

    @Test
    fun `on error event test`() {
        whenever(playQueueRepository.isCurrentCompositionAtEndOfQueue)
            .thenReturn(Single.just(false))

        playerEventSubject.onNext(PlayerEvent.ErrorEvent(mock<Exception>(), testLibrarySource))

        verify(musicProviderRepository).writeErrorAboutComposition(
            eq(CorruptionType.UNKNOWN),
            eq(testComposition)
        )
        verify(playQueueRepository).skipToNext()
    }

    @Test
    fun `on error event at the end of the queue`() {
        whenever(playQueueRepository.isCurrentCompositionAtEndOfQueue)
            .thenReturn(Single.just(true))

        playerEventSubject.onNext(PlayerEvent.ErrorEvent(mock<Exception>(), testLibrarySource))

        verify(playQueueRepository, never()).skipToNext()
        verify(playerCoordinatorInteractor).stop(eq(PlayerType.LIBRARY))
        verify(musicProviderRepository).writeErrorAboutComposition(
            eq(CorruptionType.UNKNOWN),
            eq(testComposition)
        )
    }

    @Test
    fun `on acceptable error event test`() {
        val ex: AcceptablePlayerException = mock()
        val playerException = AcceptablePlayerException(ex)
        playerEventSubject.onNext(PlayerEvent.ErrorEvent(playerException, testLibrarySource))

        verify(playerCoordinatorInteractor).error(eq(PlayerType.LIBRARY), eq(ex))
        verify(musicProviderRepository, never()).writeErrorAboutComposition(any(), any())
        verify(playQueueRepository, never()).skipToNext()
    }

    @Test
    fun `on empty queue event test`() {
        currentCompositionSubject.onNext(PlayQueueEvent(null))

        verify(playerCoordinatorInteractor).reset(eq(PlayerType.LIBRARY))
    }

    @Test
    fun `skip to previous test`() {
        whenever(settingsRepository.skipConstraintMillis).thenReturn(15)
        whenever(playerCoordinatorInteractor.getActualTrackPosition(any()))
            .thenReturn(Single.just(10L))
        libraryPlayerInteractor.skipToPrevious()
        inOrder.verify(playQueueRepository).skipToPrevious()

        whenever(settingsRepository.skipConstraintMillis).thenReturn(15)
        whenever(playerCoordinatorInteractor.getActualTrackPosition(any()))
            .thenReturn(Single.just(30L))
        libraryPlayerInteractor.skipToPrevious()
        inOrder.verify(playerCoordinatorInteractor).onSeekFinished(eq(0L), any())
    }
}