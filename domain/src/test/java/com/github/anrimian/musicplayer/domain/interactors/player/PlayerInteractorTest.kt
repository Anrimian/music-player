package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.musicplayer.domain.Constants.TRIGGER
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.RelaunchSourceException
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit

class PlayerInteractorTest {

    private val musicPlayerController: MusicPlayerController = mock()
    private val compositionSourceInteractor: CompositionSourceInteractor = mock()
    private val playerErrorParser: PlayerErrorParser = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val systemMusicController: SystemMusicController = mock()
    private val systemServiceController: SystemServiceController = mock()
    private val analytics: Analytics = mock()

    private val playerInteractor = PlayerInteractor(
        musicPlayerController,
        compositionSourceInteractor,
        playerErrorParser,
        systemMusicController,
        systemServiceController,
        settingsRepository,
        analytics,
        1
    )

    private val playerEventSubject = PublishSubject.create<MediaPlayerEvent>()
    private val audioFocusSubject = PublishSubject.create<AudioFocusEvent>()
    private val noisyAudioSubject = PublishSubject.create<Any>()
    private val volumeSubject = PublishSubject.create<Int>()
    private val positionSubject = PublishSubject.create<Long>()

    private val currentSourceSubscriber = playerInteractor.getCurrentSourceObservable().test()
    private val positionSubscriber = playerInteractor.getTrackPositionObservable().test()
    private val playerStateSubscriber = playerInteractor.getPlayerStateObservable().test()
    private val isPlayingStateSubscriber = playerInteractor.getIsPlayingStateObservable().test()
    private val playerEventsSubscriber = playerInteractor.getPlayerEventsObservable().test()

    private val inOrder = Mockito.inOrder(
        musicPlayerController,
        compositionSourceInteractor,
        playerErrorParser,
        systemMusicController,
        systemServiceController,
        settingsRepository
    )

    private val testSource: CompositionSource = mock()
    private val testContentSource: CompositionContentSource = mock()

    @BeforeEach
    fun setUp() {
        whenever(musicPlayerController.getPlayerEventsObservable()).thenReturn(playerEventSubject)
        whenever(systemMusicController.requestAudioFocus()).thenReturn(audioFocusSubject)
        whenever(systemMusicController.audioBecomingNoisyObservable).thenReturn(noisyAudioSubject)
        whenever(systemMusicController.volumeObservable).thenReturn(volumeSubject)

        whenever(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled).thenReturn(true)
        whenever(settingsRepository.isPauseOnAudioFocusLossEnabled).thenReturn(true)

        whenever(musicPlayerController.prepareToPlay(any())).thenReturn(Completable.complete())
        whenever(musicPlayerController.getTrackPositionObservable()).thenReturn(positionSubject)

        whenever(compositionSourceInteractor.getCompositionSource(any()))
            .thenReturn(Single.just(testContentSource))

        whenever(playerErrorParser.parseError(any())).thenReturn(mock())
    }

    @Test
    fun `prepare to play in pause state`() {
        val startPosition = 0L
        playerInteractor.prepareToPlay(testSource, startPosition)

        inOrder.verify(systemServiceController, never()).startMusicService()
        inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
        inOrder.verify(musicPlayerController).seekTo(eq(startPosition))
        inOrder.verify(systemServiceController, never()).stopMusicService()

        currentSourceSubscriber.assertValue(Optional(testSource))
        positionSubscriber.assertValue(startPosition)
        playerStateSubscriber.assertValues(
            PlayerState.IDLE,
            PlayerState.LOADING,
            PlayerState.PREPARING,
            PlayerState.PAUSE,
        )
        isPlayingStateSubscriber.assertValue(false)
    }

    @Test
    fun `resume without source preparing`() {
        playerInteractor.play()

        positionSubscriber.assertNoValues()
        playerStateSubscriber.assertValue(PlayerState.IDLE)
        isPlayingStateSubscriber.assertValue(false)
    }

