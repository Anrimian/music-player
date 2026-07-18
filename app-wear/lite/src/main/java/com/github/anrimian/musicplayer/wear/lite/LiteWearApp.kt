package com.github.anrimian.musicplayer.wear.lite

import com.github.anrimian.musicplayer.wear.WearApp
import com.github.anrimian.musicplayer.wear.lite.di.LiteComponents

class LiteWearApp: WearApp() {

    override fun initComponents() {
        LiteComponents.init(applicationContext)
    }

}