package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.LinkedList

class PlayerCoordinatorInteractor(
    private val playerInteractor: PlayerInteractor,
    private val uiStateRepository: UiStateRepository
) {

    private val preparedSourcesMap = HashMap<PlayerType, SourceInfo>()
    private val cleanupCallbacksMap = HashMap<PlayerType, () -> Unit>()

    @Volatile
    private var activePlayerType: PlayerType? = null
    private val activePlayerTypeSubject = BehaviorSubject.createDefault<Opt<PlayerType>>(Opt())
    private val playerActivationHistory = LinkedList<PlayerType>()
    private val stateLock = Any()

    fun play(playerType: PlayerType, delay: Long = 0L) {
        synchronized(stateLock) {
            applyPlayerType(playerType)
            playerInteractor.play(delay)
        }
    }

    fun playAfterPrepare(playerType: PlayerType) {
        synchronized(stateLock) {
            applyPlayerType(playerType)
            playerInteractor.playAfterPrepare()
        }
    }

    fun updateSource(source: CompositionSource, playerType: PlayerType) {
        synchronized(stateLock) {
            val currentSourceInfo = preparedSourcesMap[playerType]
            if (currentSourceInfo?.source == source) {
                currentSourceInfo.source = source
            }
            if (playerType == activePlayerType) {
                playerInteractor.updateSource(source)
            }
        }
    }

    fun playOrPause(playerType: PlayerType) {
        synchronized(stateLock) {
            applyPlayerType(playerType)
            playerInteractor.playOrPause()
        }
    }

    fun stop(playerType: PlayerType) {
        synchronized(stateLock) {
            if (playerType == activePlayerType) {
                playerInteractor.stop()
            }
        }
    }

    fun pause(playerType: PlayerType) {
        synchronized(stateLock) {
            if (playerType == activePlayerType) {
                playerInteractor.pause()
            }
        }
    }

    fun error(playerType: PlayerType, throwable: Throwable) {
        synchronized(stateLock) {
            if (playerType == activePlayerType) {
                playerInteractor.error(throwable)
            }
        }
    }

    fun reset(playerType: PlayerType, fallbackToPrevious: Boolean = false) {
        synchronized(stateLock) {
            if (activePlayerType == null) {
                playerInteractor.reset()
                return
            }
            preparedSourcesMap.remove(playerType)
            if (activePlayerType == playerType) {
                if (playerActivationHistory.isNotEmpty()) {
                    playerActivationHistory.removeLast()
                }

                cleanupCallbacksMap[playerType]?.invoke()

                val prevPlayerType = playerActivationHistory.lastOrNull()
                if (prevPlayerType == null || !fallbackToPrevious) {
                    activePlayerType = null
                    activePlayerTypeSubject.onNext(Opt())
                    playerInteractor.reset()
                } else {
                    applyPlayerType(prevPlayerType)
                }
            }
        }
    }

    fun fastSeekForward(playerType: PlayerType): Single<Long> {
        synchronized(stateLock) {
            if (playerType == activePlayerType) {
                return playerInteractor.fastSeekForward()
            }
            //do not update position if player is not active
            return getActualTrackPosition(playerType)
        }
    }

    fun fastSeekBackward(playerType: PlayerType): Single<Long> {
        synchronized(stateLock) {
            if (playerType == activePlayerType) {
                return playerInteractor.fastSeekBackward()
            }
            //do not update position if player is not active
            return getActualTrackPosition(playerType)
        }
    }

    fun prepareToPlay(
        compositionSource: CompositionSource,
        playerType: PlayerType,
        startPosition: Long
    ) {
        synchronized(stateLock) {
            preparedSourcesMap[playerType] = SourceInfo(compositionSource, startPosition)
            if (playerType == activePlayerType) {
                playerInteractor.prepareToPlay(compositionSource, startPosition)
            } else if (activePlayerType == null) {
                applyPlayerType(playerType)
            }
        }
    }

    fun onSeekStarted(playerType: PlayerType) {
        synchronized(stateLock) {
            if (activePlayerType == playerType) {
                playerInteractor.onSeekStarted()
            }
        }
    }

    fun onSeekFinished(position: Long, playerType: PlayerType) {
        synchronized(stateLock) {
            if (activePlayerType == playerType) {
                playerInteractor.onSeekFinished(position)
            } else {
                preparedSourcesMap[playerType]?.trackPosition = position
            }
        }
    }

    fun setPlaybackSpeed(speed: Float, playerType: PlayerType) {
        synchronized(stateLock) {
            if (activePlayerType == playerType) {
                playerInteractor.setPlaybackSpeed(speed)
            }
        }
    }

    fun registerCleanupCallback(playerType: PlayerType, callback: () -> Unit) {
        synchronized(stateLock) {
            cleanupCallbacksMap[playerType] = callback
        }
    }

    fun getPlayerEventsObservable(playerType: PlayerType): Observable<PlayerEvent> {
        return playerInteractor.getPlayerEventsObservable()
            .filter { isPlayerTypeActive(playerType) }
    }

    fun getTrackPositionObservable(playerType: PlayerType): Observable<Long> {
        return playerInteractor.getTrackPositionObservable()
            .filter { isPlayerTypeActive(playerType) }
    }

    fun getTrackPositionChangeObservable(playerType: PlayerType): Observable<Long> {
        return playerInteractor.getTrackPositionChangeObservable()
            .filter { isPlayerTypeActive(playerType) }
    }

    fun getPlayerStateObservable(playerType: PlayerType): Observable<PlayerState> {
        return playerInteractor.getPlayerStateObservable()
            .map { state -> if (isPlayerTypeActive(playerType)) state else PlayerState.IDLE }
    }

    fun getIsPlayingStateObservable(playerType: PlayerType): Observable<Boolean> {
        return playerInteractor.getIsPlayingStateObservable()
            .map { state -> if (isPlayerTypeActive(playerType)) state else false }
    }

    fun isPlayerTypeActive(playerType: PlayerType): Boolean {
        return activePlayerType == playerType
    }

    fun getSpeedChangeAvailableObservable() = playerInteractor.getSpeedChangeAvailableObservable()

    fun getActivePlayerTypeObservable(): Observable<PlayerType> {
        return activePlayerTypeSubject.flatMap { opt ->
            val value = opt.value
            return@flatMap if (value != null) {
                Observable.just(value)
            } else {
                Observable.never()
            }
        }
    }

    fun getActualTrackPosition(playerType: PlayerType): Single<Long> {
        synchronized(stateLock) {
            return if (isPlayerTypeActive(playerType)) {
                playerInteractor.getTrackPosition()
            } else {
                Single.just(preparedSourcesMap[playerType]?.trackPosition ?: -1L)
            }
        }
    }

    @Suppress("CheckResult")
    private fun applyPlayerType(playerType: PlayerType) {
        if (activePlayerType == playerType) {
            return
        }
        if (activePlayerType != null) {
            playerInteractor.pause()
            val oldSource = preparedSourcesMap[activePlayerType]
            if (oldSource != null) {
                playerInteractor.getTrackPosition()
                    .subscribe { position -> oldSource.trackPosition = position }
            }
            cleanupCallbacksMap[activePlayerType]?.invoke()
        } else {
            cleanupCallbacksMap.forEach { (type, callback) ->
                if (type != playerType) {
                    callback.invoke()
                }
            }
        }

        initializePlayerType(playerType)
        activePlayerType = playerType
        playerActivationHistory.remove(playerType)
        playerActivationHistory.add(playerType)
        activePlayerTypeSubject.onNext(Opt(activePlayerType))

        val sourceInfo = preparedSourcesMap[playerType]
        if (sourceInfo != null) {
            playerInteractor.prepareToPlay(sourceInfo.source, sourceInfo.trackPosition)
        }
    }

    private fun initializePlayerType(playerType: PlayerType) {
        when (playerType) {
            PlayerType.LIBRARY -> {
                playerInteractor.setPlaybackSpeed(uiStateRepository.currentPlaybackSpeed)
            }
            PlayerType.EXTERNAL -> {
                playerInteractor.setPlaybackSpeed(1f)
            }
        }
    }

    private inner class SourceInfo(
        var source: CompositionSource,
        var trackPosition: Long
    )

}