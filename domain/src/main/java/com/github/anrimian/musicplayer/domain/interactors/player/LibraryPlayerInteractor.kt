package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.composition.content.*
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

class LibraryPlayerInteractor(
    private val playerCoordinatorInteractor: PlayerCoordinatorInteractor,
    private val settingsRepository: SettingsRepository,
    private val playQueueRepository: PlayQueueRepository,
    private val libraryRepository: LibraryRepository,
    private val uiStateRepository: UiStateRepository,
    private val analytics: Analytics,
) {

    private val playerDisposable = CompositeDisposable()
    private var currentQueueItemDisposable: Disposable? = null

    private val trackPositionSubject = PublishSubject.create<Long>()
    private val currentCompositionSubject = BehaviorSubject.create<CurrentComposition>()

    private var currentItem: PlayQueueItem? = null
    private var ignoredPreviousCurrentItem: PlayQueueItem? = null

    init {
        playerDisposable.add(
            playQueueRepository.currentQueueItemObservable
                .subscribe(this::onQueueItemChanged)
        )
        playerDisposable.add(
            playerCoordinatorInteractor.getPlayerEventsObservable(PlayerType.LIBRARY)
                .subscribe(this::onMusicPlayerEventReceived)
        )
        playerDisposable.add(
            playerCoordinatorInteractor.getPlayerStateObservable(PlayerType.LIBRARY)
                .subscribe(this::onPlayerStateChanged)
        )
        subscribeOnCurrentComposition()
    }

    @JvmOverloads
    fun startPlaying(
        compositions: List<Composition>,
        firstPosition: Int = Constants.NO_POSITION,
    ) {
        ignoredPreviousCurrentItem = currentItem
        playQueueRepository.setPlayQueue(compositions, firstPosition)
            .doOnComplete { playerCoordinatorInteractor.playAfterPrepare(PlayerType.LIBRARY) }
            .doOnError(analytics::processNonFatalError)
            .onErrorComplete()
            .subscribe()
    }

    @JvmOverloads
    fun play(delay: Long = 0) {
        playerCoordinatorInteractor.play(PlayerType.LIBRARY, delay)
    }

    fun playOrPause() {
        playerCoordinatorInteractor.playOrPause(PlayerType.LIBRARY)
    }

    fun pause() {
        playerCoordinatorInteractor.pause(PlayerType.LIBRARY)
    }

    fun stop() {
        playerCoordinatorInteractor.stop(PlayerType.LIBRARY)
    }

    fun reset() {
        playerCoordinatorInteractor.reset(PlayerType.LIBRARY)
    }

    fun fastSeekForward() {
        playerCoordinatorInteractor.fastSeekForward(PlayerType.LIBRARY)
            .subscribe(uiStateRepository::setTrackPosition)
    }

    fun fastSeekBackward() {
        playerCoordinatorInteractor.fastSeekBackward(PlayerType.LIBRARY)
            .subscribe(uiStateRepository::setTrackPosition)
    }

    fun skipToPrevious() {
        getActualTrackPosition().subscribe { position ->
            if (position > settingsRepository.skipConstraintMillis) {
                onSeekFinished(0)
                return@subscribe
            }
            playQueueRepository.skipToPrevious()
        }
    }

    fun skipToNext() {
        playQueueRepository.skipToNext().subscribe()
    }

    fun skipToItem(itemId: Long) {
        playQueueRepository.skipToItem(itemId)
    }

    fun getRepeatModeObservable(): Observable<Int> = settingsRepository.repeatModeObservable

    fun getRepeatMode() = settingsRepository.repeatMode

    fun setRepeatMode(mode: Int) {
        settingsRepository.repeatMode = mode
    }

    fun changeRepeatMode() {
        settingsRepository.repeatMode = when (settingsRepository.repeatMode) {
            RepeatMode.NONE -> RepeatMode.REPEAT_PLAY_QUEUE
            RepeatMode.REPEAT_PLAY_QUEUE -> RepeatMode.REPEAT_COMPOSITION
            RepeatMode.REPEAT_COMPOSITION -> RepeatMode.NONE
            else -> RepeatMode.NONE
        }
    }

    fun isRandomPlayingEnabled() = settingsRepository.isRandomPlayingEnabled

    fun setRandomPlayingEnabled(enabled: Boolean) {
        playQueueRepository.setRandomPlayingEnabled(enabled)
    }

    fun getRandomPlayingObservable(): Observable<Boolean> = settingsRepository.randomPlayingObservable

    fun onSeekStarted() {
        playerCoordinatorInteractor.onSeekStarted(PlayerType.LIBRARY)
    }

    fun seekTo(position: Long) {
        trackPositionSubject.onNext(position)
    }

    fun onSeekFinished(position: Long) {
        playerCoordinatorInteractor.onSeekFinished(position, PlayerType.LIBRARY)
        uiStateRepository.trackPosition = position
        trackPositionSubject.onNext(position)
    }

    fun getTrackPositionObservable(): Observable<Long> {
        return playerCoordinatorInteractor.getTrackPositionObservable(PlayerType.LIBRARY)
            .mergeWith(trackPositionSubject)
            .startWith(getActualTrackPosition())
    }

    fun getPlayerStateObservable(): Observable<PlayerState> {
        return playerCoordinatorInteractor.getPlayerStateObservable(PlayerType.LIBRARY)
    }

    fun getIsPlayingStateObservable(): Observable<Boolean> {
        return playerCoordinatorInteractor.getIsPlayingStateObservable(PlayerType.LIBRARY)
    }

    fun getPlayerState(): PlayerState {
        return playerCoordinatorInteractor.getPlayerState(PlayerType.LIBRARY)
    }

    fun getCompositionObservable(id: Long): Observable<Composition> {
        return libraryRepository.getCompositionObservable(id)
    }

    fun getCurrentQueueItemObservable(): Observable<PlayQueueEvent> {
        return playQueueRepository.currentQueueItemObservable
    }

    fun getCurrentCompositionObservable(): Observable<CurrentComposition> {
        return currentCompositionSubject
    }

    fun getCurrentCompositionLyrics(): Observable<Optional<String>> {
        return playQueueRepository.currentQueueItemObservable
            .switchMap { item ->
                val queueItem = item.playQueueItem
                return@switchMap if (queueItem == null) {
                    Observable.fromCallable { Optional(null) }
                } else {
                    libraryRepository.getLyricsObservable(queueItem.composition.id)
                        .map(::Optional)
                }
            }
    }

    fun getCurrentItemPositionObservable(): Flowable<Int> {
        return playQueueRepository.currentItemPositionObservable
    }

    fun getPlayQueueObservable(): Flowable<List<PlayQueueItem>> {
        return playQueueRepository.playQueueObservable
    }

    fun deleteComposition(composition: Composition): Completable {
        return libraryRepository.deleteComposition(composition)
    }

    fun deleteCompositions(compositions: List<Composition>): Completable {
        return libraryRepository.deleteCompositions(compositions)
    }

    fun removeQueueItem(item: PlayQueueItem): Completable {
        return playQueueRepository.removeQueueItem(item)
    }

    fun restoreDeletedItem(): Completable {
        return playQueueRepository.restoreDeletedItem()
    }

    fun swapItems(firstItem: PlayQueueItem, secondItem: PlayQueueItem) {
        playQueueRepository.swapItems(firstItem, secondItem).subscribe()
    }

    fun addCompositionsToPlayNext(compositions: List<Composition>): Single<List<Composition>> {
        return playQueueRepository.addCompositionsToPlayNext(compositions)
            .toSingleDefault(compositions)
    }

    fun addCompositionsToEnd(compositions: List<Composition>): Single<List<Composition>> {
        return playQueueRepository.addCompositionsToEnd(compositions)
            .toSingleDefault(compositions)
    }

    fun clearPlayQueue(): Completable {
        return playQueueRepository.clearPlayQueue()
    }

    fun getPlayQueueSizeObservable(): Observable<Int> = playQueueRepository.playQueueSizeObservable

    fun getPlaybackSpeed() = uiStateRepository.currentPlaybackSpeed

    fun setPlaybackSpeed(speed: Float) {
        playerCoordinatorInteractor.setPlaybackSpeed(speed, PlayerType.LIBRARY)
        uiStateRepository.currentPlaybackSpeed = speed
    }

    fun getPlaybackSpeedObservable(): Observable<Float> = uiStateRepository.playbackSpeedObservable

    fun getSpeedChangeAvailableObservable(): Observable<Boolean> {
        return playerCoordinatorInteractor.getSpeedChangeAvailableObservable()
    }

    fun changeRandomMode() {
        settingsRepository.isRandomPlayingEnabled = !settingsRepository.isRandomPlayingEnabled
    }

    private fun subscribeOnCurrentComposition() {
        currentQueueItemDisposable = getCurrentQueueItemObservable()
            .distinctUntilChanged()
            .switchMap { event ->
                getIsPlayingStateObservable()
                    .distinctUntilChanged()
                    .filter {
                        event.playQueueItem == null || event.playQueueItem != ignoredPreviousCurrentItem
                    }
                    .map { state -> CurrentComposition(event, state) }
            }
            .distinctUntilChanged()
            .subscribe(currentCompositionSubject::onNext)
    }

    private fun onQueueItemChanged(compositionEvent: PlayQueueEvent) {
        val previousItem = currentItem
        val currentItem = compositionEvent.playQueueItem
        this.currentItem = currentItem
        if (currentItem == null) {
            if (previousItem != null) {
                reset()
            }
            return
        }
        val currentComposition = currentItem.composition
        if (previousItem != null && previousItem == currentItem) {
            //if file changed - re prepare with actual position
            //if not - check if changes exists - if true - update source with actual position
            val isFileChanged = PlayQueueItemHelper.hasSourceChanges(previousItem, currentItem)
            val isModelChanged = !PlayQueueItemHelper.areSourcesTheSame(previousItem, currentItem)
            if (isFileChanged || isModelChanged) {
                getActualTrackPosition().subscribe { actualTrackPosition ->
                    val source = LibraryCompositionSource(currentComposition)
                    if (isFileChanged) {
                        playerCoordinatorInteractor.prepareToPlay(source, PlayerType.LIBRARY, actualTrackPosition)
                        return@subscribe   //and cover will be not updated, f.e.?
                    }
                    playerCoordinatorInteractor.updateSource(source, PlayerType.LIBRARY)
                }
            }
            return
        }
        playerCoordinatorInteractor.prepareToPlay(
            LibraryCompositionSource(currentComposition),
            PlayerType.LIBRARY,
            compositionEvent.trackPosition
        )
    }

    private fun getActualTrackPosition(): Single<Long> {
        return playerCoordinatorInteractor.getActualTrackPosition(PlayerType.LIBRARY)
            .map { position ->
                if (position == -1L) {
                    return@map uiStateRepository.trackPosition
                }
                return@map position
            }
    }

    private fun onMusicPlayerEventReceived(playerEvent: PlayerEvent) {
        val source = playerEvent.source
        if (source !is LibraryCompositionSource) {
            return
        }

        when (playerEvent) {
            is PlayerEvent.FinishedEvent -> {
                onCompositionPlayFinished()
            }
            is PlayerEvent.ErrorEvent -> {
                handleErrorWithComposition(playerEvent.throwable, source)
            }
            is PlayerEvent.PreparedEvent -> {
                cleanErrorAboutComposition(source)
//                if (playOnNextPrepareEvent) {
//                    setPlayOnNextPrepareEvent(false)
//                    playerCoordinatorInteractor.play(PlayerType.LIBRARY)
//                }
                if (currentQueueItemDisposable == null) {
                    subscribeOnCurrentComposition()
                }
            }
        }
    }

    private fun cleanErrorAboutComposition(source: LibraryCompositionSource) {
        val composition = source.composition
        if (composition.corruptionType != null) {
            writeErrorAboutComposition(composition, null)
        }
    }

    private fun handleErrorWithComposition(throwable: Throwable, source: LibraryCompositionSource) {
        if (throwable is AcceptablePlayerException) {
            playerCoordinatorInteractor.error(PlayerType.LIBRARY, throwable.cause)
            return
        }
        playQueueRepository.isCurrentCompositionAtEndOfQueue
            .flatMapCompletable { isLast ->
                return@flatMapCompletable if (isLast) {
                    Completable.fromAction { stop() }
                } else {
                    playQueueRepository.skipToNext().ignoreElement()
                }
            }
            .doOnError(analytics::processNonFatalError)
            .onErrorComplete()
            .doOnComplete {
                val corruptionType = toCorruptionType(throwable)
                writeErrorAboutComposition(source.composition, corruptionType)
            }
            .subscribe()
    }

    private fun writeErrorAboutComposition(
        composition: Composition,
        corruptionType: CorruptionType?,
    ) {
        libraryRepository.writeErrorAboutComposition(corruptionType, composition)
            .doOnError(analytics::processNonFatalError)
            .onErrorComplete()
            .subscribe()
    }

    private fun onPlayerStateChanged(playerState: PlayerState) {
        when(playerState) {
            PlayerState.STOP -> uiStateRepository.trackPosition = 0
            PlayerState.PAUSE -> {
                playerCoordinatorInteractor.getActualTrackPosition(PlayerType.LIBRARY)
                    .subscribe(uiStateRepository::setTrackPosition)
            }
            else -> {}
        }
    }

    private fun toCorruptionType(throwable: Throwable): CorruptionType {
        return when (throwable) {
            is UnsupportedSourceException -> CorruptionType.UNSUPPORTED
            is LocalSourceNotFoundException -> CorruptionType.NOT_FOUND
            is RemoteSourceNotFoundException -> CorruptionType.SOURCE_NOT_FOUND
            is TooLargeSourceException -> CorruptionType.TOO_LARGE_SOURCE
            is CorruptedMediaFileException -> CorruptionType.FILE_IS_CORRUPTED
            else -> CorruptionType.UNKNOWN
        }
    }

    private fun onCompositionPlayFinished() {
        if (settingsRepository.repeatMode == RepeatMode.REPEAT_COMPOSITION) {
            onSeekFinished(0)
            return
        }
        playQueueRepository.skipToNext()
            .doOnSuccess(this::onAutoSkipNextFinished)
            .subscribe()
    }

    private fun onAutoSkipNextFinished(currentPosition: Int) {
        if (currentPosition == 0) {
            if (settingsRepository.repeatMode == RepeatMode.NONE) {
                stop()
            } else {
                onSeekFinished(0)
            }
        }
    }

}