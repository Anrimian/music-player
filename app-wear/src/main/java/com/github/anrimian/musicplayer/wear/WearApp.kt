package com.github.anrimian.musicplayer.wear

import android.app.Application
import com.github.anrimian.musicplayer.wear.di.Components

abstract class WearApp: Application() {

    override fun onCreate() {
        super.onCreate()

        initComponents()

        Components.getAppComponent().wearStateInteractor().onAppStarted()
    }

    protected abstract fun initComponents()

}