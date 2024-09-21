package com.github.anrimian.musicplayer.lite

import android.content.Context
import com.github.anrimian.musicplayer.App
import com.github.anrimian.musicplayer.lite.di.LiteComponents

class LiteApp : App() {

    override fun initComponents(appContext: Context) {
        LiteComponents.init(appContext)
    }

}
