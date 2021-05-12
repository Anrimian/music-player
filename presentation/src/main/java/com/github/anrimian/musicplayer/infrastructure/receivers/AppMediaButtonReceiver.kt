package com.github.anrimian.musicplayer.infrastructure.receivers

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl
import com.github.anrimian.musicplayer.utils.Permissions

class AppMediaButtonReceiver: MediaButtonReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null
            || intent.action != Intent.ACTION_MEDIA_BUTTON
            || !intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
            return
        }
        val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        if (keyEvent != null) {
            handleExternalAction(context, keyEvent)
            return
        }
        //likely we never reach it
        super.onReceive(context, intent)
    }

    //we can move all external broadcast events to single method
    private fun handleExternalAction(context: Context, keyEvent: KeyEvent) {
        val appComponent = Components.getAppComponent()
        if (!Permissions.hasFilePermission(context)) {
            appComponent.notificationDisplayer().showErrorNotification(R.string.no_file_permission)
            return
        }

        val libraryPlayerInteractor = appComponent.libraryPlayerInteractor()
        val playerInteractor = appComponent.playerInteractor()
        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                SystemServiceControllerImpl.startPlayForegroundService(context)
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
                playerInteractor.pause()
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                playerInteractor.playOrPause()
            }
            KeyEvent.KEYCODE_MEDIA_STEP_FORWARD,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                playerInteractor.fastSeekForward()
            }
            KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD,
            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                playerInteractor.fastSeekBackward()
            }
            else -> appComponent.analytics().logMessage("unhandled key event: ${keyEvent.keyCode}")
        }
    }

}