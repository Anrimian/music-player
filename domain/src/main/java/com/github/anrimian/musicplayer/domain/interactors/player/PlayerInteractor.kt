package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.content.RelaunchSourceException
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class PlayerInteractor(
    private val musicPlayerController: MusicPlayerController,
    private val compositionSourceInteractor: CompositionSourceInteractor,
    private val playerErrorParser: PlayerErrorParser,
    private val systemMusicController: SystemMusicController,
    private val systemServiceController: SystemServiceController,
    private val settingsRepository: SettingsRepository,
    private val analytics: Analytics,
    private val maxRePrepareTries: Int = 2
) {

    private val playerStateSubject = BehaviorSubject.createDefault<PlayerState>(PlayerState.IDLE)
    private val isPlayingSubject = BehaviorSubject.createDefault(false)

    private val playerEventsSubject = PublishSubject.create<PlayerEvent>()

    private var currentSource: CompositionSource? = null
    private val currentSourceSubject = BehaviorSubject.create<Optional<CompositionSource>>()//add default null value and check all behaviour

    private var currentPosition = 0L
    private val trackPositionSubject = BehaviorSubject.create<Long>()

    private var playAfterPrepare = false
    private var preparationDisposable: Disposable? = null
    private var isPreparing = false
    private var isPrepared = false

    private var pausedTransient = false

    private val eventsDisposable = CompositeDisposable()
    private var trackPositionDisposable: Disposable? = null
    private var playerEventsDisposable: Disposable? = null

    private var resumeDelay = 0L

    private var rePrepareCount = 0

    fun prepareToPlay(compositionSource: CompositionSource, startPosition: Long) {
        rePrepareCount = 0

        this.currentSource = compositionSource
        currentSourceSubject.onNext(Optional(currentSource))

        prepareToPlayInternal(compositionSource, startPosition)
    }

    fun updateSource(source: CompositionSource) {
        if (this.currentSource == source) {
            this.currentSource = source
            currentSourceSubject.onNext(Optional(currentSource))
        }
    }

    fun reset() {
        stopTracingTrackPosition()
        eventsDisposable.clear()
        preparationDisposable?.dispose()
        currentSource = null
        currentSourceSubject.onNext(Optional(null))
        systemServiceController.stopMusicService()
        musicPlayerController.stop()
        playAfterPrepare = false
        isPlayingSubject.onNext(false)
        playerStateSubject.onNext(PlayerState.IDLE)
    }

    fun play(delay: Long = 0L) {
        val currentSource = this.currentSource
        if (playerStateSubject.value == PlayerState.PLAY
            || currentSource == null
            || pausedTransient) {
            return
        }
        this.resumeDelay = delay
        if (isPreparing) {
            isPlayingSubject.onNext(true)
            playAfterPrepare = true
            return
        }
        if (!isPrepared) {
            isPlayingSubject.onNext(true)
            playAfterPrepare = true
            prepareToPlayInternal(currentSource, currentPosition)
            return
        }
        eventsDisposable.clear()
        val audioFocusObservable = systemMusicController.requestAudioFocus()
        if (audioFocusObservable == null) {
            if (isPlayingSubject.value == true) {
                systemServiceController.stopMusicService()
                isPlayingSubject.onNext(false)
                playerStateSubject.onNext(PlayerState.PAUSE)
            }
            return
        }
        if (resumeDelay == 0L) {
            resumeInternal()
        } else {
            Completable.complete()
                .delay(resumeDelay, TimeUnit.MILLISECONDS)
                .subscribe {
                    resumeDelay = 0L
                    resumeInternal()
                }
        }
        systemServiceController.startMusicService()
        isPlayingSubject.onNext(true)
        playerStateSubject.onNext(PlayerState.PLAY)

        subscribeOnPlayerEvents(audioFocusObservable)
    }

    fun playAfterPrepare() {
        playAfterPrepare = true
        isPlayingSubject.onNext(true)
    }

    fun playOrPause() {
        if (isPlayingSubject.value == false) {
            play()
        } else {
            pause()
        }
    }

    fun stop() {
        systemServiceController.stopMusicService()
        playAfterPrepare = false
        musicPlayerController.stop()
        isPlayingSubject.onNext(false)
        playerStateSubject.onNext(PlayerState.STOP)
        stopTracingTrackPosition()
        eventsDisposable.clear()
    }

    fun pause() {
        isPlayingSubject.onNext(false)
        systemServiceController.stopMusicService()
        eventsDisposable.clear()
        if (isPreparing) {
            playAfterPrepare = false
            return
        }
        if (isPrepared) {
            if (pausedTransient) {
                pausedTransient = false
                return
            }
            pauseInternal()
            playerStateSubject.onNext(PlayerState.PAUSE)
        }
    }

    fun error(throwable: Throwable) {
        systemServiceController.stopMusicService()
        musicPlayerController.seekTo(0)
        pauseInternal()
        eventsDisposable.clear()

        isPlayingSubject.onNext(false)
        playerStateSubject.onNext(PlayerState.Error(throwable))
    }

    fun onSeekStarted() {
        if (playerStateSubject.value == PlayerState.PLAY) {
            pauseInternal()
        }
    }

    fun onSeekFinished(position: Long) {
        if (playerStateSubject.value == PlayerState.PLAY) {
            resumeInternal()
        }
        if (!isPreparing) {
            musicPlayerController.seekTo(position)
        }
        updateCurrentPosition(position)
    }

    fun fastSeekForward(): Single<Long> {
        return seekBy(settingsRepository.rewindValueMillis)
    }

    fun fastSeekBackward(): Single<Long> {
        return seekBy(-settingsRepository.rewindValueMillis)
    }

    fun getTrackPositionObservable(): Observable<Long> {
        return trackPositionSubject.distinctUntilChanged()
    }

    fun getPlayerStateObservable(): Observable<PlayerState> {
        return playerStateSubject
    }

    fun getIsPlayingStateObservable(): Observable<Boolean> {
        return isPlayingSubject.distinctUntilChanged()
    }

    fun getPlayerState(): PlayerState {
        return playerStateSubject.value!!
    }

    fun isPlaying() = isPlayingSubject.value == true

    fun getTrackPosition(): Single<Long> {
        if (isPreparing) {
            return Single.just(currentPosition)
        }
        return musicPlayerController.getTrackPosition()
    }

    fun setPlaybackSpeed(speed: Float) {
        musicPlayerController.setPlaybackSpeed(speed)
    }

    fun getPlayerEventsObservable(): Observable<PlayerEvent> {
        return playerEventsSubject
    }

    fun getCurrentSourceObservable(): Observable<Optional<CompositionSource>> {
        return currentSourceSubject
    }

    fun getCurrentSource(): CompositionSource? {
        return currentSource
    }

    fun getSpeedChangeAvailableObservable(): Observable<Boolean> {
        return musicPlayerController.getSpeedChangeAvailableObservable()
    }

    fun getCurrentPlaybackSpeedObservable(): Observable<Float> {
        return musicPlayerController.getCurrentPlaybackSpeedObservable()
    }

    private fun prepareToPlayInternal(compositionSource: CompositionSource, startPosition: Long) {
        isPrepared = false

        stopTracingTrackPosition()
        updateCurrentPosition(startPosition)

        isPreparing = true
        RxUtils.dispose(preparationDisposable)
        if (isPlayingSubject.value == true) {
            musicPlayerController.pause()
        }
        if (playerEventsDisposable == null) {
            playerEventsDisposable = musicPlayerController.getPlayerEventsObservable()
                .subscribe(this::onPlayerEventReceived)
        }
        preparationDisposable = compositionSourceInteractor.getCompositionSource(compositionSource)
            .doOnSubscribe { playerStateSubject.onNext(PlayerState.LOADING) }
            .doOnSuccess { playerStateSubject.onNext(PlayerState.PREPARING) }
            .flatMapCompletable(musicPlayerController::prepareToPlay)
            .subscribe(this::onSourcePrepared, this::onSourcePreparingError)
    }

    private fun onSourcePrepared() {
        isPreparing = false
        isPrepared = true
        musicPlayerController.seekTo(currentPosition)
        currentSource?.let { source ->
            playerEventsSubject.onNext(PlayerEvent.PreparedEvent(source))
        }

        if (playAfterPrepare) {
            playAfterPrepare = false
            play()
        } else {
            if (isPlayingSubject.value == true) {
                musicPlayerController.resume()
                startTracingTrackPosition()
                playerStateSubject.onNext(PlayerState.PLAY)
            } else {
                playerStateSubject.onNext(PlayerState.PAUSE)
            }
        }
    }

    private fun onSourcePreparingError(throwable: Throwable) {
        val formattedException = parseError(throwable) ?: return

        isPreparing = false
        playAfterPrepare = false

        currentSource?.let { source ->
            playerEventsSubject.onNext(PlayerEvent.ErrorEvent(formattedException, source))
        }
    }

    private fun subscribeOnPlayerEvents(audioFocusObservable: Observable<AudioFocusEvent>) {
        if (eventsDisposable.size() != 0) {
            return
        }
        eventsDisposable.add(audioFocusObservable.subscribe(this::onAudioFocusChanged))
        eventsDisposable.add(systemMusicController.audioBecomingNoisyObservable
            .subscribe { onAudioBecomingNoisy() })
        eventsDisposable.add(systemMusicController.volumeObservable
            .subscribe(this::onVolumeChanged))
    }

    private fun updateCurrentPosition(position: Long) {
        currentPosition = position
        trackPositionSubject.onNext(position)
    }

    private fun onAudioFocusChanged(event: AudioFocusEvent) {
        val isPlaying = isPlayingSubject.value == true
        when (event) {
            AudioFocusEvent.GAIN -> {
                musicPlayerController.setVolume(1f)
                if (pausedTransient) {
                    pausedTransient = false
                    isPlayingSubject.onNext(true)
                    if (isPreparing) {
                        return
                    }
                    playerStateSubject.onNext(PlayerState.PLAY)
                    resumeInternal()
                } else if (!isPlaying) {
                    systemServiceController.stopMusicService()
                }
            }
            AudioFocusEvent.LOSS_SHORTLY -> {
                if (isPlaying && settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled) {
                    musicPlayerController.setVolume(0.5f)
                }
            }
            AudioFocusEvent.LOSS_TRANSIENT -> {
                if (isPlaying && settingsRepository.isPauseOnAudioFocusLossEnabled) {
                    pausedTransient = true
                    isPlayingSubject.onNext(false)
                    if (isPreparing) {
                        return
                    }
                    pauseInternal()
                    playerStateSubject.onNext(PlayerState.PAUSE)
                }
            }
            AudioFocusEvent.LOSS -> {
                if (isPlaying && settingsRepository.isPauseOnAudioFocusLossEnabled) {
                    pausedTransient = false
                    isPlayingSubject.onNext(false)
                    systemServiceController.stopMusicService()
                    if (isPreparing) {
                        return
                    }
                    pauseInternal()
                    playerStateSubject.onNext(PlayerState.PAUSE)
                }
            }
        }
    }

    private fun onAudioBecomingNoisy() {
        systemServiceController.stopMusicService()
        if (pausedTransient) {
            pausedTransient = false
            return
        }
        pauseInternal()
        isPlayingSubject.onNext(false)
        playerStateSubject.onNext(PlayerState.PAUSE)
    }

    private fun onPlayerEventReceived(event: MediaPlayerEvent) {
        val currentSource = currentSource ?: return
        when(event) {
            MediaPlayerEvent.Finished -> playerEventsSubject.onNext(PlayerEvent.FinishedEvent(currentSource))
            is MediaPlayerEvent.Error -> onPlayerErrorEventReceived(event.throwable, currentSource)
        }
    }

    private fun onPlayerErrorEventReceived(throwable: Throwable, currentSource: CompositionSource) {
        if (isPreparing) {
            return
        }
        val formattedException = parseError(throwable) ?: return

        isPrepared = false

        playerEventsSubject.onNext(PlayerEvent.ErrorEvent(formattedException, currentSource))
    }

    private fun parseError(throwable: Throwable): Throwable? {
        val formattedException = playerErrorParser.parseError(throwable)
        if (formattedException is RelaunchSourceException) {
            if (launchRePrepare()) {
                return null
            } else {
                val cause = formattedException.cause
                analytics.processNonFatalError(cause)
                return cause
            }
        }
        return formattedException
    }

    private fun launchRePrepare(): Boolean {
        val source = currentSource
        if (source == null || rePrepareCount >= maxRePrepareTries) {
            rePrepareCount = 0
            return false
        }
        rePrepareCount++
        prepareToPlayInternal(source, currentPosition)
        return true
    }

    private fun onVolumeChanged(volume: Int) {
        if (settingsRepository.isPauseOnZeroVolumeLevelEnabled
            && playerStateSubject.value == PlayerState.PLAY
            && volume == 0) {
            pause()
        }
    }

    private fun seekBy(millis: Long): Single<Long> {
        return Single.zip(getTrackPosition(), getDuration()) { position, duration ->
            val targetPosition = (position + millis).coerceAtLeast(0)
            if (targetPosition > duration && duration != -1L) {
                return@zip position
            }
            seekTo(targetPosition)
            return@zip targetPosition
        }
    }

    private fun getDuration(): Single<Long> {
        if (isPreparing) {
            return Single.just(-1L)
        }
        return musicPlayerController.getDuration()
    }

    private fun seekTo(position: Long) {
        updateCurrentPosition(position)
        if (!isPreparing) {
            musicPlayerController.seekTo(position)
        }
    }

    private fun resumeInternal() {
        musicPlayerController.resume()
        startTracingTrackPosition()
    }

    private fun pauseInternal() {
        musicPlayerController.pause()
        stopTracingTrackPosition()
    }

    private fun startTracingTrackPosition() {
        stopTracingTrackPosition()
        trackPositionDisposable = musicPlayerController.getTrackPositionObservable()
            .doOnSubscribe { trackPositionSubject.onNext(currentPosition) }
            .subscribe(this::updateCurrentPosition)
    }

    private fun stopTracingTrackPosition() {
        if (trackPositionDisposable != null) {
            trackPositionDisposable!!.dispose()
            trackPositionDisposable = null
        }
    }

}