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
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.infrastructure.receivers.AppMediaButtonReceiver
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.RESUME_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.media_browser.SHUFFLE_ALL_AND_PLAY_ACTION_ID
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.main.MainActivity

class MediaSessionHandler(private val context: Context,
                          private val playerInteractor: PlayerInteractor,
                          private val musicServiceInteractor: MusicServiceInteractor,
                          private val errorParser: ErrorParser
) {

    private var mediaSession: MediaSessionCompat? = null
    private var activeServicesCount = 0

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
        getMediaSession().isActive = false
        getMediaSession().release()
        mediaSession = null
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
                    //handle permission
                    musicServiceInteractor.shuffleAllAndPlay()
                        .subscribe({}, this::processError)
                }
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

        override fun onSkipToQueueItem(id: Long) {
            super.onSkipToQueueItem(id)
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