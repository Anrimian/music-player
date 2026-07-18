package com.github.anrimian.musicplayer.wear.infrastructure.remote.complications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.anrimian.musicplayer.wear.Constants
import com.github.anrimian.musicplayer.wear.di.Components

class ComplicationActionReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getIntExtra(Constants.Actions.ACTION, 0)
        if (action == 0) {
            return
        }

        when(action) {
            Constants.Actions.PLAY_PAUSE -> Components.getAppComponent().wearStateInteractor().playPause()
        }
    }

}