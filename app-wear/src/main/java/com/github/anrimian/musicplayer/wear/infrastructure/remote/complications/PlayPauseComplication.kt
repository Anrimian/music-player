package com.github.anrimian.musicplayer.wear.infrastructure.remote.complications

import android.app.PendingIntent
import android.content.Intent
import com.github.anrimian.musicplayer.wear.Constants
import com.github.anrimian.utils.createBroadcastPIntent

class PlayPauseComplication: BaseRangedComplication() {

    override fun getTapAction(): PendingIntent {
        val intentPlayPause = Intent(this, ComplicationActionReceiver::class.java)
        intentPlayPause.putExtra(Constants.Actions.ACTION, Constants.Actions.PLAY_PAUSE)
        return createBroadcastPIntent(intentPlayPause, Constants.Actions.PLAY_PAUSE)
    }

}