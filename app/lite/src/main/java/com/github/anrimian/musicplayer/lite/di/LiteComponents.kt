package com.github.anrimian.musicplayer.lite.di

import android.content.Context
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.di.app.AppModule
import com.github.anrimian.musicplayer.lite.di.app.DaggerLiteAppComponent
import com.github.anrimian.musicplayer.lite.di.app.LiteAppComponent

object LiteComponents {

    private lateinit var liteAppComponent: LiteAppComponent

    fun init(appContext: Context) {
        liteAppComponent = DaggerLiteAppComponent.builder()
            .appModule(AppModule(appContext))
            .build()
        Components.init(liteAppComponent)
    }

    fun getLiteAppComponent(): LiteAppComponent {
        return liteAppComponent
    }

}
