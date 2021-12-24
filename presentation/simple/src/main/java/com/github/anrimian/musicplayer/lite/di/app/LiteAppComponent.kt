package com.github.anrimian.musicplayer.lite.di.app

import com.github.anrimian.musicplayer.di.app.AppComponent
import dagger.Component

@LiteAppSingleton
@Component(modules = [ LiteAppModule::class ])
interface LiteAppComponent {

    fun appComponent(): AppComponent

}