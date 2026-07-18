package com.github.anrimian.musicplayer.wear.lite.di

import android.content.Context
import com.github.anrimian.musicplayer.wear.di.Components.init
import com.github.anrimian.musicplayer.wear.di.app.AppModule
import com.github.anrimian.musicplayer.wear.lite.di.app.DaggerLiteAppComponent
import com.github.anrimian.musicplayer.wear.lite.di.app.LiteAppComponent

class LiteComponents private constructor(appContext: Context) {

    companion object {
        private lateinit var instance: LiteComponents

        fun init(appContext: Context) {
            instance = LiteComponents(appContext)
        }

        private fun getInstance(): LiteComponents {
            if (!::instance.isInitialized) {
                throw IllegalStateException("components must be initialized first")
            }
            return instance
        }
    }

    private val liteAppComponent: LiteAppComponent

    init {
        liteAppComponent = DaggerLiteAppComponent.builder()
            .appModule(AppModule(appContext))
            .build()
        init(liteAppComponent)
    }

}
