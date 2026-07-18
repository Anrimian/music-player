package com.github.anrimian.musicplayer.infrastructure.receivers

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.Permissions
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.AppAndroidUtils
import com.github.anrimian.musicplayer.ui.utils.getParcelable

class AppMediaButtonReceiver: MediaButtonReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null
            || intent.action != Intent.ACTION_MEDIA_BUTTON
            || !intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
            handleUnsupportedAction(context)
            return
        }
        val keyEvent = intent.getParcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        if (keyEvent != null) {
            handleExternalAction(context, keyEvent, intent)
            return
        }
        // process to media browser service
        super.onReceive(context, intent)
    }

    /**
     * consider unsupported action as play-pause command
     */
    //add option in settings to disable it?
    private fun handleUnsupportedAction(context: Context) {
        Components.checkInitialization(context)
        val appComponent = Components.getAppComponent()
        if (!appComponent.settingsRepository().isProcessUnsupportedBluetoothEventEnabled) {
            return
        }

        val currentTime = System.currentTimeMillis()
        if (lastUnsupportedEventTime + UNSUPPORTED_EVENT_PROCESS_WINDOW_MILLIS > currentTime) {
            return
        }
        lastUnsupportedEventTime = currentTime

        if (!Permissions.hasFilePermission(context)) {
            appComponent.notificationsDisplayer().showErrorNotification(R.string.no_file_permission)
            return
        }
        AppAndroidUtils.playPause(context, appComponent.playerInteractor())
    }

    private fun handleExternalAction(
        context: Context,
        keyEvent: KeyEvent,
        intent: Intent,
    ) {
        val appComponent = Components.getAppComponent()
        if (!Permissions.hasFilePermission(context)) {
            appComponent.notificationsDisplayer().showErrorNotification(R.string.no_file_permission)
            return
        }
        if (BluetoothConnectionReceiver.shouldIgnorePlayEvent()) {
            return
        }

        val libraryPlayerInteractor = appComponent.libraryPlayerInteractor()
        val playerInteractor = appComponent.playerInteractor()
        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (keyEvent.action == KeyEvent.ACTION_UP) {
                    AppAndroidUtils.playPause(context, playerInteractor)
                }
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                if (keyEvent.action == KeyEvent.ACTION_UP) {
                    libraryPlayerInteractor.skipToPrevious()
                }
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                if (keyEvent.action == KeyEvent.ACTION_UP) {
                    libraryPlayerInteractor.skipToNext()
                }
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                if (keyEvent.action == KeyEvent.ACTION_UP) {
                    AppAndroidUtils.playPause(context, playerInteractor)
                }
            }
            KeyEvent.KEYCODE_MEDIA_STEP_FORWARD,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> playerInteractor.fastSeekForward().subscribe()
            KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD,
            KeyEvent.KEYCODE_MEDIA_REWIND -> playerInteractor.fastSeekBackward().subscribe()
            else -> {
                val mediaSession = appComponent.mediaSessionHandler().getMediaSession()
                handleIntent(mediaSession, intent)
            }
        }
    }

    private companion object {
        const val UNSUPPORTED_EVENT_PROCESS_WINDOW_MILLIS = 1000L

        var lastUnsupportedEventTime = 0L
    }

}