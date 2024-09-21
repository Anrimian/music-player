package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.composition.content.AcceptablePlayerException
import com.github.anrimian.musicplayer.domain.models.composition.content.CorruptedMediaFileException
import com.github.anrimian.musicplayer.domain.models.composition.content.FileReadTimeoutException
import com.github.anrimian.musicplayer.domain.models.composition.content.LocalSourceNotFoundException
import com.github.anrimian.musicplayer.domain.models.composition.content.RemoteSourceNotFoundException
import com.github.anrimian.musicplayer.domain.models.composition.content.TooLargeSourceException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper
import com.github.anrimian.musicplayer.domain.models.utils.toFileKey
import com.github.anrimian.musicplayer.domain.models.utils.toFileKeys
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import com.github.anrimian.musicplayer.domain.utils.rx.LazyBehaviorSubject
import com.github.anrimian.musicplayer.domain.utils.rx.doOnEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject

class LibraryPlayerInteractor(
    private val playerCoordinatorInteractor: PlayerCoordinatorInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    private val settingsRepository: SettingsRepository,
    private val playQueueRepository: PlayQueueRepository,
    private val libraryRepository: LibraryRepository,
    private val uiStateRepository: UiStateRepository,
    private val systemServiceController: SystemServiceController,
    private val analytics: Analytics,
) {

    private val playerSourceDisposable = CompositeDisposable()
    private val playerEventsDisposable = CompositeDisposable()

    private val preparationEventSubject = PublishSubject.create<Any>()

    private val trackPositionSubject = LazyBehaviorSubject(
        playQueueRepository.getCurrentQueueItemObservable()
            .flatMapSingle { event ->
                val item = event.playQueueItem
                if (item == null) {
                    Single.just(0L)
                } else {
                    playQueueRepository.getItemTrackPosition(item.itemId)
                }
            }.takeUntil(preparationEventSubject)
    )

    private val currentCompositionObservable = createCurrentCompositionObservable()
        .replay(1)
        .refCount()

    private var currentItem: PlayQueueItem? = null
    private var ignoredPreviousCurrentItem: PlayQueueItem? = null

    fun prepare() {
        checkSourcePrepare().onErrorComplete().subscribe()
    }

    @JvmOverloads
    fun setCompositionsQueueAndPlay(
        compositions: List<Composition>,
        firstPosition: Int = Constants.NO_POSITION,
    ): Completable {
        return setQueueAndPlay(compositions.map(Composition::id), firstPosition)
    }

    fun setQueueAndPlay(
        compositionIds: List<Long>,
        firstPosition: Int = Constants.NO_POSITION,
    ): Completable {
        if (compositionIds.isEmpty()) {
            return Completable.complete()
        }
        ignoredPreviousCurrentItem = currentItem
        return playQueueRepository.setPlayQueue(compositionIds, firstPosition)
            .andThen(checkSourcePrepare())
            .doOnComplete { playerCoordinatorInteractor.playAfterPrepare(PlayerType.LIBRARY) }
    }

    @Suppress("CheckResult")
    fun play(delay: Long = 0) {
        checkSourcePrepare().subscribe(
            { playerCoordinatorInteractor.play(PlayerType.LIBRARY, delay) },
            {}
        )
    }

    @Suppress("CheckResult")
    fun playOrPause() {
        checkSourcePrepare().subscribe(
            { playerCoordinatorInteractor.playOrPause(PlayerType.LIBRARY) },
            {}
        )
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

    fun skipToPrevious() {
        getTrackPosition().flatMapCompletable { trackPosition ->
            if (trackPosition > settingsRepository.skipConstraintMillis) {
                onSeekFinished(0)
                return@flatMapCompletable Completable.complete()
            }
            return@flatMapCompletable saveCurrentItemTrackPosition(0)
                .doOnComplete(playQueueRepository::skipToPrevious)
        }.subscribe()
    }

    fun skipToNext() {
        saveCurrentItemTrackPosition()
            .andThen(playQueueRepository.skipToNext())
            .subscribe()
    }

    fun skipToItem(itemId: Long) {
        saveCurrentItemTrackPosition()
            .doOnComplete { playQueueRepository.skipToItem(itemId) }
            .subscribe()
    }

    fun getRepeatModeObservable(): Observable<Int> = settingsRepository.repeatModeObservable

    fun getRepeatMode() = settingsRepository.repeatMode

    fun setRepeatMode(mode: Int) {
        settingsRepository.repeatMode = mode
    }

    fun changeRepeatMode() {
        settingsRepository.repeatMode = when (settingsRepository.repeatMode) {
            RepeatMode.PLAY_COMPOSITION_ONCE -> RepeatMode.REPEAT_PLAY_QUEUE
            RepeatMode.REPEAT_PLAY_QUEUE -> RepeatMode.REPEAT_COMPOSITION
            RepeatMode.REPEAT_COMPOSITION -> RepeatMode.NONE
            RepeatMode.NONE -> RepeatMode.PLAY_COMPOSITION_ONCE
            else -> RepeatMode.NONE
        }
    }

    fun isRandomPlayingEnabled() = settingsRepository.isRandomPlayingEnabled

    fun changeRandomMode() {
        playQueueRepository.setRandomPlayingEnabled(!settingsRepository.isRandomPlayingEnabled)
    }

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
        saveCurrentItemTrackPosition(position).subscribe()
        trackPositionSubject.onNext(position)
    }

    fun fastSeekForward() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(PlayerType.LIBRARY)) {
            playerCoordinatorInteractor.fastSeekForward(PlayerType.LIBRARY)
                .flatMapCompletable(this::saveCurrentItemTrackPosition)
                .subscribe()
        } else {
            seekBy(settingsRepository.rewindValueMillis)
        }
    }

    fun fastSeekBackward() {
        if (playerCoordinatorInteractor.isPlayerTypeActive(PlayerType.LIBRARY)) {
            playerCoordinatorInteractor.fastSeekBackward(PlayerType.LIBRARY)
                .flatMapCompletable(this::saveCurrentItemTrackPosition)
                .subscribe()
        } else {
            seekBy(-settingsRepository.rewindValueMillis)
        }
    }

    fun getTrackPositionObservable(): Observable<Long> {
        return trackPositionSubject.getObservable()
    }

    fun getPlayerStateObservable(): Observable<PlayerState> {
        return playerCoordinatorInteractor.getPlayerStateObservable(PlayerType.LIBRARY)
    }

    fun getIsPlayingStateObservable(): Observable<Boolean> {
        return playerCoordinatorInteractor.getIsPlayingStateObservable(PlayerType.LIBRARY)
    }

    fun getCompositionObservable(id: Long): Observable<Composition> {
        return libraryRepository.getCompositionObservable(id)
    }

    fun getCurrentQueueItemObservable(): Observable<PlayQueueEvent> {
        return playQueueRepository.getCurrentQueueItemObservable()
    }

    fun getCurrentCompositionObservable(): Observable<CurrentComposition> {
        return currentCompositionObservable
    }

    fun getCurrentCompositionLyrics(): Observable<Optional<String>> {
        return playQueueRepository.getCurrentQueueItemObservable()
            .switchMap { item ->
                val queueItem = item.playQueueItem
                return@switchMap if (queueItem == null) {
                    Observable.fromCallable { Optional(null) }
                } else {
                    libraryRepository.getLyricsObservable(queueItem.id)
                        .map(::Optional)
                }
            }
    }

    fun getCurrentItemPositionObservable(): Flowable<Int> {
        return playQueueRepository.getCurrentItemPositionObservable()
    }

    fun getPlayQueueObservable(): Flowable<List<PlayQueueItem>> {
        return playQueueRepository.getPlayQueueObservable()
    }

    fun deleteComposition(composition: Composition): Single<DeletedComposition> {
        return libraryRepository.deleteComposition(composition)
            .flatMap { c ->
                syncInteractor.onLocalFileDeleted(c.toFileKey()).andThen(Single.just(c))
            }
    }

    fun deleteCompositions(compositions: List<Composition>): Single<List<DeletedComposition>> {
        return libraryRepository.deleteCompositions(compositions)
            .doOnSuccess { c -> syncInteractor.onLocalFilesDeleted(c.toFileKeys()) }
    }

    fun removeQueueItem(item: PlayQueueItem): Completable {
        return playQueueRepository.removeQueueItem(item)
    }

    fun restoreDeletedItem(): Completable {
        return playQueueRepository.restoreDeletedItem()
    }

    fun swapItems(firstItem: PlayQueueItem, secondItem: PlayQueueItem): Completable {
        return playQueueRepository.swapItems(firstItem, secondItem)
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

    fun getPlayQueueSizeObservable(): Observable<Int> = playQueueRepository.getPlayQueueSizeObservable()

    fun getPlaybackSpeed() = uiStateRepository.currentPlaybackSpeed

    fun setPlaybackSpeed(speed: Float) {
        playerCoordinatorInteractor.setPlaybackSpeed(speed, PlayerType.LIBRARY)
        uiStateRepository.currentPlaybackSpeed = speed
    }

    fun getPlaybackSpeedObservable(): Observable<Float> = uiStateRepository.playbackSpeedObservable

    fun getSpeedChangeAvailableObservable(): Observable<Boolean> {
        return playerCoordinatorInteractor.getSpeedChangeAvailableObservable()
    }

    private fun checkSourcePrepare(): Completable {
        return Completable.create { emitter ->
            if (playerSourceDisposable.size() != 0) {
                emitter.onComplete()
                return@create
            }
            playQueueRepository.getCurrentQueueItemObservable()
                .flatMapSingle(this::onQueueItemChanged)
                .subscribe(
                    {
                        preparationEventSubject.onNext(Constants.TRIGGER)
                        subscribeOnPlayerEvents()
                        emitter.onComplete()
                    },
                    { t ->
                        playerEventsDisposable.clear()
                        systemServiceController.stopMusicService(true, true)
                        emitter.onError(t)
                    },
                    {},
                    playerSourceDisposable
                )
        }
    }

    private fun subscribeOnPlayerEvents() {
        if (playerEventsDisposable.size() != 0) {
            return
        }
        playerEventsDisposable.add(
            playerCoordinatorInteractor.getPlayerEventsObservable(PlayerType.LIBRARY)
                .subscribe(this::onAudioPlayerEventReceived)
        )
        playerEventsDisposable.add(
            playerCoordinatorInteractor.getPlayerStateObservable(PlayerType.LIBRARY)
                .doOnEvent(this::onPlayerStateChanged)
                .subscribe()
        )
        playerEventsDisposable.add(
            playerCoordinatorInteractor.getTrackPositionObservable(PlayerType.LIBRARY)
                .subscribe(trackPositionSubject::onNext)
        )
    }

    private fun onQueueItemChanged(event: PlayQueueEvent): Single<PlayQueueEvent> {
        val previousItem = currentItem
        val currentItem = event.playQueueItem
        this.currentItem = currentItem
        if (currentItem == null) {
            if (previousItem != null) {
                reset()
            }
            return Single.just(event)
        }
        if (previousItem != null && previousItem == currentItem) {
            //if file changed - re prepare with actual position
            //if not - check if changes exists - if true - update source with actual position
            val isFileChanged = PlayQueueItemHelper.hasSourceChanges(previousItem, currentItem)
            val isModelChanged = !PlayQueueItemHelper.areSourcesTheSame(previousItem, currentItem)
            if (isFileChanged || isModelChanged) {
                val source = LibraryCompositionSource(currentItem)
                if (isFileChanged) {
                    return getTrackPosition().doOnSuccess { trackPosition ->
                        //and cover will be not updated, f.e.?
                        playerCoordinatorInteractor.prepareToPlay(source, PlayerType.LIBRARY, trackPosition)
                    }.map { event }
                }
                playerCoordinatorInteractor.updateSource(source, PlayerType.LIBRARY)
            }
            return Single.just(event)
        }
        return playQueueRepository.getItemTrackPosition(currentItem.itemId)
            .doOnSuccess { trackPosition ->
                trackPositionSubject.onNext(trackPosition)
                playerCoordinatorInteractor.prepareToPlay(
                    LibraryCompositionSource(currentItem),
                    PlayerType.LIBRARY,
                    trackPosition
                )
            }.map { event }
    }

    private fun seekBy(rewindValueMillis: Long) {
        Single.zip(
            getCurrentComposition(),
            getTrackPosition()
        ) { compositionOpt, trackPosition ->
            val composition = compositionOpt.value ?: return@zip Completable.complete()

            val duration = composition.duration

            val targetPosition = (trackPosition + rewindValueMillis).coerceAtLeast(0)
            if (targetPosition > duration && duration != -1L) {
                return@zip Completable.complete()
            }
            Completable.fromAction { onSeekFinished(targetPosition) }
        }.flatMapCompletable { c -> c }
            .subscribe()
    }

    private fun getCurrentComposition(): Single<Optional<PlayQueueItem>> {
        return playQueueRepository.getCurrentQueueItemObservable()
            .onErrorComplete()
            .map { event -> Optional(event.playQueueItem) }
            .first(Optional())
    }

    private fun createCurrentCompositionObservable(): Observable<CurrentComposition> {
        return playQueueRepository.getCurrentQueueItemObservable()
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
    }

    private fun getTrackPosition(): Single<Long> {
        return if (playerCoordinatorInteractor.isPlayerTypeActive(PlayerType.LIBRARY)) {
            getActualTrackPosition()
        } else {
            return trackPositionSubject.getValue(0L)
        }
    }

    private fun getActualTrackPosition(): Single<Long> {
        return playerCoordinatorInteractor.getActualTrackPosition(PlayerType.LIBRARY)
    }

    private fun onAudioPlayerEventReceived(playerEvent: PlayerEvent) {
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
                clearCompositionError(source)
            }
        }
    }

    private fun clearCompositionError(source: LibraryCompositionSource) {
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
        playQueueRepository.isCurrentCompositionAtEndOfQueue()
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

    private fun onPlayerStateChanged(playerState: PlayerState): Completable? {
        return when(playerState) {
            PlayerState.STOP -> {
                trackPositionSubject.onNext(0)
                saveCurrentItemTrackPosition(0)
            }
            PlayerState.PAUSE -> {
                getActualTrackPosition().flatMapCompletable(this::saveCurrentItemTrackPosition)
            }
            else -> null
        }
    }

    private fun saveCurrentItemTrackPosition(): Completable {
        return Single.zip(
            getCurrentComposition(),
            getTrackPosition()
        ) { compositionOpt, trackPosition ->
            val composition = compositionOpt.value ?: return@zip Completable.complete()
            val positionToSave = if (
                trackPosition < settingsRepository.skipSaveStartMillis
                || trackPosition > composition.duration - settingsRepository.skipSaveEndMillis
            ) {
                0L
            } else {
                trackPosition
            }
            saveCurrentItemTrackPosition(positionToSave)
        }.flatMapCompletable { c -> c }
    }

    private fun saveCurrentItemTrackPosition(trackPosition: Long): Completable {
        return playQueueRepository.setCurrentItemTrackPosition(trackPosition)
            .onErrorComplete()
    }

    private fun resetItemTrackPosition(itemId: Long): Completable {
        return playQueueRepository.setItemTrackPosition(itemId, 0L)
            .onErrorComplete()
    }

    private fun toCorruptionType(throwable: Throwable): CorruptionType {
        return when (throwable) {
            is UnsupportedSourceException -> CorruptionType.UNSUPPORTED
            is LocalSourceNotFoundException -> CorruptionType.NOT_FOUND
            is RemoteSourceNotFoundException -> CorruptionType.SOURCE_NOT_FOUND
            is TooLargeSourceException -> CorruptionType.TOO_LARGE_SOURCE
            is CorruptedMediaFileException -> CorruptionType.FILE_IS_CORRUPTED
            is FileReadTimeoutException -> CorruptionType.FILE_READ_TIMEOUT
            else -> CorruptionType.UNKNOWN
        }
    }

    private fun onCompositionPlayFinished() {
        if (settingsRepository.repeatMode == RepeatMode.PLAY_COMPOSITION_ONCE) {
            onSeekFinished(0)
            pause()
            return
        }
        if (settingsRepository.repeatMode == RepeatMode.REPEAT_COMPOSITION) {
            onSeekFinished(0)
            return
        }
        saveCurrentItemTrackPosition(0)
            .andThen(playQueueRepository.getNextQueueItemId())
            .flatMapCompletable(this::resetItemTrackPosition)
            .andThen(playQueueRepository.skipToNext())
            .doOnSuccess(this::onAutoSkipNextFinished)
            .subscribe()
    }

    private fun onAutoSkipNextFinished(queuePosition: Int) {
        if (queuePosition == 0) {
            if (settingsRepository.repeatMode == RepeatMode.NONE) {
                pause()
            }
        }
    }

}