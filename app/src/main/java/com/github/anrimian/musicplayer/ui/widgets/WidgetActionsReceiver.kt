package com.github.anrimian.musicplayer.ui.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.Permissions
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService
import com.github.anrimian.musicplayer.ui.common.AppAndroidUtils

class WidgetActionsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appComponent = Components.getAppComponent()
        if (!Permissions.hasFilePermission(context)) {
            appComponent.notificationsDisplayer().showErrorNotification(R.string.no_file_permission)
            return
        }

        val action = intent.getIntExtra(MusicService.REQUEST_CODE, 0)
        if (action == 0) {
            return
        }

        val interactor = appComponent.libraryPlayerInteractor()
        when (action) {
            Constants.Actions.SKIP_TO_PREVIOUS -> interactor.skipToPrevious()
            Constants.Actions.SKIP_TO_NEXT -> interactor.skipToNext()
            Constants.Actions.PAUSE,
            Constants.Actions.PLAY -> {
                AppAndroidUtils.playPause(context, appComponent.playerInteractor())
            }
            Constants.Actions.CHANGE_REPEAT_MODE -> interactor.changeRepeatMode()
            Constants.Actions.CHANGE_SHUFFLE_NODE -> {
                interactor.setRandomPlayingEnabled(!interactor.isRandomPlayingEnabled())
            }
            Constants.Actions.REWIND -> interactor.fastSeekBackward()
            Constants.Actions.FAST_FORWARD -> interactor.fastSeekForward()
        }
    }
}