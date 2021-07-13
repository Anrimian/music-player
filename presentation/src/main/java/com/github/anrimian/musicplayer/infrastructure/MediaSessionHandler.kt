package com.github.anrimian.musicplayer.infrastructure

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.infrastructure.receivers.AppMediaButtonReceiver
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.*
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAdditionalInfoForMediaBrowser
import com.github.anrimian.musicplayer.ui.main.MainActivity
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

private const val REPEAT_ACTION_ID = "repeat_action_id"
private const val SHUFFLE_ACTION_ID = "shuffle_action_id"
private const val FAST_FORWARD_ACTION_ID = "fast_forward_action_id"
private const val REWIND_ACTION_ID = "rewind_action_id"

class MediaSessionHandler(private val context: Context,
                          private val playerInteractor: PlayerInteractor,
                          private val libraryPlayerInteractor: LibraryPlayerInteractor,
                          private val musicServiceInteractor: MusicServiceInteractor,
                          private val errorParser: ErrorParser
) {

    private var mediaSession: MediaSessionCompat? = null
    private var activeServicesCount = 0

    private val mediaSessionDisposable = CompositeDisposable()
    private var actionDisposable: Disposable? = null

    private val playbackState = PlaybackState()

    fun getMediaSession(): MediaSessionCompat {
        if (mediaSession == null) {
            mediaSession = MediaSessionCompat(context, MusicService::javaClass.name).apply {
                setCallback(AppMediaSessionCallback())

                val activityIntent = Intent(context, MainActivity::class.java)
                val pActivityIntent = PendingIntent.getActivity(context, 0, activityIntent, 0)
                setSessionActivity(pActivityIntent)

                val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null, context, AppMediaButtonReceiver::class.java)
                val pMediaButtonIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, 0)
                setMediaButtonReceiver(pMediaButtonIntent)
            }
            subscribeOnPlayQueue()
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
            isActive = false
            release()
        }
        mediaSession = null
    }

    private fun subscribeOnPlaybackStateActions() {
        mediaSessionDisposable.add(Observable.combineLatest(
            libraryPlayerInteractor.playerStateObservable,
            libraryPlayerInteractor.currentQueueItemObservable,
            playerInteractor.trackPositionObservable,
            playerInteractor.currentPlaybackSpeedObservable,
            libraryPlayerInteractor.repeatModeObservable,
            musicServiceInteractor.randomModeObservable,//controversial
            playbackState::set
        ).subscribe(this::onPlayBackStateReceived))
    }

    private fun onPlayBackStateReceived(playbackState: PlaybackState) {
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
            )


        //move media metadata subscriptions to here - it needed(just launch auto without app to check)
        //correct action handling, ignore external player

        playbackStateBuilder.addCustomAction(
            REPEAT_ACTION_ID,
            context.getString(FormatUtils.getRepeatModeText(playbackState.repeatMode)),
            FormatUtils.getRepeatModeIcon(playbackState.repeatMode)
        )

        //icon, description
        playbackStateBuilder.addCustomAction(
            SHUFFLE_ACTION_ID,
            "8",
            R.drawable.ic_shuffle
        )

        //icon, description
        playbackStateBuilder.addCustomAction(REWIND_ACTION_ID, "2", R.drawable.ic_skip_previous)
        //icon, description
        playbackStateBuilder.addCustomAction(FAST_FORWARD_ACTION_ID, "1", R.drawable.ic_skip_next)

        //leave it for now
