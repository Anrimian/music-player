package com.github.anrimian.musicplayer.data.controllers.music.players

import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.RelaunchSourceException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.upstream.Loader.UnexpectedLoaderException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

class CompositeMediaPlayer(
    private val mediaPlayers: ArrayList<() -> AppMediaPlayer>,
    private var currentPlaySpeed: Float,
    private var currentSoundBalance: SoundBalance,
): AppMediaPlayer {

    private val startPlayerIndex = 0

    private val playerEventsSubject = PublishSubject.create<MediaPlayerEvent>()
    private val playerDisposable = CompositeDisposable()

    private var currentPlayerIndex = startPlayerIndex
    private var currentPlayer = createNewPlayerInstance(currentPlayerIndex)
    private val currentPlayerSubject = BehaviorSubject.createDefault(currentPlayer)

    private var currentSource: CompositionContentSource? = null
    private var previousPrepareException: Exception? = null

    override fun prepareToPlay(
        source: CompositionContentSource,
        previousException: Exception?,
    ): Completable {
        //reset player for new source
        if (currentPlayerIndex != startPlayerIndex && source != currentSource) {
            setPlayer(startPlayerIndex)
        }
        //workaround for android media player unsupported error case
        var ex: Exception? = null
        if (source == currentSource) {
            ex = previousPrepareException
            previousPrepareException = null
        }
        currentSource = source
        return Single.fromCallable { currentPlayer }
            .flatMapCompletable { player -> player.prepareToPlay(source, ex) }
            .retry(this::switchPlayerOnError)
    }

    override fun stop() {
        currentSource = null
        currentPlayer.stop()
    }

    override fun resume() {
        currentPlayer.resume()
    }

    override fun pause() {
        currentPlayer.pause()
    }

    override fun seekTo(position: Long) {
        currentPlayer.seekTo(position)
    }

    override fun setVolume(volume: Float) {
        currentPlayer.setVolume(volume)
    }

    override fun getTrackPositionObservable(): Observable<Long> {
        return currentPlayerSubject.switchMap(AppMediaPlayer::getTrackPositionObservable)
    }

    override fun getTrackPosition(): Single<Long> {
        return currentPlayer.getTrackPosition()
    }

    override fun getDuration(): Single<Long> {
        return currentPlayer.getDuration()
    }

    override fun setPlaybackSpeed(speed: Float) {
        currentPlaySpeed = speed
        currentPlayer.setPlaybackSpeed(speed)
    }

    override fun release() {
        currentSource = null
        currentPlayer.release()
    }

    override fun getSpeedChangeAvailableObservable(): Observable<Boolean> {
        return currentPlayerSubject.switchMap(AppMediaPlayer::getSpeedChangeAvailableObservable)
    }

    override fun getPlayerEventsObservable(): Observable<MediaPlayerEvent> {
        return playerEventsSubject
    }

    override fun setSoundBalance(soundBalance: SoundBalance) {
        currentSoundBalance = soundBalance
        currentPlayer.setSoundBalance(soundBalance)
    }

    private fun setPlayer(index: Int) {
        currentPlayerIndex = index
        currentPlayer.release()
        currentPlayer = createNewPlayerInstance(index)
        currentPlayerSubject.onNext(currentPlayer)
    }

    private fun createNewPlayerInstance(index: Int): AppMediaPlayer {
        val newPlayer = mediaPlayers[index]()
        newPlayer.setPlaybackSpeed(currentPlaySpeed)
        newPlayer.setSoundBalance(currentSoundBalance)

        playerDisposable.clear()
        playerDisposable.add(newPlayer.getPlayerEventsObservable()
            .subscribe(this::onPlayerEventReceived)
        )
        return newPlayer
    }

    private fun onPlayerEventReceived(event: MediaPlayerEvent) {
        val eventToEmit = if (
            event is MediaPlayerEvent.Error && switchPlayerOnError(event.throwable)
        ) {
            MediaPlayerEvent.Error(RelaunchSourceException(event.throwable))
        } else {
            event
        }
        playerEventsSubject.onNext(eventToEmit)
    }

    private fun switchPlayerOnError(throwable: Throwable): Boolean {
        if (throwable is UnsupportedSourceException || isOutOfMemoryError(throwable)) {
            val newPlayerIndex = currentPlayerIndex + 1
            //don't switch player when we reached end of available players
            if (newPlayerIndex >= 0 && newPlayerIndex < mediaPlayers.size) {
                setPlayer(newPlayerIndex)
                return true
            }
        }
        return false
    }

    private fun isOutOfMemoryError(throwable: Throwable): Boolean {
        if (throwable is PlaybackException) {
            val cause = throwable.cause
            return cause is UnexpectedLoaderException && cause.cause is OutOfMemoryError
        }
        return false
    }

}