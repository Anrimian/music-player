package com.github.anrimian.musicplayer.lite.di

import android.content.Context
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.lite.di.app.DaggerLiteAppComponent
import com.github.anrimian.musicplayer.lite.di.app.LiteAppComponent
import com.github.anrimian.musicplayer.lite.di.app.LiteAppModule

object LiteComponents {

    private lateinit var liteAppComponent: LiteAppComponent

    fun init(appContext: Context) {
        liteAppComponent = DaggerLiteAppComponent.builder()
            .liteAppModule(LiteAppModule(appContext))
            .build()

        Components.init(liteAppComponent.appComponent())
    }

}