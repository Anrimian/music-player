package com.github.anrimian.musicplayer.domain.interactors.player

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
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
import com.github.anrimian.musicplayer.domain.models.exceptions.NotAllowedPathException
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
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import com.github.anrimian.musicplayer.domain.utils.rx.LazyBehaviorSubject
import com.github.anrimian.musicplayer.domain.utils.rx.attachGateObservable
import com.github.anrimian.musicplayer.domain.utils.rx.doOnEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Notification
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicBoolean

class LibraryPlayerInteractor(
    private val playerCoordinatorInteractor: PlayerCoordinatorInteractor,
    private val syncInteractor: SyncInteractor<FileKey, *, Long>,
    private val settingsRepository: SettingsRepository,
    private val playQueueRepository: PlayQueueRepository,
    private val libraryRepository: LibraryRepository,
    private val uiStateRepository: UiStateRepository,
    private val analytics: Analytics,
) {

    private val playerSourceDisposable = CompositeDisposable()
    private val playerEventsDisposable = CompositeDisposable()

    private val isAnySourcePrepared = AtomicBoolean(false)
    private val preparationOutcomeSubject = PublishSubject.create<Notification<Any>>()

    private val trackPositionSubject = LazyBehaviorSubject(
        playQueueRepository.getCurrentQueueItemObservable()
            .flatMapSingle { event ->
                val item = event.playQueueItem
                if (item == null) {
                    Single.just(0L)
                } else {
                    playQueueRepository.getItemTrackPosition(item.itemId)
                }
            }.takeUntil(preparationOutcomeSubject)
    )

    private val currentCompositionGateSubject = BehaviorSubject.createDefault(false)

    private val currentCompositionObservable = createCurrentCompositionObservable()
        .replay(1)
        .refCount()

    private var currentItem: PlayQueueItem? = null

    fun prepare() {
        ensureSourceReady().doOnError(analytics::processNonFatalError).onErrorComplete().subscribe()
    }

    @JvmOverloads
    fun setCompositionsQueueAndPlay(
        compositions: List<CompositionModel>,
        firstPosition: Int = Constants.NO_POSITION,
    ): Completable {
        return setQueueAndPlay(compositions.map(CompositionModel::id), firstPosition)
    }

    fun setQueueAndPlay(
        compositionIds: List<Long>,
        firstPosition: Int = Constants.NO_POSITION,
    ): Completable {
        if (compositionIds.isEmpty()) {
            return Completable.complete()
        }
        val ignoredPreviousCurrentItem = currentItem
        return playQueueRepository.setPlayQueue(compositionIds, firstPosition)
            .andThen(
                playQueueRepository.getCurrentQueueItemObservable()
                    .filter { event -> event.playQueueItem != ignoredPreviousCurrentItem }
                    .take(1)
                    .ignoreElements()
            )
            .andThen(ensureSourceReady())
            .doOnSuccess { playerCoordinatorInteractor.playAfterPrepare(PlayerType.LIBRARY) }
            .ignoreElement()
            .doOnSubscribe { currentCompositionGateSubject.onNext(true) }
            .doFinally { currentCompositionGateSubject.onNext(false) }
    }

    @Suppress("CheckResult")
    fun play(delay: Long = 0) {
        ensureSourceReady().subscribe(
            { playerCoordinatorInteractor.play(PlayerType.LIBRARY, delay) },
            {}
        )
    }

    @Suppress("CheckResult")
    fun playOrPause() {
        ensureSourceReady().subscribe(
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

    fun getTrackPosition(): Single<Long> {
        return if (playerCoordinatorInteractor.isPlayerTypeActive(PlayerType.LIBRARY)) {
            getActualTrackPosition()
        } else {
            trackPositionSubject.getValue(0L)
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

    fun getCurrentCompositionLyrics(): Observable<Opt<String>> {
        return playQueueRepository.getCurrentQueueItemObservable()
            .switchMap { item ->
                val queueItem = item.playQueueItem
                return@switchMap if (queueItem == null) {
                    Observable.fromCallable { Opt(null) }
                } else {
                    libraryRepository.getLyricsObservable(queueItem.id)
                        .map(::Opt)
                }
            }
    }

    fun getCurrentItemPositionObservable(): Flowable<Int> {
        return playQueueRepository.getCurrentItemPositionObservable()
    }

    fun getPlayQueueObservable(): Flowable<List<PlayQueueItem>> {
        return playQueueRepository.getPlayQueueObservable()
    }

    fun getWindowPlayQueueObservable(
        startOffset: Int,
        endOffset: Int,
    ): Observable<List<PlayQueueItem>> {
        return playQueueRepository.getWindowPlayQueueObservable(startOffset, endOffset)
    }

    fun deleteComposition(composition: Composition): Single<DeletedComposition> {
        return libraryRepository.deleteComposition(composition)
            .flatMap { c ->
                syncInteractor.onLocalFileDeleted(c.toFileKey()).andThen(Single.just(c))
            }
    }

    fun deleteCompositions(compositions: List<CompositionModel>): Single<List<DeletedComposition>> {
        return libraryRepository.deleteCompositions(compositions)
            .flatMap { c ->
                syncInteractor.onLocalFilesDeleted(c.toFileKeys()).andThen(Single.just(c))
            }
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

    fun addCompositionsToPlayNext(compositions: List<CompositionModel>): Single<List<CompositionModel>> {
        return playQueueRepository.addCompositionsToPlayNext(compositions)
            .toSingleDefault(compositions)
    }

    fun addCompositionsToEnd(compositions: List<CompositionModel>): Single<List<CompositionModel>> {
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

    private fun ensureSourceReady(): Maybe<Any> {
        return Maybe.defer {
            if (isAnySourcePrepared.get()) {
                Maybe.just(Constants.TRIGGER)
            } else {
                Maybe.create { emitter ->
                    // use this approach to subscribe on result before any source will be prepared
                    val disposable = preparationOutcomeSubject
                        .take(1)
                        .subscribe(
                            { notification ->
                                if (notification.isOnNext) {
                                    emitter.onSuccess(Constants.TRIGGER)
                                } else if (notification.isOnError) {
                                    emitter.onError(notification.error!!)
                                } else {
                                    emitter.onComplete()
                                }
                            },
                            emitter::onError
                        )
                    emitter.setCancellable { disposable.dispose() }
                    subscribeOnCurrentSource()
                }
            }
        }
    }

    private fun subscribeOnCurrentSource() {
        if (playerSourceDisposable.size() != 0) {
            return
        }
        playQueueRepository.getCurrentQueueItemObservable()
            .flatMapSingle(this::onQueueItemChanged)
            .subscribe(
                { isSourcePrepared ->
                    if (isSourcePrepared) {
                        subscribeOnPlayerEvents()
                        isAnySourcePrepared.set(true)
                        preparationOutcomeSubject.onNext(Notification.createOnNext(Constants.TRIGGER))
                    } else {
                        playerEventsDisposable.clear()
                        playerCoordinatorInteractor.reset(PlayerType.LIBRARY)
                        isAnySourcePrepared.set(false)
                        playerSourceDisposable.clear()
                        preparationOutcomeSubject.onNext(Notification.createOnComplete())
                    }
                },
                { t ->
                    playerEventsDisposable.clear()
                    playerCoordinatorInteractor.reset(PlayerType.LIBRARY)
                    isAnySourcePrepared.set(false)
                    playerSourceDisposable.clear()
                    preparationOutcomeSubject.onNext(Notification.createOnError(t))
                },
                {},
                playerSourceDisposable
            )
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

    private fun onQueueItemChanged(event: PlayQueueEvent): Single<Boolean> {
        val previousItem = currentItem
        val currentItem = event.playQueueItem
        this.currentItem = currentItem
        if (currentItem == null) {
            return Single.just(false)
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
                    }.map { true }
                }
                playerCoordinatorInteractor.updateSource(source, PlayerType.LIBRARY)
            }
            return Single.just(true)
        }
        return playQueueRepository.getItemTrackPosition(currentItem.itemId)
            .doOnSuccess { trackPosition ->
                trackPositionSubject.onNext(trackPosition)
                playerCoordinatorInteractor.prepareToPlay(
                    LibraryCompositionSource(currentItem),
                    PlayerType.LIBRARY,
                    trackPosition
                )
            }.map { true }
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

    private fun getCurrentComposition(): Single<Opt<PlayQueueItem>> {
        return playQueueRepository.getCurrentQueueItemObservable()
            .doOnError(analytics::processNonFatalError)
            .onErrorComplete()
            .map { event -> Opt(event.playQueueItem) }
            .first(Opt())
    }

    private fun createCurrentCompositionObservable(): Observable<CurrentComposition> {
        return Observable.combineLatest(
            playQueueRepository.getCurrentQueueItemObservable(),
            getIsPlayingStateObservable(),
            { playQueueEvent, isPlaying -> CurrentComposition(playQueueEvent.playQueueItem, isPlaying) }
        ).distinctUntilChanged()
            .attachGateObservable(currentCompositionGateSubject)
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
            .doOnError(analytics::processNonFatalError)
            .onErrorComplete()
    }

    private fun resetItemTrackPosition(itemId: Long): Completable {
        return playQueueRepository.setItemTrackPosition(itemId, 0L)
            .doOnError(analytics::processNonFatalError)
            .onErrorComplete()
    }

    private fun toCorruptionType(throwable: Throwable): CorruptionType {
        return when (throwable) {
            is UnsupportedSourceException -> CorruptionType.UNSUPPORTED
            is LocalSourceNotFoundException -> CorruptionType.NOT_FOUND
            is RemoteSourceNotFoundException -> CorruptionType.NOT_FOUND_IN_ALL_STORAGES
            is TooLargeSourceException -> CorruptionType.TOO_LARGE_SOURCE
            is CorruptedMediaFileException -> CorruptionType.FILE_IS_CORRUPTED
            is FileReadTimeoutException -> CorruptionType.FILE_READ_TIMEOUT
            is NotAllowedPathException -> CorruptionType.NOT_ALLOWED_PATH
            else -> CorruptionType.UNKNOWN
        }
    }

    private fun onCompositionPlayFinished() {
        if (settingsRepository.repeatMode == RepeatMode.PLAY_COMPOSITION_ONCE) {
            pause()
            onSeekFinished(0)
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
                onSeekFinished(0)
            } else if (settingsRepository.repeatMode == RepeatMode.REPEAT_PLAY_QUEUE) {
                onSeekFinished(0)
            }
        }
    }

}