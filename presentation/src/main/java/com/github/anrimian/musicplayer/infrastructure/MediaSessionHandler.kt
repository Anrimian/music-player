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
import com.github.anrimian.musicplayer.ui.main.MainActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

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

    private val playbackStateBuilder = PlaybackStateCompat.Builder()
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
            subscribeOnCurrentQueueItem()
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

    fun updatePlaybackState(playerState: PlayerState, trackPosition: Long, playbackSpeed: Float) {
        playbackStateBuilder.setState(toMediaState(playerState), trackPosition, playbackSpeed)
        getMediaSession().setPlaybackState(playbackStateBuilder.build())
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

    private fun subscribeOnPlayQueue() {
        mediaSessionDisposable.add(libraryPlayerInteractor.playQueueObservable
            .subscribe(this::onPlayQueueReceived))
    }

    private fun subscribeOnCurrentQueueItem() {
        mediaSessionDisposable.add(libraryPlayerInteractor.currentQueueItemObservable
            .subscribe(this::onPlayQueueEventReceived))
    }

    private fun onPlayQueueEventReceived(event: PlayQueueEvent) {
        val id = event.playQueueItem?.id ?: 0L
        playbackStateBuilder.setActiveQueueItemId(id)
        getMediaSession().setPlaybackState(playbackStateBuilder.build())
    }

    private fun onPlayQueueReceived(queue: List<PlayQueueItem>) {
        getMediaSession().setQueue(queue.map(this::toSessionQueueItem))
    }

    private fun toSessionQueueItem(item: PlayQueueItem): MediaSessionCompat.QueueItem {
        val composition = item.composition
        val mediaDescription = MediaDescriptionCompat.Builder()
            .setTitle(CompositionHelper.formatCompositionName(composition))
            .setSubtitle(FormatUtils.formatAuthor(composition.artist, context))
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

        override fun onCustomAction(action: String, extras: Bundle) {
            super.onCustomAction(action, extras)
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