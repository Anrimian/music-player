package com.github.anrimian.musicplayer.infrastructure

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import android.widget.Toast
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.data.utils.rx.retryWithDelay
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.models.composition.content.CorruptedMediaFileException
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import com.github.anrimian.musicplayer.infrastructure.receivers.AppMediaButtonReceiver
import com.github.anrimian.musicplayer.infrastructure.receivers.BluetoothConnectionReceiver
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.ALBUM_ITEMS_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.ARTIST_ITEMS_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.COMPOSITIONS_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.DELIMITER
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.FOLDERS_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.GENRE_ITEMS_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.PAUSE_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.PLAYLIST_ITEMS_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.RECENT_MEDIA_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.RESUME_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.SEARCH_ITEMS_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.AppMediaBrowserService.Companion.SHUFFLE_ALL_AND_PLAY_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.music.CompositionSourceModelHelper
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAdditionalInfoForMediaBrowser
import com.github.anrimian.musicplayer.ui.main.MainActivity
import com.github.anrimian.musicplayer.ui.main.external_player.ExternalPlayerActivity
import com.github.anrimian.musicplayer.ui.utils.getParcelable
import com.github.anrimian.utils.pIntentFlag
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

class MediaSessionHandler(
    private val context: Context,
    private val playerInteractor: PlayerInteractor,
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    private val musicServiceInteractor: MusicServiceInteractor,
    private val ioScheduler: Scheduler,
    private val uiScheduler: Scheduler,
    private val errorParser: ErrorParser
) {

    private var mediaSession: MediaSessionCompat? = null
    private var activeServicesCount = 0

    private val mediaSessionDisposable = CompositeDisposable()
    private var actionDisposable: Disposable? = null

    private val playbackState = PlaybackState()
    private val metadataState = MetadataState()

    private var lastMetadataSource: CompositionSource? = null
    private var lastMetadataSettings: MusicNotificationSetting? = null
    private var currentArtLoadCancellable: Runnable? = null

    fun getMediaSession(): MediaSessionCompat {
        if (mediaSession == null) {
            mediaSession = MediaSessionCompat(context, MusicService::javaClass.name).apply {
                setCallback(AppMediaSessionCallback())

                val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null, context, AppMediaButtonReceiver::class.java)
                val pMediaButtonIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, pIntentFlag())
                setMediaButtonReceiver(pMediaButtonIntent)

                isActive = true
            }
            subscribeOnPlayQueue()
            subscribeOnMediaSessionMetadata()
            subscribeOnPlaybackStateActions()
        }
        return mediaSession!!
    }

    fun dispatchServiceCreated() {
        activeServicesCount++
    }

    fun dispatchServiceDestroyed() {
        activeServicesCount--
        if (activeServicesCount <= 0) {
            release()
        }
    }

    private fun release() {
        actionDisposable?.dispose()
        mediaSessionDisposable.clear()
        currentArtLoadCancellable?.run()
        currentArtLoadCancellable = null
        lastMetadataSource = null
        lastMetadataSettings = null
        mediaSession?.run {
//            isActive = false //removed after build 129. Observe and see how it works
            try {
                setSessionActivity(null)
                release()
            } catch (_: Exception) {
                // OEM bugs (e.g. Vivo) can throw SecurityException from ISession.destroy()
            }
        }
        mediaSession = null
    }

    private fun subscribeOnPlaybackStateActions() {
        mediaSessionDisposable.add(Observable.combineLatest(
            playerInteractor.getPlayerStateObservable(),
            libraryPlayerInteractor.getCurrentQueueItemObservable(),
            playerInteractor.getCurrentSourceObservable(),
            musicServiceInteractor.getTrackPositionChangeObservable(),
            musicServiceInteractor.getPlaybackSpeedObservable(),
            musicServiceInteractor.getRepeatModeObservable(),
            musicServiceInteractor.getRandomModeObservable(),
            playbackState::set
        ).flatMapSingle { state ->
            musicServiceInteractor.getTrackPosition()
                .map { trackPosition ->
                    state.trackPosition = trackPosition
                    return@map state
                }
        }.retryWithDelay(10, 10, TimeUnit.SECONDS)
            .observeOn(uiScheduler)
            .subscribe(this::onPlayBackStateReceived, errorParser::logError))
    }

    private fun onPlayBackStateReceived(playbackState: PlaybackState) {
        when(playbackState.currentSource) {
            is LibraryCompositionSource -> processLibraryPlaybackState((playbackState))
            is ExternalCompositionSource -> processExternalPlaybackState(playbackState)
            else -> {
                val playbackStateBuilder = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY
                                or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                                or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                    )
                getMediaSession().setPlaybackState(playbackStateBuilder.build())
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun processExternalPlaybackState(playbackState: PlaybackState) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_SEEK_TO
                        or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                        or PlaybackStateCompat.ACTION_FAST_FORWARD
                        or PlaybackStateCompat.ACTION_REWIND
                        or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                        or PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED
            )

        playbackStateBuilder.addCustomAction(
            REWIND_ACTION_ID,
            context.getString(R.string.rewind),
            R.drawable.ic_rewind
        )

        playbackStateBuilder.addCustomAction(
            FAST_FORWARD_ACTION_ID,
            context.getString(R.string.fast_forward),
            R.drawable.ic_fast_forward
        )

        playbackStateBuilder.addCustomAction(
            REPEAT_ACTION_ID,
            context.getString(FormatUtils.getRepeatModeText(playbackState.repeatMode)),
            FormatUtils.getRepeatModeIcon(playbackState.repeatMode)
        )

        playbackStateBuilder.addCustomAction(
            CLOSE_ACTION_ID,
            context.getString(R.string.close),
            R.drawable.ic_close
        )

        setMediaState(playbackStateBuilder, playbackState)

        playbackStateBuilder.setActiveQueueItemId(0L)

        getMediaSession().setPlaybackState(playbackStateBuilder.build())
        getMediaSession().setRepeatMode(getSessionRepeatMode(playbackState.repeatMode))
        getMediaSession().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)

        val activityIntent = Intent(context, ExternalPlayerActivity::class.java)
        activityIntent.putExtra(AppConstants.Arguments.LAUNCH_PREPARE_ARG, false)
        val pActivityIntent = PendingIntent.getActivity(context, 0, activityIntent, pIntentFlag())
        getMediaSession().setSessionActivity(pActivityIntent)
    }

    @SuppressLint("WrongConstant")
    private fun processLibraryPlaybackState(playbackState: PlaybackState) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        or PlaybackStateCompat.ACTION_SEEK_TO
                        or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                        or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                        or PlaybackStateCompat.ACTION_FAST_FORWARD
                        or PlaybackStateCompat.ACTION_REWIND
                        or PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
                        or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                        or PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED
            )

        playbackStateBuilder.addCustomAction(
            REPEAT_ACTION_ID,
            context.getString(FormatUtils.getRepeatModeText(playbackState.repeatMode)),
            FormatUtils.getRepeatModeIcon(playbackState.repeatMode)
        )
        playbackStateBuilder.addCustomAction(
            SHUFFLE_ACTION_ID,
            context.getString(R.string.content_description_shuffle),
            FormatUtils.getRandomModeIcon(playbackState.randomMode)
        )
        playbackStateBuilder.addCustomAction(
            REWIND_ACTION_ID,
            context.getString(R.string.rewind),
            R.drawable.ic_rewind
        )
        playbackStateBuilder.addCustomAction(
            FAST_FORWARD_ACTION_ID,
            context.getString(R.string.fast_forward),
            R.drawable.ic_fast_forward
        )

        //leave it for now
