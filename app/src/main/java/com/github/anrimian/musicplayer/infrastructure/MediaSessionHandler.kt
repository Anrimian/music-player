package com.github.anrimian.musicplayer.infrastructure

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
import android.widget.Toast
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.data.utils.rx.retryWithDelay
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import com.github.anrimian.musicplayer.infrastructure.receivers.AppMediaButtonReceiver
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.*
import com.github.anrimian.musicplayer.infrastructure.service.music.CompositionSourceModelHelper
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService
import com.github.anrimian.musicplayer.ui.common.AppAndroidUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAdditionalInfoForMediaBrowser
import com.github.anrimian.musicplayer.ui.main.MainActivity
import com.github.anrimian.musicplayer.ui.utils.pIntentFlag
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

private const val REPEAT_ACTION_ID = "repeat_action_id"
private const val SHUFFLE_ACTION_ID = "shuffle_action_id"
private const val FAST_FORWARD_ACTION_ID = "fast_forward_action_id"
private const val REWIND_ACTION_ID = "rewind_action_id"

class MediaSessionHandler(private val context: Context,
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

    fun getMediaSession(): MediaSessionCompat {
        if (mediaSession == null) {
            mediaSession = MediaSessionCompat(context, MusicService::javaClass.name).apply {
                setCallback(AppMediaSessionCallback())

                val activityIntent = Intent(context, MainActivity::class.java)
                val pActivityIntent = PendingIntent.getActivity(context, 0, activityIntent, pIntentFlag())
                setSessionActivity(pActivityIntent)

                val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null, context, AppMediaButtonReceiver::class.java)
                val pMediaButtonIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, pIntentFlag())
                setMediaButtonReceiver(pMediaButtonIntent)
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
        mediaSession?.run {
//            isActive = false //removed after build 129. Observe and see how it works
            release()
        }
        mediaSession = null
    }

    private fun subscribeOnPlaybackStateActions() {
        mediaSessionDisposable.add(Observable.combineLatest(
            playerInteractor.getPlayerStateObservable(),
            libraryPlayerInteractor.getCurrentQueueItemObservable(),
            playerInteractor.getCurrentSourceObservable(),
            playerInteractor.getTrackPositionObservable(),
            playerInteractor.getCurrentPlaybackSpeedObservable(),
            musicServiceInteractor.repeatModeObservable,
            musicServiceInteractor.randomModeObservable,
            playbackState::set
        ).observeOn(uiScheduler)
            .subscribe(this::onPlayBackStateReceived))
    }

    private fun onPlayBackStateReceived(playbackState: PlaybackState) {
        when(playbackState.currentSource) {
            is ExternalCompositionSource -> processExternalPlaybackState(playbackState)
            else -> processLibraryPlaybackState((playbackState))
        }
    }

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

        setMediaState(playbackStateBuilder, playbackState)

        playbackStateBuilder.setActiveQueueItemId(0L)

        getMediaSession().setPlaybackState(playbackStateBuilder.build())
        getMediaSession().setRepeatMode(getSessionRepeatMode(playbackState.repeatMode))
        getMediaSession().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
    }

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

        val playQueueCurrentItemId = playbackState.playQueueCurrentItem.playQueueItem?.id ?: 0L
        playbackStateBuilder.setActiveQueueItemId(playQueueCurrentItemId)

        getMediaSession().setPlaybackState(playbackStateBuilder.build())
        getMediaSession().setRepeatMode(getSessionRepeatMode(playbackState.repeatMode))

        val sessionShuffleMode = if (playbackState.randomMode) {
            PlaybackStateCompat.SHUFFLE_MODE_ALL
        } else {
            PlaybackStateCompat.SHUFFLE_MODE_NONE
        }
        getMediaSession().setShuffleMode(sessionShuffleMode)
    }

    private fun getSessionRepeatMode(repeatMode: Int) = when (repeatMode) {
        RepeatMode.REPEAT_COMPOSITION -> PlaybackStateCompat.REPEAT_MODE_ONE
        RepeatMode.REPEAT_PLAY_LIST -> PlaybackStateCompat.REPEAT_MODE_ALL
        else -> PlaybackStateCompat.REPEAT_MODE_NONE
    }

    private fun subscribeOnMediaSessionMetadata() {
        mediaSessionDisposable.add(Observable.combineLatest(
            playerInteractor.getCurrentSourceObservable(),
            musicServiceInteractor.notificationSettingObservable,
            metadataState::set
        ).observeOn(uiScheduler)
            .subscribe(this::onMetadataStateReceived))
    }

    private fun onMetadataStateReceived(state: MetadataState) {
        val metadataBuilder = MediaMetadataCompat.Builder()
        val currentSource = state.currentSource.value

        CompositionSourceModelHelper.updateMediaSessionMetadata(
            currentSource,
            metadataBuilder,
            getMediaSession(),
            context
        )

        //we can use uri
        CompositionSourceModelHelper.updateMediaSessionAlbumArt(
            currentSource,
            metadataBuilder,
            getMediaSession(),
            state.settings.isCoversOnLockScreen
        )
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
        val composition = item.composition
        val mediaDescription = MediaDescriptionCompat.Builder()
            .setTitle(CompositionHelper.formatCompositionName(composition))
            .setSubtitle(formatCompositionAdditionalInfoForMediaBrowser(context, composition))
            .build()
        return MediaSessionCompat.QueueItem(mediaDescription, item.id)
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
                    is UnsupportedSourceException -> PlaybackStateCompat.ERROR_CODE_NOT_SUPPORTED
                    else -> PlaybackStateCompat.ERROR_CODE_APP_ERROR
                }
                val errorMessage = errorParser.parseError(playerState.throwable).message
                playbackStateBuilder.setErrorMessage(errorCode, errorMessage)
                PlaybackStateCompat.STATE_PAUSED
            }
        }
        playbackStateBuilder.setState(
            playerState,
            playbackState.trackPosition,
            playbackState.playbackSpeed)
    }

    private class MetadataState {
        lateinit var currentSource: Optional<CompositionSource>
        lateinit var settings: MusicNotificationSetting

        fun set(
            currentSource: Optional<CompositionSource>,
            settings: MusicNotificationSetting
        ): MetadataState {
            this.currentSource = currentSource
            this.settings = settings
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
            currentSource: Optional<CompositionSource>,
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

        override fun onPlay() {
            AppAndroidUtils.playPause(context, playerInteractor)
        }

        override fun onPause() {
            AppAndroidUtils.playPause(context, playerInteractor)
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
                    RepeatMode.REPEAT_PLAY_LIST
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
            playerInteractor.fastSeekForward()
        }

        override fun onRewind() {
            playerInteractor.fastSeekBackward()
        }

        override fun onSetPlaybackSpeed(speed: Float) {
            musicServiceInteractor.setPlaybackSpeed(speed)
        }

        override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
            when(mediaId) {
                RESUME_ACTION_ID -> libraryPlayerInteractor.play()
                PAUSE_ACTION_ID -> libraryPlayerInteractor.pause()
                SHUFFLE_ALL_AND_PLAY_ACTION_ID -> {
                    actionDisposable = musicServiceInteractor.shuffleAllAndPlay()
                        .subscribe({}, this::processError)
                }
                COMPOSITIONS_ACTION_ID -> {
                    val position = extras.getInt(POSITION_ARG)
                    actionDisposable = musicServiceInteractor.startPlayingFromCompositions(position)
                        .subscribe({}, this::processError)
                }
                SEARCH_ITEMS_ACTION_ID -> {
                    val position = extras.getInt(POSITION_ARG)
                    val searchQuery = extras.getString(SEARCH_QUERY_ARG)
                    actionDisposable = musicServiceInteractor.playFromSearch(searchQuery, position)
                        .subscribe({}, this::processError)
                }
                FOLDERS_ACTION_ID -> {
                    var folderId: Long? = extras.getLong(FOLDER_ID_ARG)
                    if (folderId == 0L) {
                        folderId = null
                    }
                    val compositionId = extras.getLong(COMPOSITION_ID_ARG)
                    musicServiceInteractor.play(folderId, compositionId)
                }
                ARTIST_ITEMS_ACTION_ID -> {
                    val artistId = extras.getLong(ARTIST_ID_ARG)
                    val position = extras.getInt(POSITION_ARG)
                    actionDisposable = musicServiceInteractor.startPlayingFromArtistCompositions(artistId, position)
                        .subscribe({}, this::processError)
                }
                ALBUM_ITEMS_ACTION_ID -> {
                    val albumId = extras.getLong(ALBUM_ID_ARG)
                    val position = extras.getInt(POSITION_ARG)
                    actionDisposable = musicServiceInteractor.startPlayingFromAlbumCompositions(albumId, position)
                        .subscribe({}, this::processError)
                }
                PLAYLIST_ITEMS_ACTION_ID -> {
                    val playlistId = extras.getLong(PLAYLIST_ID_ARG)
                    val position = extras.getInt(POSITION_ARG)
                    actionDisposable = musicServiceInteractor.startPlayingFromPlaylistItems(playlistId, position)
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
            }
        }

        override fun onPlayFromSearch(query: String, extras: Bundle) {
            val formattedQuery = query.ifEmpty { null }
            actionDisposable = musicServiceInteractor.playFromSearch(formattedQuery)
                .subscribe({}, this::processError)
        }

        //next - not implemented
        override fun onCommand(command: String, extras: Bundle, cb: ResultReceiver) {
            super.onCommand(command, extras, cb)
        }

        override fun onPrepare() {
            super.onPrepare()
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
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        private fun processError(throwable: Throwable) {
            errorParser.logError(throwable)
            val errorCommand = errorParser.parseError(throwable)
            Toast.makeText(context, errorCommand.message, Toast.LENGTH_LONG).show()
        }
    }

}