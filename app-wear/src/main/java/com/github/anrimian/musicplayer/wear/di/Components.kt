package com.github.anrimian.musicplayer.wear.di

import com.github.anrimian.musicplayer.wear.di.app.AppComponent

object Components {

    private lateinit var appComponent: AppComponent

    fun init(appComponent: AppComponent) {
        this.appComponent = appComponent
    }

    fun getAppComponent() = appComponent
}