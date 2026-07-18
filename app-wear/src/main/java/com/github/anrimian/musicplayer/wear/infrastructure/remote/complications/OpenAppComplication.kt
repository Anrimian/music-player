package com.github.anrimian.musicplayer.wear.infrastructure.remote.complications

import android.app.PendingIntent
import android.content.Intent
import com.github.anrimian.musicplayer.wear.ui.MainActivity
import com.github.anrimian.utils.createActivityPIntent

class OpenAppComplication: BaseImageComplication() {//OpenAppImageComplication

    override fun getTapAction(): PendingIntent {
        return createActivityPIntent(Intent(this, MainActivity::class.java))
    }

}