    @Nested
    @DisplayName("in prepare process")
    inner class InPrepareProcess {

        private val startPosition = 0L
        private val testScheduler = TestScheduler()

        @BeforeEach
        fun setUp() {
            whenever(compositionSourceInteractor.getCompositionSource(testSource)).thenReturn(
                Single.just(testContentSource).delay(1000, TimeUnit.MILLISECONDS, testScheduler)
            )

            playerInteractor.prepareToPlay(testSource, startPosition)
            testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)

            inOrder.verify(systemServiceController, never()).startMusicService()

            currentSourceSubscriber.assertValue(Optional(testSource))
        }

        @Test
        fun resume() {
            playerInteractor.play()
            testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).seekTo(eq(startPosition))
            inOrder.verify(systemMusicController).requestAudioFocus()
            inOrder.verify(musicPlayerController).resume()
            inOrder.verify(systemServiceController).startMusicService()

            positionSubscriber.assertValue(startPosition)
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PLAY,
            )
            isPlayingStateSubscriber.assertValues(false, true)
        }

        @Test
        fun `resume and audio focus not gained`() {
            whenever(systemMusicController.requestAudioFocus()).thenReturn(null)
            playerInteractor.play()
            testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).seekTo(eq(startPosition))
            verify(musicPlayerController, never()).resume()
            verify(systemServiceController, never()).startMusicService()
            verify(systemServiceController, times(1)).stopMusicService()

            positionSubscriber.assertValue(startPosition)
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE
            )
            isPlayingStateSubscriber.assertValues(false, true, false)
        }

        @Test
        fun `resume and pause`() {
            playerInteractor.play()
            playerInteractor.pause()
            testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

            inOrder.verify(systemServiceController).stopMusicService()
            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).seekTo(eq(startPosition))

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValues(false, true, false)

        }

        @Test
        fun `seek to`() {
            val newPosition = 100L
            playerInteractor.onSeekFinished(newPosition)
            testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

            inOrder.verify(musicPlayerController).seekTo(eq(newPosition))

            positionSubscriber.assertValues(startPosition, newPosition)
        }

        @Test
        fun `seek by`() {
            val rewindValue = 100L
            val newPosition = startPosition + rewindValue
            whenever(settingsRepository.rewindValueMillis).thenReturn(rewindValue)

            playerInteractor.fastSeekForward().subscribe()
            testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

            inOrder.verify(musicPlayerController).seekTo(eq(newPosition))

            positionSubscriber.assertValues(startPosition, newPosition)
        }

        @Test
        fun `prepare another source`() {
            val testSource2: CompositionSource = mock()
            val testContentSource2: CompositionContentSource = mock()
            whenever(compositionSourceInteractor.getCompositionSource(testSource2))
                .thenReturn(Single.just(testContentSource2))

            playerInteractor.prepareToPlay(testSource2, startPosition)
            testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

            verify(musicPlayerController, never()).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource2))
            inOrder.verify(musicPlayerController).seekTo(eq(startPosition))
            inOrder.verify(systemServiceController, never()).stopMusicService()

            currentSourceSubscriber.assertValues(Optional(testSource), Optional(testSource2))
            positionSubscriber.assertValue(startPosition)
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValue(false)
        }

        @Test
        fun `call reset`() {
            playerInteractor.reset()
            testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

            playerEventsSubscriber.assertNoValues()
            verify(musicPlayerController, never()).resume()
            inOrder.verify(systemServiceController).stopMusicService()
            inOrder.verify(musicPlayerController).stop()

            isPlayingStateSubscriber.assertValue(false)
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.IDLE,
            )
        }

        @Test
        fun `error and call reset`() {
            whenever(musicPlayerController.prepareToPlay(any()))
                .thenReturn(Completable.error(mock<Exception>()))
            playerInteractor.reset()
            testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

            playerEventsSubscriber.assertNoValues()
            verify(playerErrorParser, never()).parseError(any())
            inOrder.verify(systemServiceController).stopMusicService()
            inOrder.verify(musicPlayerController).stop()

            isPlayingStateSubscriber.assertValue(false)
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.IDLE,
            )
        }

        @Nested
        @DisplayName("resume, prepare another source in progress")
        inner class ResumeAndPrepareNext {

            private val testSource2: CompositionSource = mock()
            private val testContentSource2: CompositionContentSource = mock()

            @BeforeEach
            fun setUp() {
                whenever(compositionSourceInteractor.getCompositionSource(testSource2))
                    .thenReturn(Single.just(testContentSource2).delay(1000, TimeUnit.MILLISECONDS, testScheduler))

                testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
                playerInteractor.play()
                playerInteractor.prepareToPlay(testSource2, startPosition)
                testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
            }

            @Test
            fun ` and audio focus loss in prepare`() {
                audioFocusSubject.onNext(AudioFocusEvent.LOSS)
                testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)

                inOrder.verify(systemServiceController).stopMusicService()
                inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource2))
                inOrder.verify(musicPlayerController).seekTo(eq(startPosition))

                currentSourceSubscriber.assertValues(Optional(testSource), Optional(testSource2))
                positionSubscriber.assertValues(startPosition)
                playerStateSubscriber.assertValues(
                    PlayerState.IDLE,
                    PlayerState.LOADING,
                    PlayerState.PREPARING,
                    PlayerState.PAUSE,
                    PlayerState.PLAY,
                    PlayerState.LOADING,
                    PlayerState.PREPARING,
                    PlayerState.PAUSE,
                )
                isPlayingStateSubscriber.assertValues(false, true, false)
            }

            @Test
            fun ` and audio focus transient loss in prepare and gain again`() {
                audioFocusSubject.onNext(AudioFocusEvent.LOSS_TRANSIENT)
                audioFocusSubject.onNext(AudioFocusEvent.GAIN)
                testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)

                inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource2))
                inOrder.verify(musicPlayerController).seekTo(eq(startPosition))
                inOrder.verify(musicPlayerController, never()).pause()
                inOrder.verify(musicPlayerController).resume()

                currentSourceSubscriber.assertValues(Optional(testSource), Optional(testSource2))
                positionSubscriber.assertValues(startPosition)
                playerStateSubscriber.assertValues(
                    PlayerState.IDLE,
                    PlayerState.LOADING,
                    PlayerState.PREPARING,
                    PlayerState.PAUSE,
                    PlayerState.PLAY,
                    PlayerState.LOADING,
                    PlayerState.PREPARING,
                    PlayerState.PLAY,
                )
                isPlayingStateSubscriber.assertValues(false, true, false, true)
            }
        }
    }

    @Nested
    @DisplayName("prepare error")
    inner class PrepareError {

        private val testException: Exception = mock()
        private val formattedException: Exception = mock()
        private val startPosition = 0L

        @BeforeEach
        fun setUp() {
            whenever(musicPlayerController.prepareToPlay(any()))
                .thenReturn(Completable.error(testException))
            whenever(playerErrorParser.parseError(any()))
                .thenReturn(formattedException)

            playerInteractor.prepareToPlay(testSource, startPosition)

            inOrder.verify(playerErrorParser).parseError(eq(testException))

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
            )
            currentSourceSubscriber.assertValues(Optional(testSource))
            playerEventsSubscriber.assertValue { event -> event is PlayerEvent.ErrorEvent }

        }

        @Test
        fun play() {
            whenever(musicPlayerController.prepareToPlay(any()))
                .thenReturn(Completable.complete())

            playerInteractor.play()

            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).seekTo(eq(startPosition))
            inOrder.verify(systemMusicController).requestAudioFocus()
            inOrder.verify(musicPlayerController).resume()
            inOrder.verify(systemServiceController).startMusicService()

            currentSourceSubscriber.assertValue(Optional(testSource))
            positionSubscriber.assertValue(startPosition)
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PLAY,
            )
            isPlayingStateSubscriber.assertValues(false, true)
        }

        @Test
        fun pause() {
            playerInteractor.pause()

            inOrder.verify(musicPlayerController, never()).pause()

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
            )
            isPlayingStateSubscriber.assertValues(false)
        }

        @Test
        fun `and move to error state`() {
            playerInteractor.error(formattedException)

            verify(systemServiceController).stopMusicService()
            verify(musicPlayerController).seekTo(eq(0L))
            verify(musicPlayerController).pause()

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.Error(formattedException),
            )
            isPlayingStateSubscriber.assertValues(false)
        }
    }

    @Test
    fun `prepare and re prepare error`() {
        val ex: Exception = mock()
        val relaunchException = RelaunchSourceException(ex)
        whenever(musicPlayerController.prepareToPlay(any())).thenReturn(Completable.error(ex))
        whenever(playerErrorParser.parseError(eq(ex))).thenReturn(relaunchException)

        playerInteractor.prepareToPlay(testSource, 0)

        inOrder.verify(musicPlayerController, times(2)).prepareToPlay(eq(testContentSource))

        playerStateSubscriber.assertValues(
            PlayerState.IDLE,
            PlayerState.LOADING,
            PlayerState.PREPARING,
            PlayerState.LOADING,
            PlayerState.PREPARING,
        )
        currentSourceSubscriber.assertValues(Optional(testSource))
        playerEventsSubscriber.assertValue { event ->
            (event as PlayerEvent.ErrorEvent).throwable == ex
        }
    }

    @Nested
    @DisplayName("prepare error and re prepare")
    inner class PrepareErrorAndRePrepare {

        private val startPosition = 0L
        private val testScheduler = TestScheduler()

        private val testException: Exception = mock()

        @BeforeEach
        fun setUp() {
            whenever(compositionSourceInteractor.getCompositionSource(testSource)).thenReturn(
                Single.just(testContentSource).delay(1000, TimeUnit.MILLISECONDS, testScheduler)
            )
            whenever(musicPlayerController.prepareToPlay(any()))
                .thenReturn(Completable.error(testException))
                .thenReturn(Completable.complete())
            whenever(playerErrorParser.parseError(eq(testException))).thenReturn(RelaunchSourceException(testException))
        }

        @Test
        fun `in pause state`() {
            playerInteractor.prepareToPlay(testSource, 0)
            playerInteractor.pause()
            testScheduler.advanceTimeBy(2000, TimeUnit.MILLISECONDS)

            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).seekTo(eq(startPosition))

            positionSubscriber.assertValue(startPosition)
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
            )
            currentSourceSubscriber.assertValues(Optional(testSource))
            playerEventsSubscriber.assertValue { event -> event is PlayerEvent.PreparedEvent }
        }

        @Test
        fun `in play state`() {
            playerInteractor.prepareToPlay(testSource, 0)
            playerInteractor.play()
            testScheduler.advanceTimeBy(2000, TimeUnit.MILLISECONDS)

            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).seekTo(eq(startPosition))
            inOrder.verify(systemMusicController).requestAudioFocus()
            inOrder.verify(musicPlayerController).resume()
            inOrder.verify(systemServiceController).startMusicService()

            positionSubscriber.assertValue(startPosition)
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PLAY,
            )
            currentSourceSubscriber.assertValues(Optional(testSource))
            playerEventsSubscriber.assertValue { event -> event is PlayerEvent.PreparedEvent }
        }
    }

    @Test
    fun `prepare, resume, and audio focus not gained`() {
        val startPosition = 0L
        whenever(systemMusicController.requestAudioFocus()).thenReturn(null)

        playerInteractor.prepareToPlay(testSource, startPosition)
        playerInteractor.play()

        verify(musicPlayerController, never()).resume()
        verify(systemServiceController, never()).startMusicService()
        verify(systemServiceController, never()).stopMusicService()

        positionSubscriber.assertValue(startPosition)
        playerStateSubscriber.assertValues(
            PlayerState.IDLE,
            PlayerState.LOADING,
            PlayerState.PREPARING,
            PlayerState.PAUSE
        )
        isPlayingStateSubscriber.assertValues(false)
    }

    @Test
    fun `prepare and get error event`() {
        val startPosition = 0L
        val ex: Exception = mock()

        playerInteractor.prepareToPlay(testSource, startPosition)
        playerEventSubject.onNext(MediaPlayerEvent.Error(ex))

        playerStateSubscriber.assertValues(
            PlayerState.IDLE,
            PlayerState.LOADING,
            PlayerState.PREPARING,
            PlayerState.PAUSE,
        )
        isPlayingStateSubscriber.assertValues(false)

        playerEventsSubscriber.assertValueAt(0) { event -> event is PlayerEvent.PreparedEvent }
        playerEventsSubscriber.assertValueAt(1) { event -> event is PlayerEvent.ErrorEvent }
    }

    @Test
    fun `prepare and get maximum error event with re prepare`() {
        val startPosition = 0L
        val ex: Exception = mock()
        val relaunchException = RelaunchSourceException(ex)

        whenever(playerErrorParser.parseError(eq(relaunchException))).thenReturn(relaunchException)

        playerInteractor.prepareToPlay(testSource, startPosition)
        playerEventSubject.onNext(MediaPlayerEvent.Error(relaunchException))
        playerEventSubject.onNext(MediaPlayerEvent.Error(relaunchException))

        playerStateSubscriber.assertValues(
            PlayerState.IDLE,
            PlayerState.LOADING,
            PlayerState.PREPARING,
            PlayerState.PAUSE,
            PlayerState.LOADING,
            PlayerState.PREPARING,
            PlayerState.PAUSE
        )
        isPlayingStateSubscriber.assertValues(false)

        playerEventsSubscriber.assertValueAt(0) { event -> event is PlayerEvent.PreparedEvent }
        playerEventsSubscriber.assertValueAt(1) { event -> event is PlayerEvent.PreparedEvent }
        playerEventsSubscriber.assertValueAt(2) { event ->
            (event as PlayerEvent.ErrorEvent).throwable == ex
        }
    }

    @Nested
    @DisplayName("prepare to play and resume")
    inner class PrepareToPlayAndResume {

        private val startPosition = 0L

        @BeforeEach
        fun setUp() {
            playerInteractor.prepareToPlay(testSource, startPosition)
            playerInteractor.play()

            inOrder.verify(musicPlayerController).prepareToPlay(eq(testContentSource))
            inOrder.verify(musicPlayerController).seekTo(eq(startPosition))
            inOrder.verify(systemMusicController).requestAudioFocus()
            inOrder.verify(musicPlayerController).resume()
            inOrder.verify(systemServiceController).startMusicService()

            positionSubscriber.assertValue(startPosition)
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
            )
            isPlayingStateSubscriber.assertValues(false, true)
        }

        @Test
        fun `and pause`() {
            playerInteractor.pause()

            inOrder.verify(systemServiceController).stopMusicService()
            inOrder.verify(musicPlayerController).pause()
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValues(false, true, false)
        }

        @Test
        fun `then prepare another source`() {
            playerInteractor.prepareToPlay(mock(), 0L)

            inOrder.verify(systemServiceController, never()).stopMusicService()
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PLAY,
            )
            isPlayingStateSubscriber.assertValues(false, true)
        }

        @Test
        fun `then get error event`() {
            val ex: Exception = mock()

            playerEventSubject.onNext(MediaPlayerEvent.Error(ex))

            playerEventsSubscriber.assertValueAt(1) { event -> event is PlayerEvent.ErrorEvent }
        }

        @Test
        fun `pause, then get error event`() {
            val ex: Exception = mock()

            playerInteractor.pause()
            playerEventSubject.onNext(MediaPlayerEvent.Error(ex))

            playerEventsSubscriber.assertValueAt(1) { event -> event is PlayerEvent.ErrorEvent }
        }

        @Test
        fun `then audio focus loss`() {
            audioFocusSubject.onNext(AudioFocusEvent.LOSS)

            inOrder.verify(systemServiceController).stopMusicService()
            inOrder.verify(musicPlayerController).pause()

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValues(false, true, false)
        }

        @Test
        fun `then audio focus loss transient and gain`() {
            audioFocusSubject.onNext(AudioFocusEvent.LOSS_TRANSIENT)

            inOrder.verify(systemServiceController, never()).stopMusicService()
            inOrder.verify(musicPlayerController).pause()

            audioFocusSubject.onNext(AudioFocusEvent.GAIN)

            inOrder.verify(musicPlayerController).resume()
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.PAUSE,
                PlayerState.PLAY,
            )
            isPlayingStateSubscriber.assertValues(false, true, false, true)
        }

        @Test
        fun `then audio focus loss transient and pause and gain`() {
            audioFocusSubject.onNext(AudioFocusEvent.LOSS_TRANSIENT)

            inOrder.verify(musicPlayerController).pause()

            playerInteractor.pause()
            audioFocusSubject.onNext(AudioFocusEvent.GAIN)

            inOrder.verify(systemServiceController).stopMusicService()
            inOrder.verify(musicPlayerController, never()).resume()
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValues(false, true, false)
        }

        @Test
        fun `then audio focus loss transient and play`() {
            audioFocusSubject.onNext(AudioFocusEvent.LOSS_TRANSIENT)

            inOrder.verify(musicPlayerController).pause()

            playerInteractor.play()

            inOrder.verify(musicPlayerController, never()).resume()
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValues(false, true, false)
        }

        @Test
        fun `then audio focus loss shortly and gain`() {
            audioFocusSubject.onNext(AudioFocusEvent.LOSS_SHORTLY)

            inOrder.verify(musicPlayerController).setVolume(eq(0.5f))

            audioFocusSubject.onNext(AudioFocusEvent.GAIN)

            inOrder.verify(musicPlayerController).setVolume(eq(1f))

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
            )
            isPlayingStateSubscriber.assertValues(false, true)
        }

        @Test
        fun `then audio focus loss shortly and gain without decrease volume`() {
            whenever(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled).thenReturn(false)

            audioFocusSubject.onNext(AudioFocusEvent.LOSS_SHORTLY)

            inOrder.verify(musicPlayerController, never()).setVolume(eq(0.5f))

            audioFocusSubject.onNext(AudioFocusEvent.GAIN)

            inOrder.verify(musicPlayerController).setVolume(eq(1f))
        }

        @Test
        fun `then audio focus loss transient and gain without pause setting`() {
            whenever(settingsRepository.isPauseOnAudioFocusLossEnabled).thenReturn(false)

            audioFocusSubject.onNext(AudioFocusEvent.LOSS_TRANSIENT)

            inOrder.verify(musicPlayerController, never()).pause()

            audioFocusSubject.onNext(AudioFocusEvent.GAIN)

            inOrder.verify(musicPlayerController, never()).resume()
            inOrder.verify(systemServiceController, never()).stopMusicService()
            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY
            )
            isPlayingStateSubscriber.assertValues(false, true)
        }

        @Test
        fun `then audio became noisy`() {
            noisyAudioSubject.onNext(TRIGGER)

            inOrder.verify(musicPlayerController).pause()

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValues(false, true, false)
        }

        @Test
        fun `then audio focus loss, then became noisy, then gain focus`() {
            audioFocusSubject.onNext(AudioFocusEvent.LOSS_TRANSIENT)

            inOrder.verify(musicPlayerController).pause()

            noisyAudioSubject.onNext(TRIGGER)
            audioFocusSubject.onNext(AudioFocusEvent.GAIN)

            inOrder.verify(systemServiceController, times(1)).stopMusicService()
            inOrder.verify(musicPlayerController, never()).resume()

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValues(false, true, false)
        }

        @Test
        fun `then audio became noisy, then focus loss, then gain focus`() {
            noisyAudioSubject.onNext(TRIGGER)

            inOrder.verify(musicPlayerController).pause()

            audioFocusSubject.onNext(AudioFocusEvent.LOSS_TRANSIENT)
            audioFocusSubject.onNext(AudioFocusEvent.GAIN)

            inOrder.verify(musicPlayerController, never()).resume()

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValues(false, true, false)
        }

        @Test
        fun `then volume set to silent`() {
            whenever(settingsRepository.isPauseOnZeroVolumeLevelEnabled).thenReturn(true)

            volumeSubject.onNext(0)

            inOrder.verify(systemServiceController).stopMusicService()
            inOrder.verify(musicPlayerController).pause()

            playerStateSubscriber.assertValues(
                PlayerState.IDLE,
                PlayerState.LOADING,
                PlayerState.PREPARING,
                PlayerState.PAUSE,
                PlayerState.PLAY,
                PlayerState.PAUSE,
            )
            isPlayingStateSubscriber.assertValues(false, true, false)
        }
    }

}