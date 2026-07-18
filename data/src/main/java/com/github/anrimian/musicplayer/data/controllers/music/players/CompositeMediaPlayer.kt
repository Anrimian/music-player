package com.github.anrimian.musicplayer.data.controllers.music.players

import com.github.anrimian.musicplayer.data.controllers.music.players.exceptions.PlayerOutOfMemoryException
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.RelaunchSourceException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.player.events.MediaPlayerEvent
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
    private var currentSkipSilenceEnabled: Boolean,
): AppMediaPlayer {

    private val playerEventsSubject = PublishSubject.create<MediaPlayerEvent>()
    private val playerDisposable = CompositeDisposable()

    private var currentPlayerIndex = START_PLAYER_INDEX
    private var nextPlayerIndex = currentPlayerIndex
    private var currentPlayer = createNewPlayerInstance(currentPlayerIndex)
    private val currentPlayerSubject = BehaviorSubject.createDefault(currentPlayer)

    private var currentSource: CompositionContentSource? = null
    private var previousPlayerException: Exception? = null

    override fun prepareToPlay(
        source: CompositionContentSource,
        previousException: Exception?,
    ): Completable {
        return Single.fromCallable {
            if (source == currentSource) {
                if (currentPlayerIndex != nextPlayerIndex) {
                    // switch player to next one
                    currentPlayerIndex = nextPlayerIndex
                    setPlayer(currentPlayerIndex)
                }
            } else {
                if (currentPlayerIndex == START_PLAYER_INDEX) {
                    if (nextPlayerIndex != START_PLAYER_INDEX) {
                        nextPlayerIndex = START_PLAYER_INDEX
                    }
                } else {
                    // reset player for new source
                    currentPlayerIndex = START_PLAYER_INDEX
                    setPlayer(currentPlayerIndex)
                }
            }
            currentPlayer
        }.flatMapCompletable { player ->
            //workaround for android media player unsupported error case
            var ex: Exception? = null
            if (source == currentSource) {
                ex = previousPlayerException
                previousPlayerException = null
            }
            currentSource = source
            player.prepareToPlay(source, ex)
        }.retry(this::prepareRelaunchOnError)
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

    override fun setSkipSilenceEnabled(enabled: Boolean) {
        currentSkipSilenceEnabled = enabled
        currentPlayer.setSkipSilenceEnabled(enabled)
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
        currentPlayer.release()
        currentPlayer = createNewPlayerInstance(index)
        currentPlayerSubject.onNext(currentPlayer)
    }

    private fun createNewPlayerInstance(index: Int): AppMediaPlayer {
        val newPlayer = mediaPlayers[index]()
        newPlayer.setPlaybackSpeed(currentPlaySpeed)
        newPlayer.setSoundBalance(currentSoundBalance)
        newPlayer.setSkipSilenceEnabled(currentSkipSilenceEnabled)

        playerDisposable.clear()
        playerDisposable.add(newPlayer.getPlayerEventsObservable()
            .subscribe(this::onPlayerEventReceived)
        )
        return newPlayer
    }

    private fun onPlayerEventReceived(event: MediaPlayerEvent) {
        val eventToEmit = if (
            event is MediaPlayerEvent.Error && prepareRelaunchOnError(event.throwable)
        ) {
            MediaPlayerEvent.Error(RelaunchSourceException(event.throwable))
        } else {
            event
        }
        playerEventsSubject.onNext(eventToEmit)
    }

    private fun prepareRelaunchOnError(throwable: Throwable): Boolean {
        if (throwable is UnsupportedSourceException || throwable is PlayerOutOfMemoryException) {
            if (currentPlayerIndex >= 0 && currentPlayerIndex < mediaPlayers.lastIndex) {
                previousPlayerException = throwable as Exception
                nextPlayerIndex = currentPlayerIndex + 1
                return true
            }
        }
        return false
    }

    companion object {
        private const val START_PLAYER_INDEX = 0
    }

}