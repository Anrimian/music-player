package com.github.anrimian.musicplayer.lite

import com.github.anrimian.musicplayer.App
import com.github.anrimian.musicplayer.lite.di.LiteComponents

class LiteApp: App() {

    override fun initComponents() {
        LiteComponents.init(applicationContext)
    }
}