//      if (!isSourceEqual) {
//          newTrackPosition = CompositionSourceModelHelper.getTrackPosition(newCompositionSource);
//      }
        playbackStateBuilder.setState(
            toMediaState(playbackState.playerState),
            playbackState.trackPosition,
            playbackState.playbackSpeed)

        val playQueueCurrentItemId = playbackState.playQueueCurrentItem.playQueueItem?.id ?: 0L
        playbackStateBuilder.setActiveQueueItemId(playQueueCurrentItemId)

        getMediaSession().setPlaybackState(playbackStateBuilder.build())

        val sessionRepeatMode = when (playbackState.repeatMode) {
            RepeatMode.REPEAT_COMPOSITION -> PlaybackStateCompat.REPEAT_MODE_ONE
            RepeatMode.REPEAT_PLAY_LIST -> PlaybackStateCompat.REPEAT_MODE_ALL
            else -> PlaybackStateCompat.REPEAT_MODE_NONE
        }
        getMediaSession().setRepeatMode(sessionRepeatMode)

        val sessionShuffleMode = if (playbackState.randomMode) {
            PlaybackStateCompat.SHUFFLE_MODE_ALL
        } else {
            PlaybackStateCompat.SHUFFLE_MODE_NONE
        }
        getMediaSession().setShuffleMode(sessionShuffleMode)
    }

    private fun subscribeOnPlayQueue() {
        mediaSessionDisposable.add(libraryPlayerInteractor.playQueueObservable
            .subscribe(this::onPlayQueueReceived))
    }

    private fun onPlayQueueReceived(queue: List<PlayQueueItem>) {
        getMediaSession().setQueue(queue.map(this::toSessionQueueItem))
    }

    private fun toSessionQueueItem(item: PlayQueueItem): MediaSessionCompat.QueueItem {
        val composition = item.composition
        val mediaDescription = MediaDescriptionCompat.Builder()
            .setTitle(CompositionHelper.formatCompositionName(composition))
            .setSubtitle(formatCompositionAdditionalInfoForMediaBrowser(context, composition))
            .build()
        return MediaSessionCompat.QueueItem(mediaDescription, item.id)
    }

    private fun toMediaState(playerState: PlayerState): Int {
        return when (playerState) {
            PlayerState.IDLE -> PlaybackStateCompat.STATE_NONE
            PlayerState.LOADING -> PlaybackStateCompat.STATE_CONNECTING
            PlayerState.PAUSE -> PlaybackStateCompat.STATE_PAUSED
            PlayerState.PLAY -> PlaybackStateCompat.STATE_PLAYING
            PlayerState.STOP -> PlaybackStateCompat.STATE_STOPPED
            else -> throw IllegalStateException("unexpected player state: $playerState")
        }
    }

    private class PlaybackState {
        lateinit var playerState: PlayerState
        lateinit var playQueueCurrentItem: PlayQueueEvent
        var trackPosition: Long = 0
        var playbackSpeed: Float = 0.0f
        var repeatMode: Int = 0
        var randomMode: Boolean = false

        fun set(
            playerState: PlayerState,
            playQueueCurrentItem: PlayQueueEvent,
            trackPosition: Long,
            playbackSpeed: Float,
            repeatMode: Int,
            randomMode: Boolean
        ): PlaybackState {
            this.playerState = playerState
            this.playQueueCurrentItem = playQueueCurrentItem
            this.trackPosition = trackPosition
            this.playbackSpeed = playbackSpeed
            this.repeatMode = repeatMode
            this.randomMode = randomMode
            return this
        }
    }

    private inner class AppMediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            playerInteractor.play()
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

        //next - test it
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
                RESUME_ACTION_ID -> {
                    playerInteractor.play()
                }
                SHUFFLE_ALL_AND_PLAY_ACTION_ID -> {
                    actionDisposable = musicServiceInteractor.shuffleAllAndPlay()
                        .subscribe({}, this::processError)
                }
                COMPOSITIONS_ACTION_ID -> {
                    val position = extras.getInt(POSITION_ARG)
                    actionDisposable = musicServiceInteractor.startPlayingFromCompositions(position)
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
                REPEAT_ACTION_ID -> libraryPlayerInteractor.changeRepeatMode()
            }
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

        override fun onPlayFromSearch(query: String, extras: Bundle) {
            super.onPlayFromSearch(query, extras)
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