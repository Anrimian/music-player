package com.github.anrimian.musicplayer.lite.di.app

import com.github.anrimian.musicplayer.di.app.ActionsModule
import com.github.anrimian.musicplayer.di.app.AppComponent
import com.github.anrimian.musicplayer.di.app.AppModule
import com.github.anrimian.musicplayer.di.app.AppSchedulerModule
import com.github.anrimian.musicplayer.di.app.DbModule
import com.github.anrimian.musicplayer.di.app.DispatcherModule
import com.github.anrimian.musicplayer.di.app.LibraryModule
import com.github.anrimian.musicplayer.di.app.MusicModule
import com.github.anrimian.musicplayer.di.app.PlayListsModule
import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.di.app.SettingsModule
import com.github.anrimian.musicplayer.di.app.StorageModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        SchedulerModule::class,
        AppSchedulerModule::class,
        DispatcherModule::class,
        MusicModule::class,
        DbModule::class,
        StorageModule::class,
        LibraryModule::class,
        SettingsModule::class,
        PlayListsModule::class,
        ActionsModule::class,
        LiteAppModule::class
    ]
)
interface LiteAppComponent : AppComponent
