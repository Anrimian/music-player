package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.content.AcceptablePlayerException
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LibraryPlayerInteractorTest {

    private val playerCoordinatorInteractor: PlayerCoordinatorInteractor = mock()
    private val syncInteractor: SyncInteractor<FileKey, *, Long> = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val playQueueRepository: PlayQueueRepository = mock()
    private val musicProviderRepository: LibraryRepository = mock()
    private val uiStateRepository: UiStateRepository = mock()
    private val systemServiceController: SystemServiceController = mock()
    private val analytics: Analytics = mock()
    
    private lateinit var libraryPlayerInteractor: LibraryPlayerInteractor

    private val playQueueItem: PlayQueueItem = mock {
        on { duration } doReturn 60_000L
    }
    private val testLibrarySource: LibraryCompositionSource = mock {
        on { composition } doReturn playQueueItem
    }
    private val testPlayQueueEvent: PlayQueueEvent = mock {
        on { playQueueItem } doReturn playQueueItem
    }
    private var isCurrentCompositionErrorEnabled = false

    private val playerEventSubject = PublishSubject.create<PlayerEvent>()
    private val playerStateSubject = PublishSubject.create<PlayerState>()
    private val currentCompositionSubject = BehaviorSubject.createDefault(testPlayQueueEvent)
    private val trackPositionSubject = BehaviorSubject.createDefault(0L)

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
        whenever(playQueueRepository.setPlayQueue(any(), anyInt())).thenReturn(Completable.complete())
        whenever(playQueueRepository.getCurrentQueueItemObservable())
            .thenReturn(currentCompositionSubject.map {
                if (isCurrentCompositionErrorEnabled) {
                    throw Exception()
                }
                return@map it
            })
        whenever(playQueueRepository.getNextQueueItemId()).thenReturn(Single.just(1L))
        whenever(playQueueRepository.skipToNext()).thenReturn(Single.just(1))
        whenever(playQueueRepository.getItemTrackPosition(any())).thenReturn(Single.just(0L))
        whenever(playQueueRepository.setItemTrackPosition(any(), any())).thenReturn(Completable.complete())
        whenever(playQueueRepository.setCurrentItemTrackPosition(any())).thenReturn(Completable.complete())

        whenever(playerCoordinatorInteractor.getPlayerEventsObservable(any())).thenReturn(playerEventSubject)
        whenever(playerCoordinatorInteractor.getPlayerStateObservable(any())).thenReturn(playerStateSubject)
        whenever(playerCoordinatorInteractor.getIsPlayingStateObservable(any()))
            .thenReturn(Observable.just(false))
        whenever(playerCoordinatorInteractor.getTrackPositionObservable(any()))
            .thenReturn(trackPositionSubject)

        whenever(musicProviderRepository.writeErrorAboutComposition(any(), any()))
            .thenReturn(Completable.complete())
        whenever(musicProviderRepository.deleteComposition(any()))
            .thenReturn(Single.just(mock()))

        whenever(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled).thenReturn(true)
        whenever(settingsRepository.isPauseOnAudioFocusLossEnabled).thenReturn(true)

        libraryPlayerInteractor = LibraryPlayerInteractor(
            playerCoordinatorInteractor,
            syncInteractor,
            settingsRepository,
            playQueueRepository,
            musicProviderRepository,
            uiStateRepository,
            systemServiceController,
            analytics
        )
    }

    @Test
    fun `start playing test`() {
        val queue: List<Long> = mock()
        libraryPlayerInteractor.setQueueAndPlay(queue, 0).subscribe()

        playQueueRepository.setPlayQueue(eq(queue), eq(0))

        verify(playerCoordinatorInteractor).playAfterPrepare(eq(PlayerType.LIBRARY))
    }

    @Test
    fun `start playing with source prepare error`() {
        val ex = Exception()
        currentCompositionSubject.onError(ex)

        val queue: List<Long> = mock()
        libraryPlayerInteractor.setQueueAndPlay(queue, 0).subscribe()

        verify(playerCoordinatorInteractor, never()).playAfterPrepare(eq(PlayerType.LIBRARY))
    }

    @Test
    fun `play, error, play again`() {
        isCurrentCompositionErrorEnabled = true
        currentCompositionSubject.onNext(testPlayQueueEvent)

        libraryPlayerInteractor.play()
        verify(playerCoordinatorInteractor, never()).play(eq(PlayerType.LIBRARY), any())
        verify(systemServiceController).stopMusicService(eq(true), eq(true))

        isCurrentCompositionErrorEnabled = false
        currentCompositionSubject.onNext(testPlayQueueEvent)
        libraryPlayerInteractor.play()
        verify(playerCoordinatorInteractor).play(eq(PlayerType.LIBRARY), any())
    }

    @Nested
    @DisplayName("without prepare")
    inner class WithoutPrepare {

        @Test
        fun `just play`() {
            libraryPlayerInteractor.play()

            verify(playerCoordinatorInteractor).play(eq(PlayerType.LIBRARY), any())
        }

        @Test
        fun `seek to`() {
            val pos = 10_000L
            libraryPlayerInteractor.onSeekFinished(pos)

            verify(playQueueRepository).setCurrentItemTrackPosition(eq(pos))
            verify(playerCoordinatorInteractor).onSeekFinished(eq(pos), eq(PlayerType.LIBRARY))
        }

        @Test
        fun `seek by`() {
            val pos = 10_000L
            whenever(settingsRepository.rewindValueMillis).thenReturn(pos)

            libraryPlayerInteractor.fastSeekForward()

            verify(playQueueRepository).setCurrentItemTrackPosition(eq(pos))
            verify(playerCoordinatorInteractor).onSeekFinished(eq(pos), eq(PlayerType.LIBRARY))
        }

    }

    @Nested
    @DisplayName("after prepare")
    inner class AfterPrepare {

        @BeforeEach
        fun setUp() {
            libraryPlayerInteractor.prepare()
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
            verify(playerCoordinatorInteractor).pause(eq(PlayerType.LIBRARY))
        }

        @Test
        fun `on finished event received with last item in queue with repeat mode`() {
            whenever(playQueueRepository.skipToNext()).thenReturn(Single.just(0))
            whenever(settingsRepository.repeatMode).thenReturn(RepeatMode.REPEAT_PLAY_QUEUE)

            playerEventSubject.onNext(PlayerEvent.FinishedEvent(testLibrarySource))

            verify(playQueueRepository).skipToNext()
            verify(playerCoordinatorInteractor, never()).stop(eq(PlayerType.LIBRARY))
        }

        @Test
        fun `on error event test`() {
            whenever(playQueueRepository.isCurrentCompositionAtEndOfQueue())
                .thenReturn(Single.just(false))

            playerEventSubject.onNext(PlayerEvent.ErrorEvent(mock<Exception>(), testLibrarySource))

            verify(musicProviderRepository).writeErrorAboutComposition(
                eq(CorruptionType.UNKNOWN),
                eq(playQueueItem)
            )
            verify(playQueueRepository).skipToNext()
        }

        @Test
        fun `on error event at the end of the queue`() {
            whenever(playQueueRepository.isCurrentCompositionAtEndOfQueue())
                .thenReturn(Single.just(true))

            playerEventSubject.onNext(PlayerEvent.ErrorEvent(mock<Exception>(), testLibrarySource))

            verify(playQueueRepository, never()).skipToNext()
            verify(playerCoordinatorInteractor).stop(eq(PlayerType.LIBRARY))
            verify(musicProviderRepository).writeErrorAboutComposition(
                eq(CorruptionType.UNKNOWN),
                eq(playQueueItem)
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

    }

    @Test
    fun `skip to previous test`() {
        whenever(settingsRepository.skipConstraintMillis).thenReturn(15)
        libraryPlayerInteractor.seekTo(10L)

        libraryPlayerInteractor.skipToPrevious()
        inOrder.verify(playQueueRepository).skipToPrevious()

        libraryPlayerInteractor.seekTo(30L)
        libraryPlayerInteractor.skipToPrevious()
        inOrder.verify(playerCoordinatorInteractor).onSeekFinished(eq(0L), any())
    }
}