//      if (!isSourceEqual) {
//          newTrackPosition = CompositionSourceModelHelper.getTrackPosition(newCompositionSource);
//      }
        setMediaState(playbackStateBuilder, playbackState)

        val playQueueCurrentItemId = playbackState.playQueueCurrentItem.playQueueItem?.itemId ?: 0L
        playbackStateBuilder.setActiveQueueItemId(playQueueCurrentItemId)

        getMediaSession().setPlaybackState(playbackStateBuilder.build())
        getMediaSession().setRepeatMode(getSessionRepeatMode(playbackState.repeatMode))

        val sessionShuffleMode = if (playbackState.randomMode) {
            PlaybackStateCompat.SHUFFLE_MODE_ALL
        } else {
            PlaybackStateCompat.SHUFFLE_MODE_NONE
        }
        getMediaSession().setShuffleMode(sessionShuffleMode)

        val activityIntent = Intent(context, MainActivity::class.java)
        val pActivityIntent = PendingIntent.getActivity(context, 0, activityIntent, pIntentFlag())
        getMediaSession().setSessionActivity(pActivityIntent)
    }

    private fun getSessionRepeatMode(repeatMode: Int) = when (repeatMode) {
        RepeatMode.REPEAT_COMPOSITION -> PlaybackStateCompat.REPEAT_MODE_ONE
        RepeatMode.REPEAT_PLAY_QUEUE -> PlaybackStateCompat.REPEAT_MODE_ALL
        else -> PlaybackStateCompat.REPEAT_MODE_NONE
    }

    private fun subscribeOnMediaSessionMetadata() {
        mediaSessionDisposable.add(Observable.combineLatest(
            playerInteractor.getCurrentSourceObservable(),
            musicServiceInteractor.getNotificationSettingObservable(),
            libraryPlayerInteractor.getCurrentItemPositionObservable().toObservable()
                .map { pos -> pos + 1 } // AVRCP value is 1-based. Map for this
                .startWithItem(0),
            libraryPlayerInteractor.getPlayQueueSizeObservable()
                .startWithItem(0),
            metadataState::set
        ).retryWithDelay(10, 10, TimeUnit.SECONDS)
            .observeOn(uiScheduler)
            .subscribe(this::onMetadataStateReceived, errorParser::logError))
    }

    private fun onMetadataStateReceived(state: MetadataState) {
        val metadataBuilder = MediaMetadataCompat.Builder()
        val currentSource = state.currentSource.value

        val trackNumber: Long
        val totalTracks: Long
        if (currentSource is LibraryCompositionSource) {
            trackNumber = state.trackNumber
            totalTracks = state.totalTracks
        } else {
            trackNumber = 0L
            totalTracks = 0L
        }

        CompositionSourceModelHelper.updateMediaSessionMetadata(
            currentSource,
            metadataBuilder,
            getMediaSession(),
            context,
            trackNumber,
            totalTracks
        )


        val sourceChanged = !sameSource(lastMetadataSource, currentSource)
        val settingsChanged = lastMetadataSettings != state.settings
        if (!sourceChanged && !settingsChanged) {
            return
        }

        currentArtLoadCancellable?.run()
        currentArtLoadCancellable = null
        lastMetadataSource = currentSource
        lastMetadataSettings = state.settings

        currentArtLoadCancellable = CompositionSourceModelHelper.updateMediaSessionAlbumArt(
            currentSource,
            metadataBuilder,
            getMediaSession(),
            state.settings
        )
    }

    private fun sameSource(a: CompositionSource?, b: CompositionSource?): Boolean {
        if (a == null && b == null) {
            return true
        }
        if (a == null || b == null) {
            return false
        }
        return CompositionSourceModelHelper.areSourcesTheSame(a, b)
    }

    private fun subscribeOnPlayQueue() {
        mediaSessionDisposable.add(Observable.combineLatest(
            libraryPlayerInteractor.getPlayQueueObservable().toObservable(),
            playerInteractor.getCurrentSourceObservable()
                .map { source -> source.value is LibraryCompositionSource }
                .distinctUntilChanged(),
            ::toSessionQueueItems
        ).retryWithDelay(10, 10, TimeUnit.SECONDS)
            .subscribeOn(ioScheduler)
            .subscribe(this::onPlayQueueReceived, errorParser::logError))
    }

    private fun onPlayQueueReceived(playQueue: List<MediaSessionCompat.QueueItem>) {
        mediaSession?.setQueue(playQueue)
    }

    private fun toSessionQueueItems(
        playQueue: List<PlayQueueItem>,
        isLibrarySource: Boolean
    ): List<MediaSessionCompat.QueueItem> {
        return if (isLibrarySource) playQueue.map(this::toSessionQueueItem) else emptyList()
    }

    private fun toSessionQueueItem(item: PlayQueueItem): MediaSessionCompat.QueueItem {
        val mediaDescription = MediaDescriptionCompat.Builder()
            .setTitle(CompositionHelper.formatCompositionName(item))
            .setSubtitle(formatCompositionAdditionalInfoForMediaBrowser(context, item))
            .build()
        return MediaSessionCompat.QueueItem(mediaDescription, item.itemId)
    }

    private fun setMediaState(
        playbackStateBuilder: PlaybackStateCompat.Builder,
        playbackState: PlaybackState
    ) {
        val playerState = when (val playerState = playbackState.playerState) {
            PlayerState.IDLE -> PlaybackStateCompat.STATE_NONE
            PlayerState.PREPARING,
            PlayerState.LOADING -> PlaybackStateCompat.STATE_CONNECTING
            PlayerState.PAUSE -> PlaybackStateCompat.STATE_PAUSED
            PlayerState.PLAY -> PlaybackStateCompat.STATE_PLAYING
            PlayerState.STOP -> PlaybackStateCompat.STATE_STOPPED
            is PlayerState.Error -> {
                val errorCode = when(playerState.throwable) {
                    is UnsupportedSourceException,
                    is CorruptedMediaFileException -> PlaybackStateCompat.ERROR_CODE_NOT_SUPPORTED
                    else -> PlaybackStateCompat.ERROR_CODE_APP_ERROR
                }
                val errorMessage = errorParser.parseError(playerState.throwable).message
                playbackStateBuilder.setErrorMessage(errorCode, errorMessage)
                PlaybackStateCompat.STATE_PAUSED
            }
        }
        // 0f is required by the PlaybackStateCompat contract when the state is not STATE_PLAYING
        val playbackSpeed = if (playerState == PlaybackStateCompat.STATE_PLAYING) {
            playbackState.playbackSpeed
        } else {
            0f
        }
        playbackStateBuilder.setState(
            playerState,
            playbackState.trackPosition,
            playbackSpeed
        )
    }

    private class MetadataState {
        lateinit var currentSource: Opt<CompositionSource>
        lateinit var settings: MusicNotificationSetting
        var trackNumber: Long = 0
        var totalTracks: Long = 0

        fun set(
            currentSource: Opt<CompositionSource>,
            settings: MusicNotificationSetting,
            trackNumber: Int,
            totalTracks: Int
        ): MetadataState {
            this.currentSource = currentSource
            this.settings = settings
            this.trackNumber = trackNumber.toLong()
            this.totalTracks = totalTracks.toLong()
            return this
        }
    }

    private class PlaybackState {
        lateinit var playerState: PlayerState
        lateinit var playQueueCurrentItem: PlayQueueEvent
        var currentSource: CompositionSource? = null
        var trackPosition: Long = 0
        var playbackSpeed: Float = 0.0f
        var repeatMode: Int = 0
        var randomMode: Boolean = false

        fun set(
            playerState: PlayerState,
            playQueueCurrentItem: PlayQueueEvent,
            currentSource: Opt<CompositionSource>,
            trackPosition: Long,
            playbackSpeed: Float,
            repeatMode: Int,
            randomMode: Boolean
        ): PlaybackState {
            this.playerState = playerState
            this.playQueueCurrentItem = playQueueCurrentItem
            this.currentSource = currentSource.value
            this.trackPosition = trackPosition
            this.playbackSpeed = playbackSpeed
            this.repeatMode = repeatMode
            this.randomMode = randomMode
            return this
        }
    }

    private inner class AppMediaSessionCallback : MediaSessionCompat.Callback() {

        private var lastEventTime = 0L

        override fun onPlay() {
            SystemServiceControllerImpl.startPlayForegroundService(context)
        }

        override fun onPause() {
            playerInteractor.pause()
        }

        override fun onStop() {
            playerInteractor.stop()
        }

        override fun onSkipToNext() {
            musicServiceInteractor.skipToNext()
        }

        override fun onSkipToPrevious() {
            musicServiceInteractor.skipToPrevious()
        }

        override fun onSeekTo(pos: Long) {
            playerInteractor.onSeekFinished(pos)
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            val appRepeatMode = when (repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_INVALID,
                PlaybackStateCompat.REPEAT_MODE_NONE -> {
                    RepeatMode.NONE
                }
                PlaybackStateCompat.REPEAT_MODE_GROUP,
                PlaybackStateCompat.REPEAT_MODE_ALL -> {
                    RepeatMode.REPEAT_PLAY_QUEUE
                }
                PlaybackStateCompat.REPEAT_MODE_ONE -> {
                    RepeatMode.REPEAT_COMPOSITION
                }
                else -> RepeatMode.NONE
            }
            musicServiceInteractor.setRepeatMode(appRepeatMode)
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            musicServiceInteractor.setRandomPlayingEnabled(shuffleMode != PlaybackStateCompat.SHUFFLE_MODE_NONE)
        }

        override fun onFastForward() {
            playerInteractor.fastSeekForward().subscribe()
        }

        override fun onRewind() {
            playerInteractor.fastSeekBackward().subscribe()
        }

        override fun onSetPlaybackSpeed(speed: Float) {
            musicServiceInteractor.setPlaybackSpeed(speed)
        }

        override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
            val parts = mediaId.split(DELIMITER)
            when(val action = parts.first()) {
                RESUME_ACTION_ID,
                RECENT_MEDIA_ACTION_ID -> SystemServiceControllerImpl.startPlayForegroundService(context)
                PAUSE_ACTION_ID -> libraryPlayerInteractor.pause()
                SHUFFLE_ALL_AND_PLAY_ACTION_ID -> {
                    actionDisposable = musicServiceInteractor.shuffleAllAndPlay()
                        .observeOn(uiScheduler)
                        .subscribe({}, this::processError)
                }
                COMPOSITIONS_ACTION_ID -> {
                    val position = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    actionDisposable = musicServiceInteractor.startPlayingFromCompositions(position)
                        .observeOn(uiScheduler)
                        .subscribe({}, this::processError)
                }
                SEARCH_ITEMS_ACTION_ID -> {
                    val position = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    val searchQuery = if (parts.size >= 3) {
                        val prefixToStrip = action + DELIMITER + parts[1] + DELIMITER
                        val extracted = mediaId.substringAfter(prefixToStrip)
                        extracted.ifEmpty { null }
                    } else {
                        null
                    }
                    actionDisposable = musicServiceInteractor.playFromSearch(searchQuery, position)
                        .observeOn(uiScheduler)
                        .subscribe({}, this::processError)
                }
                FOLDERS_ACTION_ID -> {
                    val rawFolderId = parts.getOrNull(1)?.toLongOrNull() ?: 0L
                    val folderId = if (rawFolderId == 0L) null else rawFolderId
                    val compositionId = parts.getOrNull(2)?.toLongOrNull() ?: 0L
                    actionDisposable = musicServiceInteractor.play(folderId, compositionId)
                        .observeOn(uiScheduler)
                        .subscribe({}, this::processError)
                }
                ARTIST_ITEMS_ACTION_ID -> {
                    val artistId = parts.getOrNull(1)?.toLongOrNull() ?: 0L
                    val position = parts.getOrNull(2)?.toIntOrNull() ?: 0
                    actionDisposable = musicServiceInteractor.startPlayingFromArtistCompositions(artistId, position)
                        .observeOn(uiScheduler)
                        .subscribe({}, this::processError)
                }
                ALBUM_ITEMS_ACTION_ID -> {
                    val albumId = parts.getOrNull(1)?.toLongOrNull() ?: 0L
                    val position = parts.getOrNull(2)?.toIntOrNull() ?: 0
                    actionDisposable = musicServiceInteractor.startPlayingFromAlbumCompositions(albumId, position)
                        .observeOn(uiScheduler)
                        .subscribe({}, this::processError)
                }
                GENRE_ITEMS_ACTION_ID -> {
                    val genreId = parts.getOrNull(1)?.toLongOrNull() ?: 0L
                    val position = parts.getOrNull(2)?.toIntOrNull() ?: 0
                    actionDisposable = musicServiceInteractor.startPlayingFromGenreCompositions(genreId, position)
                        .observeOn(uiScheduler)
                        .subscribe({}, this::processError)
                }
                PLAYLIST_ITEMS_ACTION_ID -> {
                    val playlistId = parts.getOrNull(1)?.toLongOrNull() ?: 0L
                    val position = parts.getOrNull(2)?.toIntOrNull() ?: 0
                    actionDisposable = musicServiceInteractor.startPlayingFromPlaylistItems(playlistId, position)
                        .observeOn(uiScheduler)
                        .subscribe({}, this::processError)
                }
            }
        }

        override fun onSkipToQueueItem(id: Long) {
            libraryPlayerInteractor.skipToItem(id)
        }

        override fun onCustomAction(action: String, extras: Bundle) {
            when(action) {
                REPEAT_ACTION_ID -> musicServiceInteractor.changeRepeatMode()
                SHUFFLE_ACTION_ID -> libraryPlayerInteractor.changeRandomMode()
                REWIND_ACTION_ID -> musicServiceInteractor.fastSeekBackward()
                FAST_FORWARD_ACTION_ID -> musicServiceInteractor.fastSeekForward()
                CLOSE_ACTION_ID -> musicServiceInteractor.reset()
            }
        }

        override fun onPlayFromSearch(query: String, extras: Bundle) {
            val formattedQuery = query.ifEmpty { null }
            actionDisposable = musicServiceInteractor.playFromSearch(formattedQuery)
                .observeOn(uiScheduler)
                .subscribe({}, this::processError)
        }

        override fun onPrepare() {
            musicServiceInteractor.prepare()
        }

        //next - not implemented
        override fun onCommand(command: String, extras: Bundle, cb: ResultReceiver) {
            super.onCommand(command, extras, cb)
        }

        override fun onPrepareFromMediaId(mediaId: String, extras: Bundle) {
            super.onPrepareFromMediaId(mediaId, extras)
        }

        override fun onPrepareFromSearch(query: String, extras: Bundle) {
            super.onPrepareFromSearch(query, extras)
        }

        override fun onPrepareFromUri(uri: Uri, extras: Bundle) {
            super.onPrepareFromUri(uri, extras)
        }

        override fun onPlayFromUri(uri: Uri, extras: Bundle) {
            super.onPlayFromUri(uri, extras)
        }

        override fun onSetRating(rating: RatingCompat) {
            super.onSetRating(rating)
        }

        override fun onSetRating(rating: RatingCompat, extras: Bundle) {
            super.onSetRating(rating, extras)
        }

        override fun onSetCaptioningEnabled(enabled: Boolean) {
            super.onSetCaptioningEnabled(enabled)
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat) {
            super.onAddQueueItem(description)
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat, index: Int) {
            super.onAddQueueItem(description, index)
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat) {
            super.onRemoveQueueItem(description)
        }

        //must call super
        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            //fix for case when double or triple tap is considered as skipTo + play/pause
            val keyEvent = mediaButtonEvent.getParcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)

            if (keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (BluetoothConnectionReceiver.shouldIgnorePlayEvent()) {
                    return true
                }
            }

            if (keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (lastEventTime + PLAY_EVENT_LOCK_WINDOW_MILLIS > System.currentTimeMillis()) {
                    return true
                }
            }
            lastEventTime = System.currentTimeMillis()
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        private fun processError(throwable: Throwable) {
            errorParser.logError(throwable)
            val errorCommand = errorParser.parseError(throwable)
            Toast.makeText(context, errorCommand.message, Toast.LENGTH_LONG).show()
        }
    }

    private companion object {
        const val REPEAT_ACTION_ID = "repeat_action_id"
        const val SHUFFLE_ACTION_ID = "shuffle_action_id"
        const val FAST_FORWARD_ACTION_ID = "fast_forward_action_id"
        const val REWIND_ACTION_ID = "rewind_action_id"
        const val CLOSE_ACTION_ID = "close_action_id"

        const val PLAY_EVENT_LOCK_WINDOW_MILLIS = 15L
    }

}