package com.github.anrimian.musicplayer.wear.lite.di.app

import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.wear.di.app.AppComponent
import com.github.anrimian.musicplayer.wear.di.app.AppModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    SchedulerModule::class
])
interface LiteAppComponent : AppComponent
